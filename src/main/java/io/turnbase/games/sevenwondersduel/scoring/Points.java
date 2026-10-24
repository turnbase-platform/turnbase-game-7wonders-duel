package io.turnbase.games.sevenwondersduel.scoring;

import io.turnbase.games.sevenwondersduel.effect.Census;
import io.turnbase.games.sevenwondersduel.effect.Modifier;

/**
 * Victory points a component is worth at the end of the game (B-END-2), fixed or computed from the
 * owner's city. They count only while the component is live for its owner.
 */
public interface Points extends Modifier {

  int points(ScoreContext context);

  /** Worth {@code points} whatever the city holds. */
  static Points fixed(int points) {
    return new FixedPoints(points);
  }

  /** Worth {@code each} for every unit {@code census} counts at scoring. */
  static Points per(Census census, int each) {
    return new PointsPer(census, each);
  }
}
