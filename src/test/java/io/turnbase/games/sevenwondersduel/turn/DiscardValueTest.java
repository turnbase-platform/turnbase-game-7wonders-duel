package io.turnbase.games.sevenwondersduel.turn;

import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P1;
import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P2;
import static org.assertj.core.api.Assertions.assertThat;

import io.turnbase.games.sevenwondersduel.catalog.Component;
import io.turnbase.games.sevenwondersduel.catalog.ComponentType;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.catalog.Tag;
import io.turnbase.games.sevenwondersduel.effect.Fixtures;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import java.util.Set;
import org.junit.jupiter.api.Test;

class DiscardValueTest {

  private record Tagged(ComponentId id, Set<Tag> tags) implements Component {
    @Override
    public String name() {
      return id.value();
    }

    @Override
    public ComponentType type() {
      return ComponentType.CARD;
    }
  }

  private static Tagged yellow(String id) {
    return new Tagged(ComponentId.of(id), Set.of(Tag.YELLOW));
  }

  private static Tagged blue(String id) {
    return new Tagged(ComponentId.of(id), Set.of(Tag.of("color:blue")));
  }

  private static final Tagged Y1 = yellow("y1");
  private static final Tagged Y2 = yellow("y2");
  private static final Tagged B1 = blue("b1");
  private static final RuleSet RULES = Fixtures.rules(Y1, Y2, B1);

  private static DuelState with(Object... placed) {
    return Fixtures.state(RULES, P1, 7, 7, placed);
  }

  @Test
  void isTwoWithNoYellowCards() {
    assertThat(DiscardValue.of(RULES, with(B1.id(), Fixtures.city(P1)), P1)).isEqualTo(2);
  }

  @Test
  void addsOnePerYellowCardInTheOwnCity() {
    DuelState state =
        with(Y1.id(), Fixtures.city(P1), Y2.id(), Fixtures.city(P1), B1.id(), Fixtures.city(P1));

    assertThat(DiscardValue.of(RULES, state, P1)).isEqualTo(4);
  }

  @Test
  void ignoresYellowCardsOutsideTheCityAndInTheOpponents() {
    DuelState state = with(Y1.id(), Fixtures.city(P2), Y2.id(), Fixtures.zone("discard"));

    assertThat(DiscardValue.of(RULES, state, P1)).isEqualTo(2);
  }
}
