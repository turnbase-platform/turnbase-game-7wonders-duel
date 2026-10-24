package io.turnbase.games.sevenwondersduel.cost;

import io.turnbase.games.sevenwondersduel.effect.Modifier;
import java.util.Set;

/** Changes what the owner pays per symbol bought from the bank (B-TRADE-3). */
public interface PriceModifier extends Modifier {

  /** The price after this modifier, given the price {@code current} before it. */
  int price(Resource resource, int current);

  /** Buying any of {@code resources} costs exactly {@code price}. */
  static PriceModifier fixedAt(int price, Resource... resources) {
    Set<Resource> covered = Set.of(resources);
    return (resource, current) -> covered.contains(resource) ? price : current;
  }
}
