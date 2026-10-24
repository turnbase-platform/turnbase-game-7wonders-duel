package io.turnbase.games.sevenwondersduel.view;

import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P1;
import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P2;
import static io.turnbase.games.sevenwondersduel.turn.TurnFixtures.ageOne;
import static io.turnbase.games.sevenwondersduel.turn.TurnFixtures.takeable;
import static org.assertj.core.api.Assertions.assertThat;

import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.cost.ChainSource;
import io.turnbase.games.sevenwondersduel.cost.ChainSymbol;
import io.turnbase.games.sevenwondersduel.cost.Cost;
import io.turnbase.games.sevenwondersduel.cost.CostSolver;
import io.turnbase.games.sevenwondersduel.cost.Production;
import io.turnbase.games.sevenwondersduel.cost.Resource;
import io.turnbase.games.sevenwondersduel.effect.Fixtures;
import io.turnbase.games.sevenwondersduel.engine.Build;
import io.turnbase.games.sevenwondersduel.engine.Engine;
import io.turnbase.games.sevenwondersduel.engine.IllegalActionException;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.setup.Setup;
import io.turnbase.games.sevenwondersduel.setup.SetupFixtures;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.state.PlayerState;
import io.turnbase.games.sevenwondersduel.structure.Structure;
import io.turnbase.games.sevenwondersduel.turn.Construct;
import io.turnbase.games.sevenwondersduel.turn.TurnFixtures;
import io.turnbase.games.sevenwondersduel.turn.TurnFixtures.Part;
import io.turnbase.games.sevenwondersduel.zone.Placements;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class QuotesTest {

  private static final long SEED = 7;
  private static final ComponentId CARD =
      takeable(SetupFixtures.startedAgeOne(TurnFixtures.rulesWith(), SEED));
  private static final ChainSymbol HORSESHOE = new ChainSymbol("horseshoe");
  private static final Part CLAY =
      Part.card("clay").withModifiers(Production.fixed(Resource.CLAY, 1, true));

  private static DuelState started(RuleSet rules) {
    return SetupFixtures.startedAgeOne(rules, SEED);
  }

  private static List<QuoteView> quotes(DuelState state, RuleSet rules) {
    return Views.of(state, rules, new Viewer.Spectator()).quotes();
  }

  private static QuoteView quote(List<QuoteView> all, PlayerId player, ComponentId component) {
    return all.stream()
        .filter(q -> q.player().equals(player.value()) && q.component().equals(component.value()))
        .findFirst()
        .orElseThrow();
  }

  private static DuelState withCoins(DuelState state, PlayerId player, int coins) {
    return state.withPlayer(player, new PlayerState(coins, false));
  }

  private static DuelState holding(RuleSet rules, DuelState state, Part held, PlayerId owner) {
    return state.withPlacements(
        Placements.place(state.placements(), held.id(), Fixtures.city(owner), rules.zones()));
  }

  @Test
  void bothPlayersAreQuotedEveryTakeableCardAndTheirOwnUnbuiltWonders() {
    RuleSet rules = TurnFixtures.rulesWith();
    DuelState state = started(rules);

    List<QuoteView> all = quotes(state, rules);

    int cards = Structure.takeable(state.placements()).size();
    for (PlayerId player : List.of(P1, P2)) {
      int wonders = Placements.idsIn(state.placements(), Construct.unbuilt(player)).size();
      assertThat(all.stream().filter(q -> q.player().equals(player.value())))
          .hasSize(cards + wonders);
    }
    assertThat(quote(all, P2, CARD)).isNotNull();
  }

  @Test
  void coveredAndFaceDownCardsAreNotQuoted() {
    RuleSet rules = TurnFixtures.rulesWith();
    DuelState state = started(rules);
    List<String> takeable =
        Structure.takeable(state.placements()).stream().map(ComponentId::value).toList();
    List<String> wonders =
        List.of(P1, P2).stream()
            .flatMap(p -> Placements.idsIn(state.placements(), Construct.unbuilt(p)).stream())
            .map(ComponentId::value)
            .toList();

    assertThat(quotes(state, rules))
        .allMatch(q -> takeable.contains(q.component()) || wonders.contains(q.component()));
  }

  @Test
  void aChainedCardQuotesNothingAndSaysSo() {
    Part source = Part.card("stable").withModifiers(new ChainSource(Set.of(HORSESHOE)));
    RuleSet rules =
        TurnFixtures.rulesWith(
            ageOne(CARD).costing(Cost.of(2, Resource.CLAY).withFreedBy(HORSESHOE)), source);
    DuelState state = holding(rules, started(rules), source, P2);

    List<QuoteView> all = quotes(state, rules);

    assertThat(quote(all, P2, CARD).chained()).isTrue();
    assertThat(quote(all, P2, CARD).total()).isZero();
    assertThat(quote(all, P1, CARD).chained()).isFalse();
  }

  @Test
  void theTradeCostIsWhatTheSolverQuotes() {
    RuleSet rules = TurnFixtures.rulesWith(ageOne(CARD).costing(Cost.of(2, Resource.CLAY)), CLAY);
    DuelState state = holding(rules, started(rules), CLAY, P2);
    var expected = new CostSolver(rules).quote(state, P1, CARD);

    QuoteView view = quote(quotes(state, rules), P1, CARD);

    assertThat(view.tradeCost()).isEqualTo(expected.tradeCost()).isPositive();
    assertThat(view.coins()).isEqualTo(expected.coins());
    assertThat(view.total()).isEqualTo(expected.total());
  }

  @Test
  void affordableAgreesWithWhetherTheEngineAcceptsTheBuild() {
    RuleSet rules = TurnFixtures.rulesWith(ageOne(CARD).costing(Cost.of(2, Resource.CLAY)), CLAY);
    DuelState rich = holding(rules, started(rules), CLAY, P2);
    int price = quote(quotes(rich, rules), P1, CARD).total();

    for (int coins : List.of(price - 1, price, price + 1)) {
      for (PlayerId player : List.of(P1, P2)) {
        DuelState state = withCoins(rich, player, coins).withActive(player);
        boolean affordable = quote(quotes(state, rules), player, CARD).affordable();

        assertThat(affordable).isEqualTo(accepts(rules, state, player));
      }
    }
  }

  @Test
  void noQuotesOutsideAnAge() {
    RuleSet rules = SetupFixtures.baseSized();
    DuelState state = Setup.newGame(rules, SEED, P1, P2);

    assertThat(quotes(state, rules)).isEmpty();
  }

  private static boolean accepts(RuleSet rules, DuelState state, PlayerId player) {
    try {
      new Engine(rules).apply(state, player, new Build(CARD));
      return true;
    } catch (IllegalActionException e) {
      return false;
    }
  }
}
