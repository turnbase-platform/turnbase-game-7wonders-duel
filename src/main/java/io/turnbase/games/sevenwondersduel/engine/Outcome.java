package io.turnbase.games.sevenwondersduel.engine;

import io.turnbase.games.sevenwondersduel.event.EventRecord;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import java.util.List;

/**
 * What an accepted action produced.
 *
 * @param state the game after the action, paused at the next decision or settled
 * @param events everything that changed, as a causation tree
 */
public record Outcome(DuelState state, List<EventRecord> events) {

  public Outcome {
    events = List.copyOf(events);
  }
}
