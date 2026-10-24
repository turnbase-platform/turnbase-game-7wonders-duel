package io.turnbase.games.sevenwondersduel.scoring;

import io.turnbase.games.sevenwondersduel.effect.Badge;
import java.util.List;

/** Worth {@code points} whatever the city holds. */
public record FixedPoints(int points) implements Points {

  @Override
  public int points(ScoreContext context) {
    return points;
  }

  @Override
  public List<Badge> badges() {
    return List.of(Badge.of("points", points));
  }
}
