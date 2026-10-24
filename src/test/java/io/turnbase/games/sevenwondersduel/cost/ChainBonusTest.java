package io.turnbase.games.sevenwondersduel.cost;

import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P1;
import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P2;
import static org.assertj.core.api.Assertions.assertThat;

import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.effect.Fixtures;
import io.turnbase.games.sevenwondersduel.effect.Fixtures.Card;
import io.turnbase.games.sevenwondersduel.effect.Resolution;
import org.junit.jupiter.api.Test;

class ChainBonusTest {

  private static Resolution holding() {
    Card token = Card.of("urbanism").withTriggers(new ChainBonus(4));
    RuleSet rules = Fixtures.rules(token);
    return new Resolution(Fixtures.state(rules, P1, 7, 7, token.id(), Fixtures.city(P1)), rules);
  }

  @Test
  void theOwnersChainBuildPaysTheBonus() {
    Resolution r = holding();

    r.chainBuilt(P1);

    assertThat(r.state().player(P1).coins()).isEqualTo(11);
  }

  @Test
  void theOpponentsChainBuildPaysNothing() {
    Resolution r = holding();

    r.chainBuilt(P2);

    assertThat(r.state().player(P1).coins()).isEqualTo(7);
    assertThat(r.state().player(P2).coins()).isEqualTo(7);
  }
}
