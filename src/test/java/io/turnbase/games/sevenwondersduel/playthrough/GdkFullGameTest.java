package io.turnbase.games.sevenwondersduel.playthrough;

import static io.turnbase.games.sevenwondersduel.GdkDoubles.ONE;
import static io.turnbase.games.sevenwondersduel.GdkDoubles.TWO;
import static io.turnbase.games.sevenwondersduel.GdkDoubles.action;
import static io.turnbase.games.sevenwondersduel.GdkDoubles.init;
import static io.turnbase.games.sevenwondersduel.GdkDoubles.team;
import static org.assertj.core.api.Assertions.assertThat;

import io.turnbase.games.sevenwondersduel.SevenWondersDuelGame;
import io.turnbase.games.sevenwondersduel.SevenWondersDuelState;
import io.turnbase.games.sevenwondersduel.agenda.ComponentOption;
import io.turnbase.games.sevenwondersduel.agenda.PlayerOption;
import io.turnbase.games.sevenwondersduel.catalog.BasePack;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.content.BaseContent;
import io.turnbase.games.sevenwondersduel.engine.Answer;
import io.turnbase.games.sevenwondersduel.engine.Build;
import io.turnbase.games.sevenwondersduel.engine.BuildWonder;
import io.turnbase.games.sevenwondersduel.engine.ChooseStarter;
import io.turnbase.games.sevenwondersduel.engine.Discard;
import io.turnbase.games.sevenwondersduel.engine.DraftWonder;
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
import io.turnbase.games.sevenwondersduel.wire.WireAction;
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

  private static WireAction wire(PlayerAction action) {
    return switch (action) {
      case DraftWonder a -> new WireAction("draftWonder", a.wonder().value(), null, null);
      case ChooseStarter a -> new WireAction("chooseStarter", null, null, a.starter().value());
      case Discard a -> new WireAction("discard", a.card().value(), null, null);
      case Build a -> new WireAction("build", a.card().value(), null, null);
      case BuildWonder a ->
          new WireAction("buildWonder", a.card().value(), a.wonder().value(), null);
      case Answer a when a.choice() instanceof ComponentOption o ->
          new WireAction("answerComponent", o.component().value(), null, null);
      case Answer a when a.choice() instanceof PlayerOption o ->
          new WireAction("answerPlayer", null, null, o.player().value());
      default -> throw new AssertionError("no wire form for " + action);
    };
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
          game.onAction(state, action(seat(move.actor()), wire(chosen)));

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
