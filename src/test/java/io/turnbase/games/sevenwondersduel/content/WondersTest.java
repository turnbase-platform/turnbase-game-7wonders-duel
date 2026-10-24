package io.turnbase.games.sevenwondersduel.content;

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
import io.turnbase.games.sevenwondersduel.cost.Production;
import io.turnbase.games.sevenwondersduel.effect.DiscardOpponentCard;
import io.turnbase.games.sevenwondersduel.effect.DrawProgressToken;
import io.turnbase.games.sevenwondersduel.effect.Fixtures;
import io.turnbase.games.sevenwondersduel.effect.GainCoins;
import io.turnbase.games.sevenwondersduel.effect.GrantPlayAgain;
import io.turnbase.games.sevenwondersduel.effect.LoseCoins;
import io.turnbase.games.sevenwondersduel.effect.Resolution;
import io.turnbase.games.sevenwondersduel.effect.Shields;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PackId;
import io.turnbase.games.sevenwondersduel.military.ShieldBonus;
import io.turnbase.games.sevenwondersduel.scoring.Points;
import io.turnbase.games.sevenwondersduel.scoring.ScoreContext;
import io.turnbase.games.sevenwondersduel.state.Pawn;
import io.turnbase.games.sevenwondersduel.zone.ZoneKind;
import java.util.List;
import org.junit.jupiter.api.Test;

class WondersTest {

  private static final List<Wonder> WONDERS = Wonders.all();

  private static Wonder wonder(String name) {
    return WONDERS.stream().filter(w -> w.name().equals(name)).findFirst().orElseThrow();
  }

  private static int pointsOf(Wonder w) {
    RuleSet rules = RuleSet.assemble(List.of(new BasePack(), new BaseContent()));
    ScoreContext context = new ScoreContext(Fixtures.state(rules, P1, 7, 7), rules, P1);
    return w.modifiers().stream()
        .filter(Points.class::isInstance)
        .mapToInt(m -> ((Points) m).points(context))
        .sum();
  }

  private static void assertWonder(
      String name, Cost cost, int points, List<?> effects, boolean producesChoice) {
    Wonder w = wonder(name);
    assertThat(w.cost()).as(name + " cost").isEqualTo(cost);
    assertThat(w.effects()).as(name + " effects").isEqualTo(effects);
    assertThat(pointsOf(w)).as(name + " points").isEqualTo(points);
    assertThat(w.modifiers().stream().anyMatch(Production.class::isInstance))
        .as(name + " production")
        .isEqualTo(producesChoice);
  }

  @Test
  void twelveDistinctWondersAreTaggedAsWondersWithNoBack() {
    assertThat(WONDERS).hasSize(12);
    assertThat(WONDERS.stream().map(Wonder::id).distinct()).hasSize(12);
    assertThat(WONDERS)
        .allSatisfy(
            w -> {
              assertThat(w.type()).isEqualTo(ComponentType.WONDER);
              assertThat(w.tags()).containsExactly(Tag.WONDER);
              assertThat(w.back()).isEmpty();
            });
  }

  @Test
  void everyWonderInTheBaseCatalogueCarriesTheWonderTag() {
    RuleSet rules = RuleSet.assemble(List.of(new BasePack(), new BaseContent()));

    assertThat(rules.catalog().ofType(ComponentType.WONDER)).hasSize(12);
    assertThat(rules.catalog().ofType(ComponentType.WONDER))
        .allSatisfy(w -> assertThat(w.tags()).contains(Tag.WONDER));
    assertThat(rules.catalog().tagged(Tag.WONDER)).hasSize(12);
  }

