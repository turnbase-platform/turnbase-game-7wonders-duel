package io.turnbase.games.sevenwondersduel.turn;

import static io.turnbase.games.sevenwondersduel.turn.TurnFixtures.ageOne;
import static io.turnbase.games.sevenwondersduel.turn.TurnFixtures.takeable;
import static org.assertj.core.api.Assertions.assertThat;

import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.cost.ChainSource;
import io.turnbase.games.sevenwondersduel.cost.ChainSymbol;
import io.turnbase.games.sevenwondersduel.cost.Cost;
import io.turnbase.games.sevenwondersduel.cost.Production;
import io.turnbase.games.sevenwondersduel.cost.Resource;
import io.turnbase.games.sevenwondersduel.cost.TradeRevenue;
import io.turnbase.games.sevenwondersduel.effect.Fixtures;
import io.turnbase.games.sevenwondersduel.engine.Build;
import io.turnbase.games.sevenwondersduel.engine.BuildWonder;
import io.turnbase.games.sevenwondersduel.engine.Engine;
import io.turnbase.games.sevenwondersduel.engine.Outcome;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.setup.SetupFixtures;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.turn.TurnFixtures.Part;
import io.turnbase.games.sevenwondersduel.zone.Location;
import io.turnbase.games.sevenwondersduel.zone.Placements;
import io.turnbase.games.sevenwondersduel.zone.Zones;
import java.util.Set;
import org.junit.jupiter.api.Test;

class EconomyTest {

  private static final long SEED = 7;
  private static final ComponentId CARD =
      takeable(SetupFixtures.startedAgeOne(TurnFixtures.rulesWith(), SEED));
  private static final ChainSymbol HORSESHOE = new ChainSymbol("horseshoe");
  private static final Part ECONOMY = Part.card("economy").withTriggers(new TradeRevenue());
  private static final Part CLAY =
      Part.card("clay").withModifiers(Production.fixed(Resource.CLAY, 1, true));

  /** The other player's clay makes one bought clay cost 3. */
  private record Game(RuleSet rules, DuelState state, PlayerId actor) {
    PlayerId rival() {
      return state.opponentOf(actor);
    }

    Game holding(Part held, PlayerId owner) {
      return new Game(
          rules,
          state.withPlacements(
              Placements.place(state.placements(), held.id(), Fixtures.city(owner), rules.zones())),
          actor);
    }

    int coins(PlayerId player) {
      return state.player(player).coins();
    }

    Outcome build() {
      return new Engine(rules).apply(state, actor, new Build(CARD));
    }
  }

  private static Game game(Part card, Part... others) {
    Part[] parts = new Part[others.length + 3];
    parts[0] = card;
    parts[1] = ECONOMY;
    parts[2] = CLAY;
    System.arraycopy(others, 0, parts, 3, others.length);
    RuleSet rules = TurnFixtures.rulesWith(parts);
    DuelState state = SetupFixtures.startedAgeOne(rules, SEED);
    return new Game(rules, state, state.active());
  }

  /** The actor buys from a rival whose clay raises the price, and the rival owns Economy. */
  private static Game rivalHoldsEconomy(Part card, Part... others) {
    Game g = game(card, others);
    return g.holding(CLAY, g.rival()).holding(ECONOMY, g.rival());
  }

  @Test
  void theHolderReceivesOnlyWhatWasPaidForBoughtResources() {
    Game g = rivalHoldsEconomy(ageOne(CARD).costing(Cost.of(2, Resource.CLAY)));

    Outcome out = g.build();

    assertThat(out.state().player(g.rival()).coins()).isEqualTo(g.coins(g.rival()) + 3);
    assertThat(out.state().player(g.actor()).coins()).isEqualTo(g.coins(g.actor()) - 5);
  }

  @Test
  void printedCoinsGoToTheBankNotTheHolder() {
    Game g = rivalHoldsEconomy(ageOne(CARD).costing(Cost.coins(4)));

    Outcome out = g.build();

    assertThat(out.state().player(g.rival()).coins()).isEqualTo(g.coins(g.rival()));
    assertThat(out.state().player(g.actor()).coins()).isEqualTo(g.coins(g.actor()) - 4);
  }

  @Test
  void aChainedBuildPaysNobody() {
    Part source = Part.card("stable").withModifiers(new ChainSource(Set.of(HORSESHOE)));
    Game g =
        rivalHoldsEconomy(
                ageOne(CARD).costing(Cost.of(2, Resource.CLAY).withFreedBy(HORSESHOE)), source)
            .holding(source, game(ageOne(CARD)).actor());

    Outcome out = g.build();

    assertThat(out.state().player(g.rival()).coins()).isEqualTo(g.coins(g.rival()));
    assertThat(out.state().player(g.actor()).coins()).isEqualTo(g.coins(g.actor()));
  }

  @Test
  void theHolderBuyingPaysTheBankNotThemselves() {
    Game base = game(ageOne(CARD).costing(Cost.of(0, Resource.CLAY)));
    Game g = base.holding(CLAY, base.rival()).holding(ECONOMY, base.actor());

    Outcome out = g.build();

    assertThat(out.state().player(g.actor()).coins()).isEqualTo(g.coins(g.actor()) - 3);
    assertThat(out.state().player(g.rival()).coins()).isEqualTo(g.coins(g.rival()));
  }

  @Test
  void aWonderPurchaseAlsoPaysTheHolder() {
    DuelState seen = SetupFixtures.startedAgeOne(TurnFixtures.rulesWith(), SEED);
    PlayerId actor = seen.active();
    ComponentId wonder =
        Placements.idsIn(
                seen.placements(), new Location(Zones.WONDERS_UNBUILT, actor.value().toString()))
            .get(0);
    Game g =
        rivalHoldsEconomy(
            ageOne(CARD), Part.wonder(wonder.value()).costing(Cost.of(0, Resource.CLAY)));

    DuelState after =
        new Engine(g.rules()).apply(g.state(), actor, new BuildWonder(CARD, wonder)).state();

    assertThat(after.player(g.rival()).coins()).isEqualTo(g.coins(g.rival()) + 3);
    assertThat(after.player(actor).coins()).isEqualTo(g.coins(actor) - 3);
  }
}
