package io.turnbase.games.sevenwondersduel.effect;

import io.turnbase.games.sevenwondersduel.catalog.Tag;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.military.Military;
import io.turnbase.games.sevenwondersduel.military.ShieldBonus;
import java.util.Set;

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
    Military.advance(resolution, owner, count + bonus(resolution, owner));
  }

  private static int bonus(Resolution resolution, PlayerId owner) {
    Set<Tag> tags =
        resolution.source().map(id -> resolution.rules().catalog().get(id).tags()).orElse(Set.of());
    return Ownership.modifiers(resolution.state(), resolution.rules(), owner, ShieldBonus.class)
        .stream()
        .filter(bonus -> tags.contains(bonus.tag()))
        .mapToInt(ShieldBonus::extra)
        .sum();
  }
}
