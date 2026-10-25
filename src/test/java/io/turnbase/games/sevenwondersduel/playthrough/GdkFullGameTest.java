package io.turnbase.games.sevenwondersduel.playthrough;

import static io.turnbase.games.sevenwondersduel.GdkDoubles.ONE;
import static io.turnbase.games.sevenwondersduel.GdkDoubles.TWO;
import static io.turnbase.games.sevenwondersduel.GdkDoubles.action;
import static io.turnbase.games.sevenwondersduel.GdkDoubles.init;
import static io.turnbase.games.sevenwondersduel.GdkDoubles.team;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;

import io.turnbase.games.sevenwondersduel.SevenWondersDuelGame;
import io.turnbase.games.sevenwondersduel.SevenWondersDuelState;
import io.turnbase.games.sevenwondersduel.catalog.BasePack;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.content.BaseContent;
import io.turnbase.games.sevenwondersduel.engine.Engine;
import io.turnbase.games.sevenwondersduel.engine.PlayerAction;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.snapshot.Snapshots;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.state.Phase;
import io.turnbase.games.sevenwondersduel.view.DuelView;
import io.turnbase.games.sevenwondersduel.view.PlacedView;
import io.turnbase.games.sevenwondersduel.wire.ServerMessage;
import io.turnbase.games.sevenwondersduel.zone.Placement;
import io.turnbase.games.sevenwondersduel.zone.Zones;
import io.turnbase.gdk.effect.BroadcastEffect;
import io.turnbase.gdk.effect.Effect;
import io.turnbase.gdk.effect.GameOverEffect;
import io.turnbase.gdk.effect.SendToPlayerEffect;
import io.turnbase.gdk.model.Player;
import io.turnbase.gdk.result.ActionResult;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** One whole game driven through the GDK hooks, as the Game Host would. */
class GdkFullGameTest {

  private static final long SEED = 11;
  private static final RuleSet RULES = RuleSet.assemble(List.of(new BasePack(), new BaseContent()));
  private static final Set<String> FACE_DOWN =
      Set.of(Zones.DECK.value(), Zones.STRUCTURE_DOWN.value(), Zones.BOX.value());

  private static Player seat(PlayerId id) {
    return id.value().equals(ONE.id()) ? ONE : TWO;
  }

  @Test
  void aWholeGameRunsThroughTheHooksWithOneGameOverAndNoLeakedCards() {
    SevenWondersDuelGame game = new SevenWondersDuelGame();
    SevenWondersDuelState state = game.onInit(init(SEED, team(ONE), team(TWO))).state();
    Random random = new Random(SEED);
    int gameOvers = 0;
    int steps = 0;

    while (Snapshots.fromSnapshot(state).phase() != Phase.OVER) {
      assertThat(++steps).as("steps").isLessThanOrEqualTo(400);
      DuelState duel = Snapshots.fromSnapshot(state);
      Bot.Move move = Bot.legalActions(RULES, duel);
      PlayerAction chosen = move.actions().get(random.nextInt(move.actions().size()));

      ActionResult<SevenWondersDuelState> result =
          game.onAction(state, action(seat(move.actor()), Moves.wire(chosen)));

      DuelState after = Snapshots.fromSnapshot(result.state());
      assertThat(after)
          .as("step " + steps + " matches the engine")
          .isEqualTo(new Engine(RULES).apply(duel, move.actor(), chosen).state());
      gameOvers += (int) result.effects().stream().filter(GameOverEffect.class::isInstance).count();
      assertNoLeaks(after, result.effects(), steps);
      state = result.state();
    }

    assertThat(gameOvers).isEqualTo(1);
  }

  private static void assertNoLeaks(DuelState after, List<Effect> effects, int step) {
    for (Effect effect : effects) {
      ServerMessage message =
          effect instanceof SendToPlayerEffect send
              ? (ServerMessage) send.payload()
              : effect instanceof BroadcastEffect broadcast
                  ? (ServerMessage) broadcast.payload()
                  : null;
      if (message != null) {
        Set<String> identified =
            message.view().visible().stream().map(PlacedView::component).collect(toSet());
        assertThat(message.events())
            .as("step " + step + ": events name only what the view identifies")
            .allSatisfy(
                event -> {
                  assertThat(event.component())
                      .satisfiesAnyOf(
                          c -> assertThat(c).isNull(), c -> assertThat(identified).contains(c));
                  assertThat(event.cause())
                      .satisfiesAnyOf(
                          c -> assertThat(c).isNull(), c -> assertThat(identified).contains(c));
                });
        assertThat(faceDownShown(after, message))
            .as("step " + step + ": hidden structure cards in a view")
            .noneMatch(id -> !Zones.BOX.value().equals(zoneOf(after, id)));
      }
    }
    for (Effect effect : effects) {
      if (effect instanceof SendToPlayerEffect send) {
        Set<ComponentId> known = after.knowledge().get(PlayerId.of(send.playerId()));
        assertThat(faceDownShown(after, (ServerMessage) send.payload()))
            .as("step " + step + ": face-down cards shown to " + send.playerId())
            .allSatisfy(id -> assertThat(known).contains(id));
      } else if (effect instanceof BroadcastEffect broadcast) {
        assertThat(faceDownShown(after, (ServerMessage) broadcast.payload()))
            .as("step " + step + ": face-down cards broadcast")
            .isEmpty();
      }
    }
  }

  private static List<ComponentId> faceDownShown(DuelState state, ServerMessage message) {
    DuelView view = message.view();
    return view.visible().stream()
        .map(PlacedView::component)
        .map(ComponentId::of)
        .filter(id -> FACE_DOWN.contains(zoneOf(state, id)))
        .toList();
  }

  private static String zoneOf(DuelState state, ComponentId id) {
    return state.placements().stream()
        .filter(p -> p.component().equals(id))
        .map(Placement::at)
        .findFirst()
        .orElseThrow()
        .zone()
        .value();
  }
}
