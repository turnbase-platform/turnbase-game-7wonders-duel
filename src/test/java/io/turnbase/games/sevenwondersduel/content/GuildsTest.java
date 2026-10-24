package io.turnbase.games.sevenwondersduel.content;

import static io.turnbase.games.sevenwondersduel.content.CardAssertions.named;
import static io.turnbase.games.sevenwondersduel.cost.Resource.*;
import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P1;
import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P2;
import static org.assertj.core.api.Assertions.assertThat;

import io.turnbase.games.sevenwondersduel.catalog.BasePack;
import io.turnbase.games.sevenwondersduel.catalog.Component;
import io.turnbase.games.sevenwondersduel.catalog.ComponentType;
import io.turnbase.games.sevenwondersduel.catalog.ContentPack;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.catalog.Tag;
import io.turnbase.games.sevenwondersduel.cost.Cost;
import io.turnbase.games.sevenwondersduel.effect.Fixtures;
import io.turnbase.games.sevenwondersduel.effect.Resolution;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PackId;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.scoring.Points;
import io.turnbase.games.sevenwondersduel.scoring.ScoreContext;
import io.turnbase.games.sevenwondersduel.scoring.Scoring;
import io.turnbase.games.sevenwondersduel.zone.Location;
import io.turnbase.games.sevenwondersduel.zone.ZoneKind;
import io.turnbase.games.sevenwondersduel.zone.Zones;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class GuildsTest {

  private static final List<Card> GUILDS = Guilds.cards();

  private record TestWonder(ComponentId id) implements Component {
    @Override
    public String name() {
      return id.value();
    }

    @Override
    public ComponentType type() {
      return ComponentType.WONDER;
    }

    @Override
    public Set<Tag> tags() {
      return Set.of(Tag.WONDER);
    }
  }

  private static final RuleSet RULES =
      RuleSet.assemble(
          List.of(
              new BasePack(),
              new BaseContent(),
              new ContentPack() {
                @Override
                public PackId id() {
                  return PackId.of("test-wonders");
                }

                @Override
                public List<ZoneKind> zoneKinds() {
                  return List.of();
                }

                @Override
                public List<Component> components() {
                  return List.of(
                      new TestWonder(ComponentId.of("w1")),
                      new TestWonder(ComponentId.of("w2")),
                      new TestWonder(ComponentId.of("w3")));
                }
              }));

  private static Location builtWonders(PlayerId player) {
    return new Location(Zones.WONDERS_BUILT, player.value().toString());
  }

  /** Placements: P1's city, P2's city, plus built Wonders by id. */
  private static io.turnbase.games.sevenwondersduel.state.DuelState state(
      int coins1,
      int coins2,
      String guild,
      List<String> mine,
      List<String> theirs,
      String... wonders) {
    List<Object> placed = new ArrayList<>();
    placed.add(ComponentId.of(guild));
    placed.add(Fixtures.city(P1));
    for (String id : mine) {
      placed.add(ComponentId.of(id));
      placed.add(Fixtures.city(P1));
    }
    for (String id : theirs) {
      placed.add(ComponentId.of(id));
      placed.add(Fixtures.city(P2));
    }
    for (int i = 0; i < wonders.length; i++) {
      placed.add(ComponentId.of(wonders[i]));
      placed.add(builtWonders(i == 0 ? P1 : P2));
    }
    return Fixtures.state(RULES, P1, coins1, coins2, placed.toArray());
  }

  private static int coinsPaid(String guild, List<String> mine, List<String> theirs) {
    Resolution r = new Resolution(state(7, 7, guild, mine, theirs), RULES);
    r.resolveEffects(ComponentId.of(guild), P1);
    return r.state().player(P1).coins() - 7;
  }

  private static int points(
      String guild,
      int coins1,
      int coins2,
      List<String> mine,
      List<String> theirs,
      String... wonders) {
    ScoreContext context =
        new ScoreContext(state(coins1, coins2, guild, mine, theirs, wonders), RULES, P1);
    return GUILDS.stream()
        .filter(g -> g.id().value().equals(guild))
        .flatMap(g -> g.modifiers().stream())
        .filter(Points.class::isInstance)
        .mapToInt(m -> ((Points) m).points(context))
        .sum();
  }

  @Test
  void sevenPurpleGuildsWithTheGuildTagTheDarkPurpleBackAndNoAge() {
    assertThat(GUILDS).hasSize(7);
    assertThat(GUILDS.stream().map(Card::id).distinct()).hasSize(7);
    assertThat(GUILDS)
        .allSatisfy(
            guild -> {
              assertThat(guild.tags()).contains(Tag.GUILD, Colors.PURPLE);
              assertThat(guild.tags()).noneMatch(t -> t.equals(Tag.age(1)) || t.equals(Tag.age(2)));
              assertThat(guild.tags()).doesNotContain(Tag.age(3));
              assertThat(guild.back()).contains(Backs.DARK_PURPLE);
            });
  }

  @Test
  void costsFollowTheRulebook() {
    assertThat(named(GUILDS, "Merchants Guild").cost())
        .isEqualTo(Cost.of(0, CLAY, WOOD, GLASS, PAPYRUS));
    assertThat(named(GUILDS, "Shipowners Guild").cost())
        .isEqualTo(Cost.of(0, CLAY, STONE, GLASS, PAPYRUS));
    assertThat(named(GUILDS, "Builders Guild").cost())
        .isEqualTo(Cost.of(0, STONE, STONE, CLAY, WOOD, GLASS));
    assertThat(named(GUILDS, "Magistrates Guild").cost())
        .isEqualTo(Cost.of(0, WOOD, WOOD, CLAY, PAPYRUS));
    assertThat(named(GUILDS, "Scientists Guild").cost())
        .isEqualTo(Cost.of(0, CLAY, CLAY, WOOD, WOOD));
    assertThat(named(GUILDS, "Moneylenders Guild").cost())
        .isEqualTo(Cost.of(0, STONE, STONE, WOOD, WOOD));
    assertThat(named(GUILDS, "Tacticians Guild").cost())
        .isEqualTo(Cost.of(0, STONE, STONE, CLAY, PAPYRUS));
  }

  @Test
  void merchantsPayForTheCityWithMostYellow() {
    assertThat(coinsPaid("merchants-guild", List.of("tavern"), List.of("brewery", "forum")))
        .isEqualTo(2);
  }

  @Test
  void aGuildWithNothingToCountPaysNothing() {
    assertThat(coinsPaid("merchants-guild", List.of(), List.of())).isZero();
  }

  @Test
  void shipownersCountBrownAndGreyInOneCity() {
    // P1: 3 brown. P2: 1 brown + 3 grey = 4. One maximum over the sum, not 3 + 3.
    assertThat(
            coinsPaid(
                "shipowners-guild",
                List.of("lumber-yard", "clay-pool", "quarry"),
                List.of("logging-camp", "glassworks", "press", "glass-blower")))
        .isEqualTo(4);
  }

  @Test
  void magistratesScientistsAndTacticiansEachPayForTheirColour() {
    assertThat(coinsPaid("magistrates-guild", List.of("theater"), List.of())).isEqualTo(1);
    assertThat(coinsPaid("scientists-guild", List.of(), List.of("workshop", "apothecary")))
        .isEqualTo(2);
    assertThat(coinsPaid("tacticians-guild", List.of("guard-tower", "stable"), List.of()))
        .isEqualTo(2);
  }

  @Test
  void buildersAndMoneylendersPayNothingOnConstruction() {
    assertThat(named(GUILDS, "Builders Guild").effects()).isEmpty();
    assertThat(named(GUILDS, "Moneylenders Guild").effects()).isEmpty();
  }

  @Test
  void merchantsScoreOnePerYellowInTheCityWithMost() {
    assertThat(points("merchants-guild", 0, 0, List.of("tavern"), List.of("brewery", "forum")))
        .isEqualTo(2);
  }

  @Test
  void guildPointsCountInTheBuildingsLineNeverTheBlueTieBreak() {
    var card =
        Scoring.score(
            state(0, 0, "magistrates-guild", List.of("theater", "baths"), List.of()), RULES, P1);

    assertThat(card.line(Scoring.BUILDINGS)).isEqualTo(2);
    assertThat(card.line(Scoring.BLUE)).isEqualTo(6);
  }

  @Test
  void buildersScoreTwoPerWonderInTheCityWithMost() {
    // P1 has one built Wonder, P2 has two: the guild counts P2's.
    assertThat(points("builders-guild", 0, 0, List.of(), List.of(), "w1", "w2", "w3")).isEqualTo(4);
  }

  @Test
  void moneylendersScoreOnePerWholeThreeCoinsInTheRichestCity() {
    assertThat(points("moneylenders-guild", 8, 2, List.of(), List.of())).isEqualTo(2);
    assertThat(points("moneylenders-guild", 2, 2, List.of(), List.of())).isZero();
    assertThat(points("moneylenders-guild", 2, 9, List.of(), List.of())).isEqualTo(3);
  }

  @Test
  void theCoinsAndThePointsMayNameDifferentCities() {
    // On construction P1 leads in yellow; by the end P2 leads: each takes its own maximum.
    assertThat(coinsPaid("merchants-guild", List.of("tavern", "brewery"), List.of("forum")))
        .isEqualTo(2);
    assertThat(
            points(
                "merchants-guild",
                0,
                0,
                List.of("tavern"),
                List.of("brewery", "forum", "caravansery")))
        .isEqualTo(3);
  }
}
