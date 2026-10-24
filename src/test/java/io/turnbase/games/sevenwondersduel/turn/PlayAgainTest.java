package io.turnbase.games.sevenwondersduel.turn;

import static org.assertj.core.api.Assertions.assertThat;

import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.engine.Discard;
import io.turnbase.games.sevenwondersduel.engine.Engine;
import io.turnbase.games.sevenwondersduel.engine.Outcome;
import io.turnbase.games.sevenwondersduel.event.PlayAgainChanged;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.setup.SetupFixtures;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.structure.Structure;
import org.junit.jupiter.api.Test;

class PlayAgainTest {

  private static final RuleSet RULES = SetupFixtures.baseSized();
  private static final Engine ENGINE = new Engine(RULES);

  private static DuelState started() {
    return SetupFixtures.startedAgeOne(RULES, 42);
  }

  private static DuelState flagged(DuelState state, PlayerId player) {
    return state.withPlayer(player, state.player(player).withPlayAgain(true));
  }

  private static Outcome take(DuelState state) {
    return ENGINE.apply(
        state, state.active(), new Discard(Structure.takeable(state.placements()).get(0)));
  }

  @Test
  void theActivePlayerWithTheFlagMovesAgainAndTheFlagIsUsedUp() {
    DuelState state = flagged(started(), started().active());

    Outcome outcome = take(state);

    assertThat(outcome.state().active()).isEqualTo(state.active());
    assertThat(outcome.state().player(state.active()).playAgain()).isFalse();
    assertThat(outcome.events())
        .anyMatch(e -> e.event().equals(new PlayAgainChanged(state.active(), false)));
  }

  @Test
  void theSecondTurnPassesToTheOpponentAsUsual() {
    DuelState state = flagged(started(), started().active());

    DuelState second = take(take(state).state()).state();

    assertThat(second.active()).isEqualTo(state.opponentOf(state.active()));
  }

  @Test
  void aFlagOnTheOpponentWaitsForTheirOwnTurn() {
    DuelState state = started();
    PlayerId opponent = state.opponentOf(state.active());
    DuelState flagged = flagged(state, opponent);

    DuelState mine = take(flagged).state();
    assertThat(mine.active()).isEqualTo(opponent);
    assertThat(mine.player(opponent).playAgain()).isTrue();

    DuelState theirs = take(mine).state();
    assertThat(theirs.active()).isEqualTo(opponent);
    assertThat(theirs.player(opponent).playAgain()).isFalse();
  }

  @Test
  void withoutAFlagTheTurnAlternates() {
    DuelState state = started();

    assertThat(take(state).state().active()).isEqualTo(state.opponentOf(state.active()));
  }
}
