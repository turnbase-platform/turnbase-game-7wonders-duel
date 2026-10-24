package io.turnbase.games.sevenwondersduel.effect;

import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.military.Military;

/**
 * The owner pushes the military pawn {@code count} spaces toward the opponent's capital (B-MIL-2).
 */
public record Shields(int count) implements Effect {

  public Shields {
    if (count <= 0) {
      throw new IllegalArgumentException("count must be positive: " + count);
    }
  }

  @Override
  public void apply(Resolution resolution, PlayerId owner) {
    Military.advance(resolution, owner, count);
  }
}
