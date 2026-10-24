package io.turnbase.games.sevenwondersduel.age;

import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P1;
import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.engine.ChooseStarter;
import io.turnbase.games.sevenwondersduel.engine.Discard;
import io.turnbase.games.sevenwondersduel.engine.Engine;
import io.turnbase.games.sevenwondersduel.engine.IllegalActionException;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.setup.SetupFixtures;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.state.Pawn;
import io.turnbase.games.sevenwondersduel.state.Phase;
import io.turnbase.games.sevenwondersduel.structure.Structure;
import io.turnbase.games.sevenwondersduel.zone.Placements;
import org.junit.jupiter.api.Test;

class AgeEndTest {

  private static final RuleSet RULES = SetupFixtures.baseSized();
  private static final Engine ENGINE = new Engine(RULES);

  private static int cardsLeft(DuelState state) {
    return Placements.idsIn(state.placements(), Structure.UP).size()
        + Placements.idsIn(state.placements(), Structure.DOWN).size();
  }

  private static DuelState discardOne(DuelState state) {
    return ENGINE
        .apply(state, state.active(), new Discard(Structure.takeable(state.placements()).get(0)))
        .state();
  }

  /** Discards until {@code remaining} cards are left in the structure. */
  private static DuelState discardDownTo(DuelState state, int remaining) {
    while (cardsLeft(state) > remaining) {
      state = discardOne(state);
    }
    return state;
  }

  private static DuelState playOutAge(DuelState state) {
    return discardOne(discardDownTo(state, 1));
  }

  private static DuelState started() {
    return SetupFixtures.startedAgeOne(RULES, 42);
  }

  @Test
  void whenTheStructureEmptiesTheNextAgeIsDealtAndTheStarterChoiceIsOpen() {
    DuelState state = discardDownTo(started(), 1);

    DuelState next = discardOne(state);

    assertThat(next.phase()).isEqualTo(Phase.AGE);
    assertThat(next.age()).isEqualTo(2);
    assertThat(cardsLeft(next)).isEqualTo(20);
    assertThat(next.deciding()).isNotNull();
  }

  @Test
  void withThePawnAtTheCentreTheLastPlayerToActDecides() {
    DuelState state = discardDownTo(started(), 1);
    PlayerId lastToAct = state.active();

    DuelState next = discardOne(state);

    assertThat(next.deciding()).isEqualTo(lastToAct);
    assertThat(next.active()).isEqualTo(lastToAct);
  }

  @Test
  void withThePawnOffCentreTheWeakerSideDecidesWhoeverTookTheLastCard() {
    for (PlayerId weaker : new PlayerId[] {P1, P2}) {
      DuelState state = discardDownTo(started().withPawn(new Pawn(weaker, 3)), 1);

      DuelState next = discardOne(state);

      assertThat(next.deciding()).isEqualTo(weaker);
      assertThat(next.active()).isEqualTo(weaker);
    }
  }

  @Test
  void aPlayAgainEarnedOnTheLastTurnIsLostForBothPlayers() {
    DuelState state = discardDownTo(started(), 1);
    DuelState flagged =
        state
            .withPlayer(P1, state.player(P1).withPlayAgain(true))
            .withPlayer(P2, state.player(P2).withPlayAgain(true));

    DuelState next = discardOne(flagged);

    assertThat(next.player(P1).playAgain()).isFalse();
    assertThat(next.player(P2).playAgain()).isFalse();
    assertThat(next.age()).isEqualTo(2);
  }

  @Test
  void theNextAgeCannotBeTakenFromUntilTheStarterIsChosen() {
    DuelState next = discardOne(discardDownTo(started(), 1));

    assertThatThrownBy(() -> discardOne(next)).isInstanceOf(IllegalActionException.class);
    DuelState playing =
        ENGINE
            .apply(next, next.deciding(), new ChooseStarter(next.opponentOf(next.deciding())))
            .state();

    assertThat(playing.active()).isEqualTo(next.opponentOf(next.deciding()));
    assertThat(discardOne(playing).age()).isEqualTo(2);
  }

  @Test
  void endingTheLastAgeMovesToScoringWithNothingLeftToDo() {
    DuelState state = playOutAge(started());
    state = ENGINE.apply(state, state.deciding(), new ChooseStarter(state.deciding())).state();
    state = playOutAge(state);
    state = ENGINE.apply(state, state.deciding(), new ChooseStarter(state.deciding())).state();
    DuelState last = discardDownTo(state, 1);

    DuelState scoring = discardOne(last);

    assertThat(scoring.phase()).isEqualTo(Phase.SCORING);
    assertThat(scoring.age()).isEqualTo(3);
    assertThat(scoring.deciding()).isNull();
    assertThat(cardsLeft(scoring)).isZero();
    assertThatThrownBy(
            () ->
                ENGINE.apply(
                    scoring, scoring.active(), new Discard(last.placements().get(0).component())))
        .isInstanceOf(IllegalActionException.class);
  }
}
