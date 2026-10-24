package io.turnbase.games.sevenwondersduel.state;

/**
 * Per-player scalar state. {@code playAgain} is a flag per player (B-TURN-6): it is consumed at the
 * end of the holder's next turn, whoever granted it.
 *
 * @param coins the player's treasury, never negative
 * @param playAgain whether the player takes another turn straight after their next one
 */
public record PlayerState(int coins, boolean playAgain) {

  public PlayerState {
    if (coins < 0) {
      throw new IllegalArgumentException("coins must not be negative: " + coins);
    }
  }

  public PlayerState withCoins(int coins) {
    return new PlayerState(coins, playAgain);
  }

  public PlayerState withPlayAgain(boolean playAgain) {
    return new PlayerState(coins, playAgain);
  }
}
