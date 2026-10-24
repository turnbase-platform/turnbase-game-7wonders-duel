package io.turnbase.games.sevenwondersduel;

import static io.turnbase.games.sevenwondersduel.GdkDoubles.ONE;
import static io.turnbase.games.sevenwondersduel.GdkDoubles.TWO;
import static io.turnbase.games.sevenwondersduel.GdkDoubles.init;
import static io.turnbase.games.sevenwondersduel.GdkDoubles.player;
import static io.turnbase.games.sevenwondersduel.GdkDoubles.spectator;
import static io.turnbase.games.sevenwondersduel.GdkDoubles.team;
import static io.turnbase.games.sevenwondersduel.GdkDoubles.viewer;
import static org.assertj.core.api.Assertions.assertThat;

import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.setup.SetupFixtures;
import io.turnbase.games.sevenwondersduel.snapshot.Snapshots;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.view.Viewer;
import io.turnbase.games.sevenwondersduel.view.Views;
import io.turnbase.games.sevenwondersduel.wire.ServerMessage;
import io.turnbase.gdk.model.Player;
import org.junit.jupiter.api.Test;

class GameResyncTest {

  private static final RuleSet RULES = SetupFixtures.baseSized();
  private static final SevenWondersDuelGame GAME = new SevenWondersDuelGame(RULES);
  private static final SevenWondersDuelState STATE =
      GAME.onInit(init(7, team(ONE), team(TWO))).state();
  private static final DuelState DUEL = Snapshots.fromSnapshot(STATE);

  private static ServerMessage viewFor(io.turnbase.gdk.context.ResyncContext ctx) {
    return (ServerMessage) GAME.onResync(STATE, ctx).view();
  }

  @Test
  void aPlayerSeesTheirSeatAndASpectatorThePublicPosition() {
    assertThat(viewFor(viewer(ONE)))
        .isEqualTo(
            ServerMessage.view(Views.of(DUEL, RULES, new Viewer.Seat(PlayerId.of(ONE.id())))));
    assertThat(viewFor(spectator()))
        .isEqualTo(ServerMessage.view(Views.of(DUEL, RULES, new Viewer.Spectator())));
  }

  @Test
  void aPlayerWhoIsNotSeatedGetsOnlyThePublicPosition() {
    Player stranger = player("00000000-0000-0000-0000-000000000009", "Stranger");

    assertThat(viewFor(viewer(stranger))).isEqualTo(viewFor(spectator()));
  }

  @Test
  void resyncIsRepeatableAndLeavesTheStateAlone() {
    SevenWondersDuelState copy = STATE;

    assertThat(viewFor(viewer(TWO))).isEqualTo(viewFor(viewer(TWO)));
    assertThat(STATE).isEqualTo(copy);
  }
}
