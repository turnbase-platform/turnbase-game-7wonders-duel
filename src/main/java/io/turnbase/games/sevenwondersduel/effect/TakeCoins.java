package io.turnbase.games.sevenwondersduel.effect;

import io.turnbase.games.sevenwondersduel.id.PlayerId;

/**
 * The owner takes {@code min(amount, opponent treasury)} coins from the opponent and gains exactly
 * what was lost (B-EFF-2).
 */
public record TakeCoins(int amount) implements Effect {

  public TakeCoins {
    if (amount <= 0) {
      throw new IllegalArgumentException("amount must be positive: " + amount);
    }
  }

  @Override
  public void apply(Resolution resolution, PlayerId owner) {
    PlayerId victim = resolution.state().opponentOf(owner);
    int taken = Math.min(amount, resolution.state().player(victim).coins());
    resolution.changeCoins(victim, -taken);
    resolution.changeCoins(owner, taken);
  }
}