  @Test
  void costsEffectsAndPointsMatchSection37() {
    assertWonder(
        "The Appian Way",
        Cost.of(0, STONE, STONE, CLAY, CLAY, PAPYRUS),
        3,
        List.of(new GainCoins(3), new LoseCoins(3), new GrantPlayAgain()),
        false);
    assertWonder(
        "Circus Maximus",
        Cost.of(0, STONE, STONE, WOOD, GLASS),
        3,
        List.of(new DiscardOpponentCard(Colors.GREY), new Shields(1)),
        false);
    assertWonder(
        "The Colossus", Cost.of(0, CLAY, CLAY, CLAY, GLASS), 3, List.of(new Shields(2)), false);
    assertWonder(
        "The Great Library",
        Cost.of(0, WOOD, WOOD, WOOD, GLASS, PAPYRUS),
        4,
        List.of(new DrawProgressToken(3)),
        false);
    assertWonder(
        "The Great Lighthouse", Cost.of(0, WOOD, STONE, PAPYRUS, PAPYRUS), 4, List.of(), true);
    assertWonder(
        "The Hanging Gardens",
        Cost.of(0, WOOD, WOOD, GLASS, PAPYRUS),
        3,
        List.of(new GainCoins(6), new GrantPlayAgain()),
        false);
    assertWonder(
        "The Mausoleum",
        Cost.of(0, CLAY, CLAY, GLASS, GLASS, PAPYRUS),
        2,
        List.of(new io.turnbase.games.sevenwondersduel.effect.BuildFromDiscard()),
        false);
    assertWonder(
        "Piraeus", Cost.of(0, WOOD, WOOD, STONE, CLAY), 2, List.of(new GrantPlayAgain()), true);
    assertWonder("The Pyramids", Cost.of(0, STONE, STONE, STONE, PAPYRUS), 9, List.of(), false);
    assertWonder(
        "The Sphinx",
        Cost.of(0, STONE, CLAY, GLASS, GLASS),
        6,
        List.of(new GrantPlayAgain()),
        false);
    assertWonder(
        "The Statue of Zeus",
        Cost.of(0, STONE, WOOD, CLAY, PAPYRUS, PAPYRUS),
        3,
        List.of(new DiscardOpponentCard(Colors.BROWN), new Shields(1)),
        false);
    assertWonder(
        "The Temple of Artemis",
        Cost.of(0, WOOD, STONE, GLASS, PAPYRUS),
        0,
        List.of(new GainCoins(12), new GrantPlayAgain()),
        false);
  }

  @Test
  void theLighthouseAndPiraeusChooseAndDoNotRaiseTheOpponentsPrice() {
    assertThat(wonder("The Great Lighthouse").modifiers())
        .contains(Production.choice(false, WOOD, CLAY, STONE));
    assertThat(wonder("Piraeus").modifiers()).contains(Production.choice(false, GLASS, PAPYRUS));
  }

  private static RuleSet rulesWithStrategy() {
    Fixtures.Card strategy =
        new Fixtures.Card(
            ComponentId.of("strategy"),
            List.of(),
            List.of(new ShieldBonus(Colors.RED, 1)),
            List.of());
    return RuleSet.assemble(
        List.of(
            new BasePack(),
            new BaseContent(),
            new ContentPack() {
              @Override
              public PackId id() {
                return PackId.of("test-strategy");
              }

              @Override
              public List<ZoneKind> zoneKinds() {
                return List.of();
              }

              @Override
              public List<Component> components() {
                return List.of(strategy);
              }
            }));
  }

  @Test
  void appianWayPlaysThroughAndStrategyDoesNotBoostAWonderShield() {
    RuleSet rules = rulesWithStrategy();
    ComponentId zeus = wonder("The Statue of Zeus").id();
    Resolution r =
        new Resolution(
            Fixtures.state(
                rules,
                P1,
                7,
                2,
                ComponentId.of("strategy"),
                Fixtures.city(P1),
                zeus,
                Fixtures.city(P1)),
            rules);

    r.resolveEffects(zeus, P1);

    assertThat(r.state().pawn()).isEqualTo(new Pawn(P2, 1));

    ComponentId appian = wonder("The Appian Way").id();
    Resolution a =
        new Resolution(Fixtures.state(rules, P1, 7, 2, appian, Fixtures.city(P1)), rules);
    a.resolveEffects(appian, P1);

    assertThat(a.state().player(P1).coins()).isEqualTo(10);
    assertThat(a.state().player(P2).coins()).isZero();
    assertThat(a.state().player(P1).playAgain()).isTrue();
  }
}
