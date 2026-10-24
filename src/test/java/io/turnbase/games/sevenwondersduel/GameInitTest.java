package io.turnbase.games.sevenwondersduel;

import static io.turnbase.games.sevenwondersduel.GdkDoubles.ONE;
import static io.turnbase.games.sevenwondersduel.GdkDoubles.TWO;
import static io.turnbase.games.sevenwondersduel.GdkDoubles.init;
import static io.turnbase.games.sevenwondersduel.GdkDoubles.player;
import static io.turnbase.games.sevenwondersduel.GdkDoubles.team;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.turnbase.games.sevenwondersduel.setup.SetupFixtures;
import io.turnbase.gdk.result.InitResult;
import java.util.Map;
import org.junit.jupiter.api.Test;

class GameInitTest {

  private static final SevenWondersDuelGame GAME =
      new SevenWondersDuelGame(SetupFixtures.baseSized());

  @Test
  void twoPlayersStartTheWonderDraftWithTheirTeamsRecorded() {
    InitResult<SevenWondersDuelState> result = GAME.onInit(init(7, team(ONE), team(TWO)));

    SevenWondersDuelState state = result.state();
    assertThat(state.phase()).isEqualTo("WONDER_DRAFT");
    assertThat(state.players()).containsOnlyKeys(ONE.id(), TWO.id());
    assertThat(state.teams()).isEqualTo(Map.of(ONE.id(), ONE.teamId(), TWO.id(), TWO.teamId()));
    assertThat(result.effects()).isEmpty();
  }

  @Test
  void twoPlayersOnOneTeamAreStillADuel() {
    SevenWondersDuelState state = GAME.onInit(init(7, team(ONE, TWO))).state();

    assertThat(state.players()).containsOnlyKeys(ONE.id(), TWO.id());
  }

  @Test
  void theSeedDecidesTheGame() {
    var teams = new io.turnbase.gdk.model.Team[] {team(ONE), team(TWO)};

    assertThat(GAME.onInit(init(7, teams)).state()).isEqualTo(GAME.onInit(init(7, teams)).state());
    assertThat(GAME.onInit(init(8, teams)).state())
        .isNotEqualTo(GAME.onInit(init(7, teams)).state());
  }

  @Test
  void aDuelNeedsExactlyTwoPlayers() {
    var three = player("00000000-0000-0000-0000-000000000003", "Three");

    assertThatThrownBy(() -> GAME.onInit(init(7, team(ONE))))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> GAME.onInit(init(7, team(ONE), team(TWO), team(three))))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
