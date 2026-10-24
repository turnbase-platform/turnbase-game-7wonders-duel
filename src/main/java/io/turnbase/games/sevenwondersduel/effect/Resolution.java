package io.turnbase.games.sevenwondersduel.effect;

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
import java.util.List;
import java.util.Optional;

/**
 * The working object for one player action: holds the current {@link DuelState}, changes it only
 * through a few primitive operations and records each change as an {@link EventRecord} in the
 * causation tree. Effects never touch the state directly.
 */
public final class Resolution {

  private record Frame(Optional<EffectRef> source, int parentId) {}

  private final RuleSet rules;
  private final List<EventRecord> events = new ArrayList<>();
  private final Deque<Frame> frames = new ArrayDeque<>();
  private DuelState state;

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

  /** Applies the instant effects of {@code source} in printed order for {@code owner} (B-EFF-4). */
  public void resolveEffects(ComponentId source, PlayerId owner) {
    List<Effect> effects = rules.catalog().get(source).effects();
    for (int i = 0; i < effects.size(); i++) {
      Effect effect = effects.get(i);
      within(new EffectRef(source, i), () -> effect.apply(this, owner));
    }
  }

  /** Runs {@code body}; events it emits are attributed to {@code source}. */
  public void within(EffectRef source, Runnable body) {
    frames.push(new Frame(Optional.of(source), currentParentId()));
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
    events.add(new EventRecord(events.size(), currentParentId(), source, event));
  }
}
