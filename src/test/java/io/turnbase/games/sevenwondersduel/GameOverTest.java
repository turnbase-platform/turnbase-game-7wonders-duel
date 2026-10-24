package io.turnbase.games.sevenwondersduel;

import static io.turnbase.games.sevenwondersduel.GdkDoubles.ONE;
import static io.turnbase.games.sevenwondersduel.GdkDoubles.TWO;
import static io.turnbase.games.sevenwondersduel.GdkDoubles.action;
import static io.turnbase.games.sevenwondersduel.GdkDoubles.left;
import static io.turnbase.games.sevenwondersduel.turn.TurnFixtures.ageOne;
import static io.turnbase.games.sevenwondersduel.turn.TurnFixtures.takeable;
import static org.assertj.core.api.Assertions.assertThat;

import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.setup.SetupFixtures;
import io.turnbase.games.sevenwondersduel.snapshot.Snapshots;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.state.Phase;
import io.turnbase.games.sevenwondersduel.state.Victory;
import io.turnbase.games.sevenwondersduel.turn.TurnFixtures;
import io.turnbase.games.sevenwondersduel.wire.WireAction;
import io.turnbase.gdk.context.PlayerLeftReason;
import io.turnbase.gdk.effect.Effect;
import io.turnbase.gdk.effect.GameOverEffect;
import io.turnbase.gdk.model.Player;
import io.turnbase.gdk.result.ActionResult;
import io.turnbase.gdk.result.PlayerLeftResult;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GameOverTest {

  private static final long SEED = 7;
  private static final Map<UUID, UUID> TEAMS =
      Map.of(ONE.id(), ONE.teamId(), TWO.id(), TWO.teamId());

  private static Player seat(PlayerId id) {
    return id.value().equals(ONE.id()) ? ONE : TWO;
  }

  /** A game where building the first takeable card wins the game on the spot for its builder. */
  private static final ComponentId WINNING =
      takeable(SetupFixtures.startedAgeOne(TurnFixtures.rulesWith(), SEED));

  private static final RuleSet RULES =
      TurnFixtures.rulesWith(
          ageOne(WINNING)
              .withEffects(
                  (resolution, owner) ->
                      resolution.declareVictory(owner, Victory.Reason.MILITARY)));

  private static final SevenWondersDuelGame GAME = new SevenWondersDuelGame(RULES);

  private static SevenWondersDuelState running() {
    return Snapshots.toSnapshot(SetupFixtures.startedAgeOne(RULES, SEED)).withTeams(TEAMS);
  }

  private static long gameOvers(List<Effect> effects) {
    return effects.stream().filter(GameOverEffect.class::isInstance).count();
  }

  private static ActionResult<SevenWondersDuelState> winningMove(SevenWondersDuelState state) {
    PlayerId active = Snapshots.fromSnapshot(state).active();
    return GAME.onAction(
        state, action(seat(active), new WireAction("build", WINNING.value(), null, null)));
  }

  @Test
  void theMoveThatEndsTheGameCarriesExactlyOneGameOver() {
    SevenWondersDuelState before = running();
    PlayerId winner = Snapshots.fromSnapshot(before).active();

    ActionResult<SevenWondersDuelState> result = winningMove(before);

    assertThat(Snapshots.fromSnapshot(result.state()).phase()).isEqualTo(Phase.OVER);
    assertThat(gameOvers(result.effects())).isEqualTo(1);
    GameOverEffect over =
        (GameOverEffect)
            result.effects().stream().filter(GameOverEffect.class::isInstance).findFirst().get();
    UUID winningTeam = TEAMS.get(winner.value());
    assertThat(over.outcome().teamResults())
        .anyMatch(r -> r.teamId().equals(winningTeam) && r.placement() == 1);
    assertThat(over.outcome().draw()).isFalse();
  }

  @Test
  void aMoveAfterTheEndIsRejectedWithoutAnotherGameOver() {
    SevenWondersDuelState over = winningMove(running()).state();

    ActionResult<SevenWondersDuelState> result =
        GAME.onAction(over, action(ONE, new WireAction("discard", WINNING.value(), null, null)));

    assertThat(result.state()).isEqualTo(over);
    assertThat(gameOvers(result.effects())).isZero();
    assertThat(result.effects()).hasSize(1);
  }

  @Test
  void leavingAForfeitsToTheOpponent() {
    SevenWondersDuelState before = running();

    PlayerLeftResult<SevenWondersDuelState> result =
        GAME.onPlayerLeft(before, left(ONE, PlayerLeftReason.LEFT));

    DuelState after = Snapshots.fromSnapshot(result.state());
    assertThat(after.phase()).isEqualTo(Phase.OVER);
    assertThat(after.victory())
        .isEqualTo(new Victory(PlayerId.of(TWO.id()), Victory.Reason.FORFEIT));
    assertThat(result.state().teams()).isEqualTo(TEAMS);
    assertThat(gameOvers(result.effects())).isEqualTo(1);
  }

  @Test
  void leavingAfterTheEndChangesNothing() {
    SevenWondersDuelState over = winningMove(running()).state();

    PlayerLeftResult<SevenWondersDuelState> result =
        GAME.onPlayerLeft(over, left(ONE, PlayerLeftReason.GRACE_PERIOD_EXPIRED));

    assertThat(result.state()).isEqualTo(over);
    assertThat(result.effects()).isEmpty();
  }
}
