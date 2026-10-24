package io.turnbase.games.sevenwondersduel.effect;

import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P1;
import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P2;
import static org.assertj.core.api.Assertions.assertThat;

import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.effect.Fixtures.Card;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.rng.Rng;
import java.util.List;
import org.junit.jupiter.api.Test;

class ResolutionDrawTest {

  private static final RuleSet RULES = Fixtures.rules(Card.of("a"), Card.of("b"), Card.of("c"));

  private static Resolution resolution() {
    return new Resolution(Fixtures.state(RULES, P1, 7, 7), RULES);
  }

  @Test
  void shuffleFollowsTheSeededStreamAndAdvancesTheCursor() {
    Resolution r = resolution();
    List<String> items = List.of("x", "y", "z", "w");

    Rng.Drawn<List<String>> expected = Rng.shuffle(items, r.state().rng());
    List<String> shuffled = r.shuffle(items);

    assertThat(shuffled).isEqualTo(expected.value());
    assertThat(r.state().rng()).isEqualTo(expected.next());
  }

  @Test
  void revealToTeachesOnePlayerOnly() {
    Resolution r = resolution();
    ComponentId a = ComponentId.of("a");

    r.revealTo(P1, a);

    assertThat(r.state().knowledge().get(P1)).containsExactly(a);
    assertThat(r.state().knowledge().get(P2)).isEmpty();
  }
}
