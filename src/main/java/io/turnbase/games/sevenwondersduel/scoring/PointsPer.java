package io.turnbase.games.sevenwondersduel.scoring;

import io.turnbase.games.sevenwondersduel.effect.Census;

/** {@code each} points for every unit the {@code census} counts for the owner at scoring. */
public record PointsPer(Census census, int each) implements Points {

  public PointsPer {
    if (each <= 0) {
      throw new IllegalArgumentException("each must be positive: " + each);
    }
  }

  @Override
  public int points(ScoreContext context) {
    return each * census.count(context.state(), context.rules(), context.owner());
  }
}
