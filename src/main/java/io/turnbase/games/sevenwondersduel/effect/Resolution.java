package io.turnbase.games.sevenwondersduel.effect;

import io.turnbase.games.sevenwondersduel.agenda.DecisionStep;
import io.turnbase.games.sevenwondersduel.agenda.EffectStep;
import io.turnbase.games.sevenwondersduel.agenda.Option;
import io.turnbase.games.sevenwondersduel.agenda.Step;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.event.CoinsChanged;
import io.turnbase.games.sevenwondersduel.event.ComponentMoved;
import io.turnbase.games.sevenwondersduel.event.EffectRef;
import io.turnbase.games.sevenwondersduel.event.EventRecord;
import io.turnbase.games.sevenwondersduel.event.GameEvent;
import io.turnbase.games.sevenwondersduel.event.PlayAgainChanged;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.zone.Location;
import io.turnbase.games.sevenwondersduel.zone.Placements;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * The working object for one player action: holds the current {@link DuelState}, changes it only
 * through a few primitive operations and records each change as an {@link EventRecord} in the
 * causation tree. Effects never touch the state directly.
 *
 * <p>After an exception the instance is poisoned and must be discarded; the caller keeps the {@link
 * DuelState} it started from, which is never modified.
 */
public final class Resolution {

  private static final int MAX_TRIGGER_DEPTH = 32;

  private record Frame(Optional<EffectRef> source, int parentId) {}

  private final RuleSet rules;
  private final List<EventRecord> events = new ArrayList<>();
  private final Deque<Frame> frames = new ArrayDeque<>();
  private DuelState state;
  private final List<Step> staged = new ArrayList<>();

  private record Running(EffectRef effect, PlayerId owner) {}

  private Running running;
  private int triggerDepth;
  private boolean draining;

  public Resolution(DuelState state, RuleSet rules) {
    this.state = state;
    this.rules = rules;
  }

  public DuelState state() {
    return state;
  }

  public RuleSet rules() {
    return rules;
  }

  /** Events recorded so far, in emission order. */
  public List<EventRecord> events() {
    return Collections.unmodifiableList(events);
  }

  /** Moves a component (rejected out of a terminal zone) and records {@link ComponentMoved}. */
  public void move(ComponentId component, Location to) {
    Location from = Placements.locationOf(state.placements(), component);
    state = state.withPlacements(Placements.move(state.placements(), component, to, rules.zones()));
    emit(new ComponentMoved(component, from, to));
  }

  /** Changes a treasury by {@code delta}; a result below zero is a defect in the caller. */
  public void changeCoins(PlayerId player, int delta) {
    int coins = state.player(player).coins();
    if (coins + delta < 0) {
      throw new IllegalStateException(
          "player " + player.value() + " cannot go below 0 coins: " + coins + " " + delta);
    }
    if (delta == 0) {
      return;
    }
    state = state.withPlayer(player, state.player(player).withCoins(coins + delta));
    emit(new CoinsChanged(player, delta));
  }

  /** Sets a player's play-again flag; records {@link PlayAgainChanged} only on a change. */
  public void setPlayAgain(PlayerId player, boolean granted) {
    if (state.player(player).playAgain() == granted) {
      return;
    }
    state = state.withPlayer(player, state.player(player).withPlayAgain(granted));
    emit(new PlayAgainChanged(player, granted));
  }

  /**
   * Applies the instant effects of {@code source} in printed order for {@code owner} (B-EFF-4),
   * stopping early if one of them opens a decision; the rest wait on the agenda for the answer.
   */
  public void resolveEffects(ComponentId source, PlayerId owner) {
    queueEffects(source, owner);
    drain();
  }

