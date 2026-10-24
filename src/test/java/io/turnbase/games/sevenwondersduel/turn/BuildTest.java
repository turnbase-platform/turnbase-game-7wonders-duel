package io.turnbase.games.sevenwondersduel.turn;

import static io.turnbase.games.sevenwondersduel.turn.TurnFixtures.ageOne;
import static io.turnbase.games.sevenwondersduel.turn.TurnFixtures.takeable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.turnbase.games.sevenwondersduel.agenda.ComponentOption;
import io.turnbase.games.sevenwondersduel.agenda.DecisionStep;
import io.turnbase.games.sevenwondersduel.agenda.TurnEndStep;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.cost.ChainSource;
import io.turnbase.games.sevenwondersduel.cost.ChainSymbol;
import io.turnbase.games.sevenwondersduel.cost.Cost;
import io.turnbase.games.sevenwondersduel.cost.Production;
import io.turnbase.games.sevenwondersduel.cost.Resource;
import io.turnbase.games.sevenwondersduel.effect.Fixtures;
import io.turnbase.games.sevenwondersduel.effect.GainCoins;
import io.turnbase.games.sevenwondersduel.effect.GrantPlayAgain;
import io.turnbase.games.sevenwondersduel.engine.Answer;
import io.turnbase.games.sevenwondersduel.engine.Build;
import io.turnbase.games.sevenwondersduel.engine.Engine;
import io.turnbase.games.sevenwondersduel.engine.IllegalActionException;
import io.turnbase.games.sevenwondersduel.engine.Outcome;
import io.turnbase.games.sevenwondersduel.event.CoinsChanged;
import io.turnbase.games.sevenwondersduel.event.ComponentMoved;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.id.ZoneId;
import io.turnbase.games.sevenwondersduel.setup.SetupFixtures;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.turn.TurnFixtures.Part;
import io.turnbase.games.sevenwondersduel.zone.Placements;
import java.util.List;
import org.junit.jupiter.api.Test;

class BuildTest {

  private static final long SEED = 7;
  private static final ComponentId CARD =
      takeable(SetupFixtures.startedAgeOne(TurnFixtures.rulesWith(), SEED));
  private static final ChainSymbol HORSESHOE = new ChainSymbol("horseshoe");

  private record Game(RuleSet rules, DuelState state, PlayerId actor) {
    Outcome build() {
      return new Engine(rules).apply(state, actor, new Build(CARD));
    }

    Game holding(Part held, PlayerId owner) {
      return new Game(
          rules,
          state.withPlacements(
              Placements.place(state.placements(), held.id(), Fixtures.city(owner), rules.zones())),
          actor);
    }
  }

  private static Game game(Part card, Part... others) {
    Part[] parts = new Part[others.length + 1];
    parts[0] = card;
    System.arraycopy(others, 0, parts, 1, others.length);
    RuleSet rules = TurnFixtures.rulesWith(parts);
    DuelState state = SetupFixtures.startedAgeOne(rules, SEED);
    return new Game(rules, state, state.active());
  }

  private static Part card() {
    return ageOne(CARD);
  }

  @Test
  void aFreeCardGoesToTheCityAndTheTurnPasses() {
    Game g = game(card());

    Outcome out = g.build();

    assertThat(out.state().placements())
        .contains(
            new io.turnbase.games.sevenwondersduel.zone.Placement(CARD, Fixtures.city(g.actor())));
    assertThat(out.state().active()).isEqualTo(g.state().opponentOf(g.actor()));
    assertThat(out.state().player(g.actor()).coins())
        .isEqualTo(g.state().player(g.actor()).coins());
  }

  @Test
  void aCoinCostGoesToTheBank() {
    Game g = game(card().costing(Cost.coins(3)));

    Outcome out = g.build();

    assertThat(out.state().player(g.actor()).coins())
        .isEqualTo(g.state().player(g.actor()).coins() - 3);
    assertThat(out.events()).anyMatch(e -> e.event().equals(new CoinsChanged(g.actor(), -3)));
  }

  @Test
  void tradeIsPaidAtThePriceTheOpponentRaised() {
    Part clay = Part.card("opp-clay").withModifiers(Production.fixed(Resource.CLAY, 1, true));
    Game g = game(card().costing(Cost.of(0, Resource.CLAY)), clay);
    g = g.holding(clay, g.state().opponentOf(g.actor()));

    Outcome out = g.build();

    assertThat(out.state().player(g.actor()).coins())
        .isEqualTo(g.state().player(g.actor()).coins() - 3);
  }

