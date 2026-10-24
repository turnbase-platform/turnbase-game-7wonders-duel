package io.turnbase.games.sevenwondersduel.effect;

import io.turnbase.games.sevenwondersduel.id.PlayerId;

/**
 * The owner gains {@code each} coins for every unit the {@code census} counts, once, when the
 * effect is applied. Counting nothing pays nothing.
 */
public record GainCoinsPer(Census census, int each) implements Effect {

  public GainCoinsPer {
    if (each <= 0) {
      throw new IllegalArgumentException("each must be positive: " + each);
    }
  }

  @Override
  public void apply(Resolution resolution, PlayerId owner) {
    int units = census.count(resolution.state(), resolution.rules(), owner);
    if (units > 0) {
      resolution.changeCoins(owner, each * units);
    }
  }
}
