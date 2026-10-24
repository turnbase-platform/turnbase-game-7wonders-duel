package io.turnbase.games.sevenwondersduel.military;

import io.turnbase.games.sevenwondersduel.catalog.Tag;
import io.turnbase.games.sevenwondersduel.effect.Modifier;

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
}
