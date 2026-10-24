package io.turnbase.games.sevenwondersduel.turn;

import static io.turnbase.games.sevenwondersduel.turn.TurnFixtures.takeable;
import static org.assertj.core.api.Assertions.assertThat;

import io.turnbase.games.sevenwondersduel.agenda.ComponentOption;
import io.turnbase.games.sevenwondersduel.agenda.DecisionStep;
import io.turnbase.games.sevenwondersduel.catalog.ComponentType;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.catalog.Tag;
import io.turnbase.games.sevenwondersduel.effect.Fixtures;
import io.turnbase.games.sevenwondersduel.effect.GainCoins;
import io.turnbase.games.sevenwondersduel.effect.Modifier;
import io.turnbase.games.sevenwondersduel.effect.ScienceCheck;
import io.turnbase.games.sevenwondersduel.engine.Answer;
import io.turnbase.games.sevenwondersduel.engine.Build;
import io.turnbase.games.sevenwondersduel.engine.Engine;
import io.turnbase.games.sevenwondersduel.engine.Outcome;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.science.ScienceSymbol;
import io.turnbase.games.sevenwondersduel.science.Symbol;
import io.turnbase.games.sevenwondersduel.setup.SetupFixtures;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.state.Phase;
import io.turnbase.games.sevenwondersduel.state.Victory;
import io.turnbase.games.sevenwondersduel.turn.TurnFixtures.Part;
import io.turnbase.games.sevenwondersduel.zone.Placements;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

class ScienceTest {

  private static final long SEED = 7;

  private static Modifier symbol(String id) {
    return new Symbol(new ScienceSymbol(id));
  }

  private static Part green(String id, String... symbols) {
    return Part.card(id)
        .withModifiers(
            java.util.Arrays.stream(symbols).map(ScienceTest::symbol).toArray(Modifier[]::new))
        .withEffects(new ScienceCheck());
  }

  private static final Part PAY =
      Part.of("pay", ComponentType.TOKEN).tagged(Tag.PROGRESS).withEffects(new GainCoins(6));
  private static final Part LAW =
      Part.of("law", ComponentType.TOKEN)
          .tagged(Tag.PROGRESS)
          .withModifiers(symbol("scales"))
          .withEffects(new ScienceCheck());

  private record Game(RuleSet rules, DuelState state, PlayerId actor, ComponentId card) {
    Outcome build() {
      return new Engine(rules).apply(state, actor, new Build(card));
    }

    Outcome answer(Outcome opened, Part token) {
      return new Engine(rules)
          .apply(opened.state(), actor, new Answer(new ComponentOption(token.id())));
    }
  }

  /**
   * The actor owns {@code held}; {@code tokens} lie on the board; the first card the actor can take
   * becomes {@code built} (made from its id), free to construct.
   */
  private static Game game(Function<String, Part> built, List<Part> held, Part... tokens) {
    List<Part> parts = new ArrayList<>(held);
    parts.addAll(List.of(tokens));
    ComponentId card =
        takeable(
            SetupFixtures.startedAgeOne(TurnFixtures.rulesWith(parts.toArray(Part[]::new)), SEED));
    parts.add(built.apply(card.value()).tagged(Tag.age(1)));
    RuleSet rules = TurnFixtures.rulesWith(parts.toArray(Part[]::new));
    DuelState state = SetupFixtures.startedAgeOne(rules, SEED);
    for (Part p : held) {
      state =
          state.withPlacements(
              Placements.place(
                  state.placements(), p.id(), Fixtures.city(state.active()), rules.zones()));
    }
    for (Part t : tokens) {
      state =
          state.withPlacements(
              Placements.move(state.placements(), t.id(), Fixtures.zone("board"), rules.zones()));
    }
    return new Game(rules, state, state.active(), card);
  }

  private static Part wheel(String id) {
    return green(id, "wheel");
  }

  @Test
  void aPairLeavesADecisionForTheBuilderAndTheTurnWaits() {
    Game g = game(ScienceTest::wheel, List.of(wheel("held")), PAY);

    Outcome opened = g.build();

    assertThat(opened.state().agenda().top()).isInstanceOf(DecisionStep.class);
    assertThat(opened.state().active()).isEqualTo(g.actor());
  }

  @Test
  void answeringTakesTheTokenAppliesItAndPassesTheTurn() {
    Game g = game(ScienceTest::wheel, List.of(wheel("held")), PAY);
    int coins = g.state().player(g.actor()).coins();

    Outcome done = g.answer(g.build(), PAY);

    assertThat(done.state().player(g.actor()).coins()).isEqualTo(coins + 6);
    assertThat(Placements.locationOf(done.state().placements(), PAY.id()))
        .isEqualTo(Fixtures.city(g.actor()));
    assertThat(done.state().agenda().isEmpty()).isTrue();
    assertThat(done.state().active()).isEqualTo(g.state().opponentOf(g.actor()));
  }

  @Test
  void theSixthDifferentSymbolEndsTheGame() {
    List<Part> held =
        List.of(
            green("a", "wheel"),
            green("b", "scales"),
            green("c", "pen"),
            green("d", "tablet"),
            green("e", "mortar"));
    Game g = game(id -> green(id, "sphere"), held, PAY);

    DuelState after = g.build().state();

    assertThat(after.phase()).isEqualTo(Phase.OVER);
    assertThat(after.victory()).isEqualTo(new Victory(g.actor(), Victory.Reason.SCIENCE));
    assertThat(after.active()).isEqualTo(g.actor());
  }

  @Test
  void takingLawCanOpenASecondDecisionBeforeTheTurnEnds() {
    Game g = game(ScienceTest::wheel, List.of(wheel("held"), green("s", "scales")), LAW, PAY);

    Outcome second = g.answer(g.build(), LAW);

    DecisionStep decision = (DecisionStep) second.state().agenda().top();
    assertThat(decision.options())
        .contains(new ComponentOption(PAY.id()))
        .doesNotContain(new ComponentOption(LAW.id()));
    assertThat(second.state().active()).isEqualTo(g.actor());
  }

  @Test
  void lawGivesTheSixthSymbolAndWinsTheGame() {
    Game g =
        game(
            id -> green(id, "a"), List.of(green("h", "a", "b", "c", "d"), wheel("held")), LAW, PAY);

    Outcome done = g.answer(g.build(), LAW);

    assertThat(done.state().phase()).isEqualTo(Phase.OVER);
    assertThat(done.state().victory()).isEqualTo(new Victory(g.actor(), Victory.Reason.SCIENCE));
  }
}
