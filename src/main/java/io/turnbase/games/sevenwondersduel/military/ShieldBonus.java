package io.turnbase.games.sevenwondersduel.military;

import io.turnbase.games.sevenwondersduel.catalog.Tag;
import io.turnbase.games.sevenwondersduel.effect.Badge;
import io.turnbase.games.sevenwondersduel.effect.Modifier;
import java.util.List;

/**
 * Its owner's shields from components carrying {@code tag} count {@code extra} more (Strategy).
 * Queried when the shields are applied, so it is neither retroactive nor reaches components without
 * the tag, such as Wonders.
 */
public record ShieldBonus(Tag tag, int extra) implements Modifier {

  public ShieldBonus {
    if (extra <= 0) {
      throw new IllegalArgumentException("extra must be positive: " + extra);
    }
  }

  @Override
  public List<Badge> badges() {
    return List.of(Badge.of("shieldBonus", extra, tag.value()));
  }
}
