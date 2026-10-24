package io.turnbase.games.sevenwondersduel.engine;

import io.turnbase.games.sevenwondersduel.id.PlayerId;

/**
 * The deciding player names who plays first in the Age (B-AGE-0).
 *
 * @param starter the player who takes the first turn
 */
public record ChooseStarter(PlayerId starter) implements PlayerAction {}
