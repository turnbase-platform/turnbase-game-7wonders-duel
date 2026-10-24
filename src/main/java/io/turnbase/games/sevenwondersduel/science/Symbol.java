package io.turnbase.games.sevenwondersduel.science;

import io.turnbase.games.sevenwondersduel.effect.Badge;
import io.turnbase.games.sevenwondersduel.effect.Modifier;
import java.util.List;

/** The component shows a scientific symbol while it is in its owner's city (B-SCI-2). */
public record Symbol(ScienceSymbol symbol) implements Modifier {

  @Override
  public List<Badge> badges() {
    return List.of(Badge.of("science", 0, symbol.id()));
  }
}
