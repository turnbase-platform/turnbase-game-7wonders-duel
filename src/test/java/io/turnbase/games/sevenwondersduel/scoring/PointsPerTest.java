package io.turnbase.games.sevenwondersduel.scoring;

import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P1;
import static org.assertj.core.api.Assertions.assertThat;

import io.turnbase.games.sevenwondersduel.catalog.Component;
import io.turnbase.games.sevenwondersduel.catalog.ComponentType;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.catalog.Tag;
import io.turnbase.games.sevenwondersduel.effect.Census;
import io.turnbase.games.sevenwondersduel.effect.Fixtures;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import org.junit.jupiter.api.Test;

class PointsPerTest {

  private static final Tag BLUE = Tag.color("blue");

  private record Blue(ComponentId id) implements Component {
    @Override
    public String name() {
      return id.value();
    }

    @Override
    public ComponentType type() {
      return ComponentType.CARD;
    }

    @Override
    public java.util.Set<Tag> tags() {
      return java.util.Set.of(BLUE);
    }
  }

  @Test
  void worthEachForEveryUnitTheCensusCounts() {
    Blue a = new Blue(ComponentId.of("a"));
    Blue b = new Blue(ComponentId.of("b"));
    RuleSet rules = Fixtures.rules(a, b);
    ScoreContext context =
        new ScoreContext(
            Fixtures.state(rules, P1, 7, 7, a.id(), Fixtures.city(P1), b.id(), Fixtures.city(P1)),
            rules,
            P1);

    assertThat(Points.per(Census.of(BLUE), 2).points(context)).isEqualTo(4);
  }

  @Test
  void nothingToCountIsWorthNothing() {
    RuleSet rules = Fixtures.rules();
    ScoreContext context = new ScoreContext(Fixtures.state(rules, P1, 7, 7), rules, P1);

    assertThat(Points.per(Census.of(BLUE), 2).points(context)).isZero();
  }
}
