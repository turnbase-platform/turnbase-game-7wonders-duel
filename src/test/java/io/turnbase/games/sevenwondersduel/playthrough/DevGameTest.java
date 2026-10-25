package io.turnbase.games.sevenwondersduel.playthrough;

import static io.turnbase.games.sevenwondersduel.GdkDoubles.ONE;
import static io.turnbase.games.sevenwondersduel.GdkDoubles.TWO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.turnbase.games.sevenwondersduel.SevenWondersDuelState;
import io.turnbase.games.sevenwondersduel.playthrough.DevGame.Delivery;
import io.turnbase.games.sevenwondersduel.snapshot.Snapshots;
import io.turnbase.games.sevenwondersduel.state.Phase;
import io.turnbase.games.sevenwondersduel.wire.ServerMessage;
import io.turnbase.games.sevenwondersduel.wire.WireAction;
import java.util.List;
import org.junit.jupiter.api.Test;

class DevGameTest {

  private static WireAction firstOffer(DevGame game) {
    var draft =
        game.resync("1").view().visible().stream()
            .filter(p -> p.zone().equals("draft"))
            .findFirst()
            .orElseThrow();
    return new WireAction("draftWonder", draft.component(), null, null);
  }

  /** Whoever drafts first, so a test does not depend on the seed's coin toss. */
  private static String mover(DevGame game) {
    return game.resync("1").view().active().equals(ONE.id()) ? "1" : "2";
  }

  private static List<Delivery> to(List<Delivery> all, String viewer) {
    return all.stream().filter(d -> d.viewer().equals(viewer)).toList();
  }

  @Test
  void aSeatGetsTheBroadcastThenItsOwnViewAndASpectatorOnlyTheBroadcast() {
    DevGame game = new DevGame(7);

    List<Delivery> sent = game.act(mover(game), firstOffer(game));

    assertThat(to(sent, "spectator")).hasSize(1);
    assertThat(((ServerMessage) to(sent, "spectator").get(0).payload()).broadcast()).isTrue();
    for (String seat : List.of("1", "2")) {
      List<Delivery> mine = to(sent, seat);
      assertThat(mine).hasSize(2);
      assertThat(((ServerMessage) mine.get(0).payload()).broadcast()).isTrue();
      assertThat(((ServerMessage) mine.get(1).payload()).broadcast()).isFalse();
    }
    assertThat(sent).allMatch(d -> d.channel().equals("game_event"));
  }

  @Test
  void aRefusalGoesToTheActorOnly() {
    DevGame game = new DevGame(7);
    String other = mover(game).equals("1") ? "2" : "1";

    List<Delivery> sent = game.act(other, firstOffer(game));

    assertThat(sent).hasSize(1);
    assertThat(sent.get(0).viewer()).isEqualTo(other);
    assertThat(((ServerMessage) sent.get(0).payload()).kind()).isEqualTo("rejected");
  }

  @Test
  void aSpectatorCannotAct() {
    DevGame game = new DevGame(7);

    assertThatThrownBy(() -> game.act("spectator", firstOffer(game)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void aSeatsResyncIsItsOwnViewWithTheCatalog() {
    DevGame game = new DevGame(7);

    ServerMessage seat = game.resync("1");
    ServerMessage spectator = game.resync("spectator");

    assertThat(seat.catalog()).isNotNull();
    assertThat(spectator.catalog()).isNotNull();
    assertThat(seat.broadcast()).isFalse();
    assertThat(game.resync("2").view().phase()).isEqualTo("WONDER_DRAFT");
  }

  @Test
  void theSameSeedGivesTheSamePositionAndAnotherSeedAnother() {
    DevGame game = new DevGame(7);
    ServerMessage first = game.resync("1");

    game.restart(7);
    assertThat(game.resync("1")).isEqualTo(first);

    game.restart(8);
    assertThat(game.resync("1")).isNotEqualTo(first);
  }

  @Test
  void advancingPlaysTheGivenNumberOfMovesAndStopsWhenTheGameIsOver() {
    DevGame game = new DevGame(7);

    game.advance(30);
    assertThat(game.moves()).isEqualTo(30);
    assertThat(game.resync("1").view().phase()).isNotEqualTo("WONDER_DRAFT");

    game.advance(10_000);
    assertThat(phase(game)).isEqualTo(Phase.OVER);
    assertThat(game.moves()).isLessThan(10_000);
  }

  @Test
  void advancingIsRepeatableFromTheSameSeed() {
    DevGame a = new DevGame(7);
    DevGame b = new DevGame(7);

    a.advance(40);
    b.advance(40);

    assertThat(a.resync("1")).isEqualTo(b.resync("1"));
  }

  private static Phase phase(DevGame game) {
    SevenWondersDuelState state = game.state();
    return Snapshots.fromSnapshot(state).phase();
  }

  @Test
  void theRosterNamesTheTwoSeats() {
    assertThat(DevGame.seats()).containsExactly(ONE, TWO);
  }
}
