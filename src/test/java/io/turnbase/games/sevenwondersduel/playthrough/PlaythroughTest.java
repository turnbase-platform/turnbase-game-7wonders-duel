package io.turnbase.games.sevenwondersduel.playthrough;

import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P1;
import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.turnbase.games.sevenwondersduel.catalog.BasePack;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.content.BaseContent;
import io.turnbase.games.sevenwondersduel.draft.Draft;
import io.turnbase.games.sevenwondersduel.engine.Answer;
import io.turnbase.games.sevenwondersduel.engine.Build;
import io.turnbase.games.sevenwondersduel.engine.BuildWonder;
import io.turnbase.games.sevenwondersduel.engine.ChooseStarter;
import io.turnbase.games.sevenwondersduel.engine.Discard;
import io.turnbase.games.sevenwondersduel.engine.DraftWonder;
import io.turnbase.games.sevenwondersduel.engine.Engine;
import io.turnbase.games.sevenwondersduel.engine.PlayerAction;
import io.turnbase.games.sevenwondersduel.setup.Setup;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.state.Phase;
import io.turnbase.games.sevenwondersduel.structure.Structure;
import java.util.List;
import org.junit.jupiter.api.Test;

class PlaythroughTest {

  private static final RuleSet RULES = RuleSet.assemble(List.of(new BasePack(), new BaseContent()));

  private static DuelState game(long seed) {
    return Setup.newGame(RULES, seed, P1, P2);
  }

  @Test
  void theDraftListsTheOfferedWondersForThePlayerToMove() {
    DuelState state = game(1);

    Bot.Move move = Bot.legalActions(RULES, state);

    assertThat(move.actor()).isEqualTo(Draft.toMove(state).orElseThrow());
    assertThat(move.actions())
        .hasSize(4)
        .allSatisfy(a -> assertThat(a).isInstanceOf(DraftWonder.class));
  }

  @Test
  void everyListedActionIsAcceptedAlongTheWholeGame() {
    Engine engine = new Engine(RULES);
    DuelState state = game(2);
    int steps = 0;
    while (state.phase() != Phase.OVER && steps++ < 400) {
      Bot.Move move = Bot.legalActions(RULES, state);
      for (PlayerAction action : move.actions()) {
        engine.apply(state, move.actor(), action);
      }
      state = engine.apply(state, move.actor(), move.actions().get(0)).state();
    }
    assertThat(state.phase()).isEqualTo(Phase.OVER);
  }

  @Test
  void inAnAgeEveryTakeableCardCanBeDiscardedAndBuiltCardsAreAffordable() {
    DuelState state =
        Playthrough.play(RULES, 3, Policy.RANDOM, 400).states().stream()
            .skip(30)
            .filter(s -> s.phase() == Phase.AGE && s.deciding() == null && s.agenda().isEmpty())
            .findFirst()
            .orElseThrow();
    Bot.Move move = Bot.legalActions(RULES, state);

    List<?> discards = move.actions().stream().filter(Discard.class::isInstance).toList();

    assertThat(state.phase()).isEqualTo(Phase.AGE);
    assertThat(discards).hasSameSizeAs(Structure.takeable(state.placements()));
    assertThat(move.actions()).allSatisfy(a -> assertThat(a).isNotInstanceOf(Answer.class));
    assertThat(move.actions().stream().filter(ChooseStarter.class::isInstance)).isEmpty();
    assertThat(
            move.actions().stream().anyMatch(Build.class::isInstance)
                || move.actions().stream().anyMatch(BuildWonder.class::isInstance)
                || !discards.isEmpty())
        .isTrue();
  }

  @Test
  void aSeedAndPolicyFixTheGame() {
    assertThat(Playthrough.play(RULES, 5, Policy.RANDOM, 400).finalState())
        .isEqualTo(Playthrough.play(RULES, 5, Policy.RANDOM, 400).finalState());
  }

  @Test
  void aGameThatDoesNotEndWithinTheCapIsReportedWithSeedAndPolicy() {
    assertThatThrownBy(() -> Playthrough.play(RULES, 7, Policy.WARMONGER, 3).finalState())
        .isInstanceOf(AssertionError.class)
        .hasMessageContaining("seed 7")
        .hasMessageContaining("WARMONGER");
  }
}
