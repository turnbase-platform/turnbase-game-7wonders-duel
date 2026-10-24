package io.turnbase.games.sevenwondersduel.engine;

import io.turnbase.games.sevenwondersduel.id.ComponentId;

/**
 * Construct a Wonder: pay the Wonder's cost and tuck {@code card} under it (B-ACT-3). The card only
 * marks the turn; its own cost and effects are ignored.
 */
public record BuildWonder(ComponentId card, ComponentId wonder) implements PlayerAction {}
