package io.turnbase.games.sevenwondersduel.snapshot;

/**
 * Wire form of a {@code PlayerState}.
 *
 * @param coins the player's treasury
 * @param playAgain whether the player holds a play-again flag
 */
public record PlayerData(int coins, boolean playAgain) {}
