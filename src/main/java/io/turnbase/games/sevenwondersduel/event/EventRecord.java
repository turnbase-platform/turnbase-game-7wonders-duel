package io.turnbase.games.sevenwondersduel.event;

import java.util.Optional;

/**
 * An event with its place in the causation tree (design §4.8).
 *
 * @param id sequential within one resolution, starting at 0
 * @param parentId id of the event that caused this one, or -1 for a root event
 * @param source the effect or trigger that emitted it; empty for the player's own action
 * @param event what happened
 */
public record EventRecord(int id, int parentId, Optional<EffectRef> source, GameEvent event) {}
