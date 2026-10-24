package io.turnbase.games.sevenwondersduel.effect;

import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.event.EffectRef;
import io.turnbase.games.sevenwondersduel.event.GameEvent;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Offers each event to the triggers of live components and guards against runaway chains. One bus
 * belongs to one {@link Resolution}, which supplies the causation frame a reaction runs in.
 */
final class TriggerBus {

  private static final int MAX_DEPTH = 32;

  /** Runs a body with events attributed to {@code source} under {@code parentId}. */
  @FunctionalInterface
  interface Frames {
    void run(Optional<EffectRef> source, int parentId, Runnable body);
  }

  private int depth;

  /** Whether a trigger is reacting right now; triggers may not open decisions. */
  boolean reacting() {
    return depth > 0;
  }

  /**
   * Offers the event to live triggers, active player first (B-EFF-4). Candidates are fixed when the
   * event is emitted; each is re-checked just before it reacts.
   */
  void dispatch(
      DuelState state,
      RuleSet rules,
      GameEvent event,
      int eventId,
      Resolution resolution,
      Frames frames) {
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
                  && Ownership.liveComponents(resolution.state(), rules.zones(), player)
                      .contains(component)) {
                react(
                    trigger,
                    event,
                    player,
                    new EffectRef(component, i),
                    eventId,
                    resolution,
                    frames);
              }
            }
          }
        });
  }

  private <E extends GameEvent> void react(
      Trigger<E> trigger,
      GameEvent event,
      PlayerId owner,
      EffectRef ref,
      int parentId,
      Resolution resolution,
      Frames frames) {
    if (depth >= MAX_DEPTH) {
      throw new IllegalStateException(
          "trigger chain too deep (" + MAX_DEPTH + "), last source: " + ref.source().value());
    }
    depth++;
    try {
      frames.run(
          Optional.of(ref),
          parentId,
          () -> trigger.react(trigger.eventType().cast(event), owner, resolution));
    } finally {
      depth--;
    }
  }
}
