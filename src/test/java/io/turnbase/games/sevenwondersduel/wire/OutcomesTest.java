package io.turnbase.games.sevenwondersduel.wire;

import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P1;
import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.effect.Fixtures;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.state.Phase;
import io.turnbase.games.sevenwondersduel.state.Victory;
import io.turnbase.gdk.model.GameOutcome;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OutcomesTest {

  private static final UUID T1 = UUID.nameUUIDFromBytes("t1".getBytes());
  private static final UUID T2 = UUID.nameUUIDFromBytes("t2".getBytes());
  private static final Map<UUID, UUID> TEAMS = Map.of(P1.value(), T1, P2.value(), T2);
  private static final RuleSet RULES = Fixtures.rules();

  private static DuelState over(Victory victory) {
    return Fixtures.state(RULES, P1, 7, 7).withPhase(Phase.OVER).withVictory(victory);
  }

  private static int placement(GameOutcome outcome, UUID team) {
    return outcome.teamResults().stream()
        .filter(r -> r.teamId().equals(team))
        .findFirst()
        .orElseThrow()
        .placement();
  }

  @Test
  void aDecisiveWinTakesFirstPlaceAndTheLoserSecond() {
    GameOutcome outcome = Outcomes.of(over(new Victory(P2, Victory.Reason.MILITARY)), RULES, TEAMS);

    assertThat(outcome.draw()).isFalse();
    assertThat(placement(outcome, T2)).isEqualTo(1);
    assertThat(placement(outcome, T1)).isEqualTo(2);
  }

  @Test
  void aSharedVictoryIsADrawWithBothFirst() {
    GameOutcome outcome = Outcomes.of(over(new Victory(null, Victory.Reason.POINTS)), RULES, TEAMS);

    assertThat(outcome.draw()).isTrue();
    assertThat(placement(outcome, T1)).isEqualTo(1);
    assertThat(placement(outcome, T2)).isEqualTo(1);
  }

  @Test
  void eachTeamCarriesTheReasonAndTheScorecard() {
    GameOutcome outcome = Outcomes.of(over(new Victory(P1, Victory.Reason.SCIENCE)), RULES, TEAMS);

    Map<String, String> stats = outcome.teamResults().get(0).stats();
    assertThat(stats)
        .containsEntry("reason", "SCIENCE")
        .containsKeys(
            "blue",
            "green",
            "yellow",
            "purple",
            "wonders",
            "progress",
            "coins",
            "military",
            "total");
    assertThat(outcome.playerResults()).hasSize(2);
  }

  @Test
  void aGameThatIsNotOverHasNoOutcome() {
    assertThatThrownBy(() -> Outcomes.of(Fixtures.state(RULES, P1, 7, 7), RULES, TEAMS))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void aForfeitNeedsAWinner() {
    assertThatThrownBy(() -> new Victory(null, Victory.Reason.FORFEIT))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
