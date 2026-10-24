package io.turnbase.games.sevenwondersduel.cost;

import io.turnbase.games.sevenwondersduel.catalog.Component;
import io.turnbase.games.sevenwondersduel.effect.Modifier;
import java.util.function.Predicate;

/**
 * Removes resource symbols from the cost of matching components (B-RED-1). The solver takes away
 * the dearest symbols the owner would otherwise have to buy.
 *
 * @param symbols how many symbols are removed
 * @param appliesTo which targets the reduction applies to
 */
public record Reduction(int symbols, Predicate<Component> appliesTo) implements Modifier {

  public Reduction {
    if (symbols < 0) {
      throw new IllegalArgumentException("symbols must not be negative: " + symbols);
    }
  }
}
