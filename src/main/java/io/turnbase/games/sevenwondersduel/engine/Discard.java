package io.turnbase.games.sevenwondersduel.engine;

import io.turnbase.games.sevenwondersduel.id.ComponentId;

/** Take an available card from the structure and sell it to the bank for coins (B-ACT-2). */
public record Discard(ComponentId card) implements PlayerAction {}