  @Test
  void aChainedCardCostsNothing() {
    Part source = Part.card("stable").withModifiers(new ChainSource(java.util.Set.of(HORSESHOE)));
    Game g = game(card().costing(Cost.coins(5).withFreedBy(HORSESHOE)), source);
    g = g.holding(source, g.actor());

    assertThat(g.build().state().player(g.actor()).coins())
        .isEqualTo(g.state().player(g.actor()).coins());
  }

  @Test
  void anUnaffordableCardIsRejected() {
    Game g = game(card().costing(Cost.coins(g0Coins() + 1)));

    assertThatThrownBy(g::build)
        .isInstanceOf(IllegalActionException.class)
        .hasMessageContaining("cannot afford");
  }

  private static int g0Coins() {
    return SetupFixtures.startedAgeOne(TurnFixtures.rulesWith(), SEED)
        .player(SetupFixtures.startedAgeOne(TurnFixtures.rulesWith(), SEED).active())
        .coins();
  }

  @Test
  void onlyTheActivePlayerMayBuild() {
    Game g = game(card());

    assertThatThrownBy(
            () ->
                new Engine(g.rules())
                    .apply(g.state(), g.state().opponentOf(g.actor()), new Build(CARD)))
        .isInstanceOf(IllegalActionException.class)
        .hasMessageContaining("not your turn");
  }

  @Test
  void aCardOutsideTheStructureCannotBeBuilt() {
    Game g = game(card());
    ComponentId buried = Placements.idsIn(g.state().placements(), ZoneId.of("deck")).get(0);

    assertThatThrownBy(() -> new Engine(g.rules()).apply(g.state(), g.actor(), new Build(buried)))
        .isInstanceOf(IllegalActionException.class)
        .hasMessageContaining("not available");
  }

  @Test
  void effectsResolveAfterTheCardIsInTheCity() {
    Game g = game(card().withEffects(new GainCoins(2)));

    List<Object> kinds =
        g.build().events().stream().<Object>map(e -> e.event().getClass()).toList();

    assertThat(kinds.indexOf(ComponentMoved.class)).isLessThan(kinds.indexOf(CoinsChanged.class));
    assertThat(g.build().state().player(g.actor()).coins())
        .isEqualTo(g.state().player(g.actor()).coins() + 2);
  }

  @Test
  void aDecisionKeepsTheTurnOpenUntilItIsAnswered() {
    Game g = game(card().withEffects(new Fixtures.Choose()));

    Outcome opened = g.build();

    assertThat(opened.state().active()).isEqualTo(g.actor());
    assertThat(opened.state().agenda().top()).isInstanceOf(DecisionStep.class);
    assertThat(opened.state().agenda().steps()).last().isEqualTo(new TurnEndStep());

    DecisionStep decision = (DecisionStep) opened.state().agenda().top();
    Outcome done =
        new Engine(g.rules())
            .apply(opened.state(), g.actor(), new Answer(decision.options().get(0)));

    assertThat(done.state().agenda().isEmpty()).isTrue();
    assertThat(done.state().active()).isEqualTo(g.state().opponentOf(g.actor()));
    assertThat(decision.options().get(0)).isInstanceOf(ComponentOption.class);
  }

  @Test
  void aPlayAgainFromTheCardIsHonoured() {
    Game g = game(card().withEffects(new GrantPlayAgain()));

    Outcome out = g.build();

    assertThat(out.state().active()).isEqualTo(g.actor());
    assertThat(out.state().player(g.actor()).playAgain()).isFalse();
  }

  @Test
  void aPlayAgainOnTheTurnThatEmptiesTheStructureIsLost() {
    ComponentId last = TurnFixtures.lastCard(SEED);
    RuleSet rules = TurnFixtures.rulesWith(ageOne(last).withEffects(new GrantPlayAgain()));
    Engine engine = new Engine(rules);
    DuelState state = SetupFixtures.startedAgeOne(rules, SEED);
    while (TurnFixtures.inStructure(state) > 1) {
      state =
          engine
              .apply(
                  state,
                  state.active(),
                  new io.turnbase.games.sevenwondersduel.engine.Discard(takeable(state)))
              .state();
    }

    DuelState after = engine.apply(state, state.active(), new Build(last)).state();

    assertThat(after.player(state.active()).playAgain()).isFalse();
    assertThat(after.player(state.opponentOf(state.active())).playAgain()).isFalse();
  }
}