  /** Puts one {@link EffectStep} per effect of {@code source} on the agenda, printed order. */
  public void queueEffects(ComponentId source, PlayerId owner) {
    requireOutsideTrigger();
    int count = rules.catalog().get(source).effects().size();
    List<Step> steps = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      steps.add(new EffectStep(new EffectRef(source, i), owner));
    }
    queue(steps);
  }

  /** Puts {@code steps} on the agenda in front of everything pending; the first becomes the top. */
  public void queue(List<Step> steps) {
    state = state.withAgenda(state.agenda().pushAll(steps));
  }

  /**
   * Asks {@code decider} to pick one of {@code options} for the effect that is running; the answer
   * goes to that effect's {@code resume}. With no options nothing is asked (B-EFF-3). Decisions
   * asked by one effect are answered in call order, so an effect puts the active player first
   * (B-EFF-4).
   */
  public void decide(PlayerId decider, List<Option> options) {
    requireOutsideTrigger();
    if (running == null) {
      throw new IllegalStateException("a decision can only be asked by a running effect");
    }
    if (!options.isEmpty()) {
      staged.add(new DecisionStep(decider, running.owner(), running.effect(), options));
    }
  }

  /**
   * Applies pending effect steps from the top of the agenda until it is empty or its top is a
   * decision.
   */
  public void drain() {
    if (draining || running != null) {
      throw new IllegalStateException("already draining the agenda");
    }
    draining = true;
    try {
      while (!state.agenda().isEmpty() && state.agenda().top() instanceof EffectStep step) {
        state = state.withAgenda(state.agenda().pop());
        runEffect(step.effect(), step.owner(), effect -> effect.apply(this, step.owner()));
      }
    } finally {
      draining = false;
    }
  }

  /**
   * Gives {@code answer} to the effect that asked the decision on top of the agenda. The caller
   * then drains to carry on with the steps below it.
   */
  public void resume(Option answer) {
    if (state.agenda().isEmpty() || !(state.agenda().top() instanceof DecisionStep step)) {
      throw new IllegalStateException("no decision is pending");
    }
    state = state.withAgenda(state.agenda().pop());
    runEffect(step.effect(), step.owner(), effect -> effect.resume(this, step.owner(), answer));
  }

  private void runEffect(EffectRef ref, PlayerId owner, Consumer<Effect> action) {
    Effect effect = effectOf(ref);
    running = new Running(ref, owner);
    try {
      within(ref, () -> action.accept(effect));
    } finally {
      running = null;
    }
    queue(staged);
    staged.clear();
  }

  private Effect effectOf(EffectRef ref) {
    List<Effect> effects;
    try {
      effects = rules.catalog().get(ref.source()).effects();
    } catch (IllegalArgumentException e) {
      throw new IllegalStateException("stale effect reference " + describe(ref), e);
    }
    if (ref.index() < 0 || ref.index() >= effects.size()) {
      throw new IllegalStateException("stale effect reference " + describe(ref));
    }
    return effects.get(ref.index());
  }

  private static String describe(EffectRef ref) {
    return ref.source().value() + "#" + ref.index();
  }

  private void requireOutsideTrigger() {
    if (triggerDepth > 0) {
      throw new IllegalStateException("triggers cannot open decisions or queue effects");
    }
  }

  /** Runs {@code body}; events it emits are attributed to {@code source}. */
  public void within(EffectRef source, Runnable body) {
    inFrame(Optional.of(source), currentParentId(), body);
  }

  private void inFrame(Optional<EffectRef> source, int parentId, Runnable body) {
    frames.push(new Frame(source, parentId));
    try {
      body.run();
    } finally {
      frames.pop();
    }
  }

  private int currentParentId() {
    return frames.isEmpty() ? -1 : frames.peek().parentId();
  }

  private void emit(GameEvent event) {
    Optional<EffectRef> source = frames.isEmpty() ? Optional.empty() : frames.peek().source();
    int id = events.size();
    events.add(new EventRecord(id, currentParentId(), source, event));
    dispatch(id, event);
  }

  /**
   * Offers the event to live triggers, active player first (B-EFF-4). Candidates are fixed when the
   * event is emitted; each is re-checked just before it reacts.
   */
  private void dispatch(int id, GameEvent event) {
    PlayerId active = state.active();
    Map<PlayerId, List<ComponentId>> candidates = new LinkedHashMap<>();
    for (PlayerId player : List.of(active, state.opponentOf(active))) {
      candidates.put(player, Ownership.liveComponents(state, rules.zones(), player));
    }
    candidates.forEach(
        (player, components) -> {
          for (ComponentId component : components) {
            List<Trigger<?>> triggers = rules.catalog().get(component).triggers();
            for (int i = 0; i < triggers.size(); i++) {
              Trigger<?> trigger = triggers.get(i);
              if (trigger.eventType().isInstance(event)
                  && Ownership.liveComponents(state, rules.zones(), player).contains(component)) {
                react(trigger, event, player, new EffectRef(component, i), id);
              }
            }
          }
        });
  }

  private <E extends GameEvent> void react(
      Trigger<E> trigger, GameEvent event, PlayerId owner, EffectRef ref, int parentId) {
    if (triggerDepth >= MAX_TRIGGER_DEPTH) {
      throw new IllegalStateException(
          "trigger chain too deep ("
              + MAX_TRIGGER_DEPTH
              + "), last source: "
              + ref.source().value());
    }
    triggerDepth++;
    try {
      inFrame(
          Optional.of(ref),
          parentId,
          () -> trigger.react(trigger.eventType().cast(event), owner, this));
    } finally {
      triggerDepth--;
    }
  }
}
