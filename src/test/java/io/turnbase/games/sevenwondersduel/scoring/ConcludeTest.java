package io.turnbase.games.sevenwondersduel.scoring;

import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P1;
import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P2;
import static io.turnbase.games.sevenwondersduel.effect.Fixtures.city;
import static org.assertj.core.api.Assertions.assertThat;

import io.turnbase.games.sevenwondersduel.catalog.Component;
import io.turnbase.games.sevenwondersduel.catalog.ComponentType;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.catalog.Tag;
import io.turnbase.games.sevenwondersduel.effect.Fixtures;
import io.turnbase.games.sevenwondersduel.effect.Modifier;
import io.turnbase.games.sevenwondersduel.effect.Resolution;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.state.Phase;
import io.turnbase.games.sevenwondersduel.state.Victory;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ConcludeTest {

  private record Card(ComponentId id, Set<Tag> tags, List<Modifier> modifiers)
      implements Component {

    static Card worth(String id, int points, Tag... tags) {
      return new Card(ComponentId.of(id), Set.of(tags), List.of(Points.fixed(points)));
    }

    @Override
    public String name() {
      return id.value();
    }

    @Override
    public ComponentType type() {
      return ComponentType.CARD;
    }
  }

  private static Resolution concluded(int coins1, int coins2, Card[] cards, Object... placed) {
    RuleSet rules = Fixtures.rules(cards);
    Resolution resolution =
        new Resolution(Fixtures.state(rules, P1, coins1, coins2, placed), rules);
    Scoring.conclude(resolution);
    return resolution;
  }

  @Test
  void theHigherTotalWins() {
    Card a = Card.worth("a", 5);
    Card b = Card.worth("b", 3);

    Resolution r = concluded(0, 0, new Card[] {a, b}, a.id(), city(P1), b.id(), city(P2));

    assertThat(r.state().phase()).isEqualTo(Phase.OVER);
    assertThat(r.state().victory()).isEqualTo(new Victory(P1, Victory.Reason.POINTS));
  }

  @Test
  void treasuryCountsTowardsTheTotal() {
    Card a = Card.worth("a", 1);
    Card b = Card.worth("b", 2);

    Resolution r = concluded(6, 0, new Card[] {a, b}, a.id(), city(P1), b.id(), city(P2));

    assertThat(r.state().victory().winner()).isEqualTo(P1);
  }

  @Test
  void equalTotalsGoToTheMoreBluePoints() {
    Card blue = Card.worth("blue", 3, Tag.BLUE);
    Card green = Card.worth("green", 3);

    Resolution r =
        concluded(0, 0, new Card[] {blue, green}, blue.id(), city(P2), green.id(), city(P1));

    assertThat(r.state().victory()).isEqualTo(new Victory(P2, Victory.Reason.POINTS));
  }

  @Test
  void equalTotalsAndBlueAreASharedVictory() {
    Card a = Card.worth("a", 3);
    Card b = Card.worth("b", 3);

    Resolution r = concluded(0, 0, new Card[] {a, b}, a.id(), city(P1), b.id(), city(P2));

    assertThat(r.state().phase()).isEqualTo(Phase.OVER);
    assertThat(r.state().victory()).isEqualTo(new Victory(null, Victory.Reason.POINTS));
  }
}
