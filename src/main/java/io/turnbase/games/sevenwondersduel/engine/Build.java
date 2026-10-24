package io.turnbase.games.sevenwondersduel.engine;

import io.turnbase.games.sevenwondersduel.id.ComponentId;

/** Construct the Building: pay its cost and put {@code card} in the actor's city (B-ACT-1). */
public record Build(ComponentId card) implements PlayerAction {}
