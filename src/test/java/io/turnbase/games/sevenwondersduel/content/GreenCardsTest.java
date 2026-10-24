package io.turnbase.games.sevenwondersduel.content;

import static org.assertj.core.api.Assertions.assertThat;

import io.turnbase.games.sevenwondersduel.catalog.Component;
import io.turnbase.games.sevenwondersduel.effect.ScienceCheck;
import io.turnbase.games.sevenwondersduel.science.Symbol;
import org.junit.jupiter.api.Test;

class GreenCardsTest {

  @Test
  void everyBaseCardShowingASymbolChecksScienceExactlyOnce() {
    for (Component component : new BaseContent().components()) {
      long symbols = component.modifiers().stream().filter(Symbol.class::isInstance).count();
      long checks = component.effects().stream().filter(ScienceCheck.class::isInstance).count();
      if (component instanceof Card card && card.tags().contains(Colors.GREEN)) {
        assertThat(symbols).as(component.name()).isEqualTo(1);
      }
      if (symbols > 0) {
        assertThat(checks).as(component.name()).isEqualTo(1);
      }
    }
  }
}
