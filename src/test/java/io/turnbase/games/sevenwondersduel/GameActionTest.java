package io.turnbase.games.sevenwondersduel;

import static io.turnbase.games.sevenwondersduel.GdkDoubles.ONE;
import static io.turnbase.games.sevenwondersduel.GdkDoubles.TWO;
import static io.turnbase.games.sevenwondersduel.GdkDoubles.action;
import static io.turnbase.games.sevenwondersduel.GdkDoubles.init;
import static io.turnbase.games.sevenwondersduel.GdkDoubles.player;
import static io.turnbase.games.sevenwondersduel.GdkDoubles.team;
import static io.turnbase.games.sevenwondersduel.turn.TurnFixtures.ageOne;
import static io.turnbase.games.sevenwondersduel.turn.TurnFixtures.takeable;
import static org.assertj.core.api.Assertions.assertThat;

import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.cost.Cost;
import io.turnbase.games.sevenwondersduel.draft.Draft;
import io.turnbase.games.sevenwondersduel.engine.DraftWonder;
import io.turnbase.games.sevenwondersduel.engine.Engine;
import io.turnbase.games.sevenwondersduel.event.EventRecord;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.setup.SetupFixtures;
import io.turnbase.games.sevenwondersduel.snapshot.Snapshots;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.turn.TurnFixtures;
import io.turnbase.games.sevenwondersduel.view.EventViews;
import io.turnbase.games.sevenwondersduel.view.Viewer;
import io.turnbase.games.sevenwondersduel.view.Views;
import io.turnbase.games.sevenwondersduel.wire.ServerMessage;
import io.turnbase.games.sevenwondersduel.wire.WireAction;
import io.turnbase.games.sevenwondersduel.zone.Placements;
import io.turnbase.games.sevenwondersduel.zone.Zones;
import io.turnbase.gdk.effect.BroadcastEffect;
import io.turnbase.gdk.effect.SendToPlayerEffect;
import io.turnbase.gdk.model.Player;
import io.turnbase.gdk.result.ActionResult;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GameActionTest {

  private static final long SEED = 7;
  private static final Map<UUID, UUID> TEAMS =
      Map.of(ONE.id(), ONE.teamId(), TWO.id(), TWO.teamId());

  private static final RuleSet BASE = SetupFixtures.baseSized();
  private static final SevenWondersDuelGame GAME = new SevenWondersDuelGame(BASE);

  private static Player seat(PlayerId id) {
    return id.value().equals(ONE.id()) ? ONE : TWO;
  }

  private static SevenWondersDuelState drafting() {
    return GAME.onInit(init(SEED, team(ONE), team(TWO))).state();
  }

  private static ComponentId offered(DuelState state) {
    return Placements.idsIn(state.placements(), Zones.DRAFT).get(0);
  }

  @Test
  void anAcceptedActionShowsEachSeatItsViewAndEveryoneThePublicOne() {
    SevenWondersDuelState before = drafting();
    DuelState duel = Snapshots.fromSnapshot(before);
    PlayerId mover = Draft.toMove(duel).orElseThrow();

    ActionResult<SevenWondersDuelState> result =
        GAME.onAction(
            before,
            action(seat(mover), new WireAction("draftWonder", offered(duel).value(), null, null)));

    DuelState after = Snapshots.fromSnapshot(result.state());
    List<EventRecord> events =
        new Engine(BASE).apply(duel, mover, new DraftWonder(offered(duel))).events();
    assertThat(after.placements()).isNotEqualTo(duel.placements());
    assertThat(result.state().teams()).isEqualTo(TEAMS);
    assertThat(events).isNotEmpty();
    assertThat(result.effects()).hasSize(3);
    for (PlayerId id : after.players().keySet()) {
      Viewer viewer = new Viewer.Seat(id);
      assertThat(result.effects())
          .contains(
              new SendToPlayerEffect(
                  id.value(),
                  ServerMessage.view(
                      Views.of(after, BASE, viewer), EventViews.of(events, after, BASE, viewer))));
    }
    Viewer spectator = new Viewer.Spectator();
    assertThat(result.effects())
        .contains(
            new BroadcastEffect(
                ServerMessage.view(
                    Views.of(after, BASE, spectator),
                    EventViews.of(events, after, BASE, spectator))));
  }

  @Test
  void thePublicViewComesFirstSoNoSeatEndsWithIt() {
    SevenWondersDuelState before = drafting();
    DuelState duel = Snapshots.fromSnapshot(before);
    PlayerId mover = Draft.toMove(duel).orElseThrow();

    ActionResult<SevenWondersDuelState> result =
        GAME.onAction(
            before,
            action(seat(mover), new WireAction("draftWonder", offered(duel).value(), null, null)));

    assertThat(result.effects().get(0)).isInstanceOf(BroadcastEffect.class);
    assertThat(result.effects().subList(1, result.effects().size()))
        .allMatch(SendToPlayerEffect.class::isInstance);
  }

  @Test
  void theBroadcastNamesNoHiddenCard() {
    SevenWondersDuelState before = drafting();
    DuelState duel = Snapshots.fromSnapshot(before);
    PlayerId mover = Draft.toMove(duel).orElseThrow();

    ActionResult<SevenWondersDuelState> result =
        GAME.onAction(
            before,
            action(seat(mover), new WireAction("draftWonder", offered(duel).value(), null, null)));

    String broadcast =
        result.effects().stream()
            .filter(BroadcastEffect.class::isInstance)
            .map(e -> ((BroadcastEffect) e).payload().toString())
            .findFirst()
            .orElseThrow();
    for (ComponentId hidden : Placements.idsIn(duel.placements(), Zones.BOX)) {
      assertThat(broadcast)
          .doesNotContain("\"" + hidden.value() + "\"")
          .doesNotContain("component=" + hidden.value() + ",");
    }
  }

  private static void assertRejected(
      SevenWondersDuelGame game, SevenWondersDuelState state, Player actor, Object payload) {
    ActionResult<SevenWondersDuelState> result = game.onAction(state, action(actor, payload));

    assertThat(result.state()).isEqualTo(state);
    assertThat(result.effects()).hasSize(1);
    SendToPlayerEffect only = (SendToPlayerEffect) result.effects().get(0);
    assertThat(only.playerId()).isEqualTo(actor.id());
    ServerMessage message = (ServerMessage) only.payload();
    assertThat(message.kind()).isEqualTo("rejected");
    assertThat(message.reason()).isNotBlank();
    assertThat(message.view()).isNull();
  }

  @Test
  void aMoveOutOfTurnIsRejectedPrivately() {
    SevenWondersDuelState before = drafting();
    DuelState duel = Snapshots.fromSnapshot(before);
    PlayerId notMover = duel.opponentOf(Draft.toMove(duel).orElseThrow());

    assertRejected(
        GAME,
        before,
        seat(notMover),
        new WireAction("draftWonder", offered(duel).value(), null, null));
  }

  @Test
  void anUnaffordableBuildIsRejectedPrivately() {
    ComponentId card = takeable(SetupFixtures.startedAgeOne(TurnFixtures.rulesWith(), SEED));
    RuleSet rules = TurnFixtures.rulesWith(ageOne(card).costing(Cost.coins(99)));
    DuelState age = SetupFixtures.startedAgeOne(rules, SEED);
    SevenWondersDuelState state = Snapshots.toSnapshot(age).withTeams(TEAMS);

    assertRejected(
        new SevenWondersDuelGame(rules),
        state,
        seat(age.active()),
        new WireAction("build", card.value(), null, null));
  }

  @Test
  void anUndecodablePayloadIsRejectedPrivately() {
    assertRejected(GAME, drafting(), ONE, new WireAction("fly", null, null, null));
  }

  @Test
  void aStrangerCannotAct() {
    SevenWondersDuelState before = drafting();
    DuelState duel = Snapshots.fromSnapshot(before);
    Player stranger = player("00000000-0000-0000-0000-000000000009", "Stranger");

    assertRejected(
        GAME, before, stranger, new WireAction("draftWonder", offered(duel).value(), null, null));
  }

  @Test
  void nothingBroadcastsWhenRejected() {
    ActionResult<SevenWondersDuelState> result =
        GAME.onAction(drafting(), action(ONE, new WireAction("fly", null, null, null)));

    assertThat(result.effects()).noneMatch(BroadcastEffect.class::isInstance);
  }
}
