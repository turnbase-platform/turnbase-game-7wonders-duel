package io.turnbase.games.sevenwondersduel.engine;

import io.turnbase.games.sevenwondersduel.id.ComponentId;

/**
 * Takes one of the offered Wonders in the draft (B-WND-2, B-WND-3).
 *
 * @param wonder the Wonder to take; must be in the {@code draft} zone
 */
public record DraftWonder(ComponentId wonder) implements PlayerAction {}
