package io.turnbase.games.sevenwondersduel.cost;

import io.turnbase.games.sevenwondersduel.effect.Modifier;
import java.util.Set;

/** The chain symbols a live component shows; each frees the components that name it (B-COST-5). */
public record ChainSource(Set<ChainSymbol> symbols) implements Modifier {

  public ChainSource {
    symbols = Set.copyOf(symbols);
  }
}
