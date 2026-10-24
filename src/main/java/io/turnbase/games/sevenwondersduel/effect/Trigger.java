package io.turnbase.games.sevenwondersduel.effect;

import io.turnbase.games.sevenwondersduel.event.GameEvent;
import io.turnbase.games.sevenwondersduel.id.PlayerId;

/**
 * Reacts to an event type while its source is live for a player (B-EFF-4).
 *
 * @param <E> the event type reacted to
 */
public interface Trigger<E extends GameEvent> {

  Class<E> eventType();

  /**
   * Reacts to an event.
   *
   * @param event the event
   * @param owner the player for whom the trigger's source is live
   * @param resolution the running resolution
   */
  void react(E event, PlayerId owner, Resolution resolution);
}
