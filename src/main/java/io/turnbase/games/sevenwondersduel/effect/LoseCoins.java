package io.turnbase.games.sevenwondersduel.effect;

import io.turnbase.games.sevenwondersduel.id.PlayerId;

/** The owner's opponent loses {@code min(amount, treasury)} coins to the bank (B-EFF-2). */
public record LoseCoins(int amount) implements Effect {

  public LoseCoins {
    if (amount <= 0) {
      throw new IllegalArgumentException("amount must be positive: " + amount);
    }
  }

  @Override
  public void apply(Resolution resolution, PlayerId owner) {
    PlayerId victim = resolution.state().opponentOf(owner);
    resolution.changeCoins(victim, -Math.min(amount, resolution.state().player(victim).coins()));
  }
}
