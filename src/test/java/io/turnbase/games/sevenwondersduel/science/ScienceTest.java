package io.turnbase.games.sevenwondersduel.science;

import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P1;
import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.effect.Fixtures;
import io.turnbase.games.sevenwondersduel.effect.Fixtures.Card;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ScienceTest {

  private static final ScienceSymbol WHEEL = new ScienceSymbol("wheel");

  private static Card with(String id, ScienceSymbol symbol) {
    return new Card(
        io.turnbase.games.sevenwondersduel.id.ComponentId.of(id),
        List.of(),
        List.of(new Symbol(symbol)),
        List.of());
  }

  private static final Card A = with("a", WHEEL);
  private static final Card B = with("b", WHEEL);
  private static final Card C = with("c", ScienceSymbol.SCALES);
  private static final RuleSet RULES = Fixtures.rules(A, B, C);

  private static DuelState state(Object... placed) {
    return Fixtures.state(RULES, P1, 7, 7, placed);
  }

  @Test
  void onlyTheCityCounts() {
    DuelState s =
        state(
            A.id(), Fixtures.city(P1),
            B.id(), Fixtures.zone("discard"),
            C.id(), Fixtures.city(P2));

    assertThat(Science.counts(s, RULES, P1)).isEqualTo(Map.of(WHEEL, 1));
    assertThat(Science.counts(s, RULES, P2)).isEqualTo(Map.of(ScienceSymbol.SCALES, 1));
  }

  @Test
  void identicalSymbolsAddUpButCountOnceAsDistinct() {
    DuelState s =
        state(A.id(), Fixtures.city(P1), B.id(), Fixtures.city(P1), C.id(), Fixtures.city(P1));

    assertThat(Science.counts(s, RULES, P1)).isEqualTo(Map.of(WHEEL, 2, ScienceSymbol.SCALES, 1));
    assertThat(Science.distinct(s, RULES, P1)).isEqualTo(2);
  }

  @Test
  void aSymbolNeedsAnId() {
    assertThatThrownBy(() -> new ScienceSymbol(" ")).isInstanceOf(IllegalArgumentException.class);
  }
}
