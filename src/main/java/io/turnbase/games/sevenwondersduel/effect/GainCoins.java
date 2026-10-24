package io.turnbase.games.sevenwondersduel.effect;

import io.turnbase.games.sevenwondersduel.id.PlayerId;
import java.util.List;

/** The owner gains {@code amount} coins. */
public record GainCoins(int amount) implements Effect {

  public GainCoins {
    if (amount <= 0) {
      throw new IllegalArgumentException("amount must be positive: " + amount);
    }
  }

  @Override
  public void apply(Resolution resolution, PlayerId owner) {
    resolution.changeCoins(owner, amount);
  }

  @Override
  public List<Badge> badges() {
    return List.of(Badge.of("coins", amount));
  }
}
