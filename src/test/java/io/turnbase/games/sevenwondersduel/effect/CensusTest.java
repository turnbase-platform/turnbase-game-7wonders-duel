package io.turnbase.games.sevenwondersduel.effect;

import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P1;
import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P2;
import static org.assertj.core.api.Assertions.assertThat;

import io.turnbase.games.sevenwondersduel.catalog.Component;
import io.turnbase.games.sevenwondersduel.catalog.ComponentType;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.catalog.Tag;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CensusTest {

  private static final Tag RED = Tag.color("red");
  private static final Tag GREY = Tag.color("grey");

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

  private static int n;

  private static void add(
      List<Component> parts, List<Object> placed, Tag tag, int times, PlayerId at) {
    for (int i = 0; i < times; i++) {
      Tagged part = new Tagged(ComponentId.of("c" + n++), Set.of(tag));
      parts.add(part);
      placed.add(part.id());
      placed.add(Fixtures.city(at));
    }
  }

  /** P1 and P2 hold the given numbers of red and grey cards. */
  private static int count(
      Census census, PlayerId owner, int red1, int red2, int grey1, int grey2) {
    List<Component> parts = new ArrayList<>();
    List<Object> placed = new ArrayList<>();
    add(parts, placed, RED, red1, P1);
    add(parts, placed, RED, red2, P2);
    add(parts, placed, GREY, grey1, P1);
    add(parts, placed, GREY, grey2, P2);
    RuleSet rules = Fixtures.rules(parts.toArray(Component[]::new));
    return census.count(Fixtures.state(rules, P1, 7, 7, placed.toArray()), rules, owner);
  }

  private static int count(Census census, PlayerId owner, int red1, int red2) {
    return count(census, owner, red1, red2, 0, 0);
  }

  private static int coins(Census census, PlayerId owner, int coins1, int coins2) {
    RuleSet rules = Fixtures.rules();
    return census.count(Fixtures.state(rules, P1, coins1, coins2), rules, owner);
  }

  @Test
  void largerOfBothTakesTheOpponentsCityWhenItHoldsMore() {
    Census mostRed = Census.largerOfBoth(Census.of(RED));

    assertThat(count(mostRed, P1, 1, 4)).isEqualTo(4);
    assertThat(count(mostRed, P2, 1, 4)).isEqualTo(4);
  }

  @Test
  void largerOfBothTakesTheOwnersCityWhenItHoldsMore() {
    assertThat(count(Census.largerOfBoth(Census.of(RED)), P1, 5, 2)).isEqualTo(5);
  }

  @Test
  void largerOfBothOnATieIsTheSameEitherWay() {
    assertThat(count(Census.largerOfBoth(Census.of(RED)), P1, 3, 3)).isEqualTo(3);
  }

  @Test
  void sumAddsTheCountsOfOneCity() {
    assertThat(count(Census.sum(Census.of(RED), Census.of(GREY)), P1, 2, 0, 3, 0)).isEqualTo(5);
  }

  @Test
  void theMaximumOfASumIsNotTheSumOfMaxima() {
    // P1: 3 red + 0 grey = 3; P2: 0 red + 3 grey = 3. Maxima per colour would add to 6.
    Census both = Census.largerOfBoth(Census.sum(Census.of(RED), Census.of(GREY)));

    assertThat(count(both, P1, 3, 0, 0, 3)).isEqualTo(3);
  }

  @Test
  void coinsReadsTheOwnersTreasury() {
    assertThat(coins(Census.coins(), P1, 8, 2)).isEqualTo(8);
    assertThat(coins(Census.coins(), P2, 8, 2)).isEqualTo(2);
  }

  @Test
  void everyCountsWholeGroupsOnly() {
    Census thirds = Census.every(3, Census.coins());

    assertThat(coins(thirds, P1, 8, 2)).isEqualTo(2);
    assertThat(coins(thirds, P2, 8, 2)).isZero();
  }

  @Test
  void richestCityWinsEvenWhenItIsTheOpponents() {
    assertThat(coins(Census.every(3, Census.largerOfBoth(Census.coins())), P1, 2, 9)).isEqualTo(3);
  }
}
