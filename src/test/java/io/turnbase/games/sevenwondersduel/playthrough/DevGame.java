package io.turnbase.games.sevenwondersduel.playthrough;

import static io.turnbase.games.sevenwondersduel.GdkDoubles.ONE;
import static io.turnbase.games.sevenwondersduel.GdkDoubles.TWO;
import static io.turnbase.games.sevenwondersduel.GdkDoubles.action;
import static io.turnbase.games.sevenwondersduel.GdkDoubles.init;
import static io.turnbase.games.sevenwondersduel.GdkDoubles.spectator;
import static io.turnbase.games.sevenwondersduel.GdkDoubles.team;
import static io.turnbase.games.sevenwondersduel.GdkDoubles.viewer;

import io.turnbase.games.sevenwondersduel.SevenWondersDuelGame;
import io.turnbase.games.sevenwondersduel.SevenWondersDuelState;
import io.turnbase.games.sevenwondersduel.catalog.BasePack;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.content.BaseContent;
import io.turnbase.games.sevenwondersduel.engine.PlayerAction;
import io.turnbase.games.sevenwondersduel.snapshot.Snapshots;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.state.Phase;
import io.turnbase.games.sevenwondersduel.wire.ServerMessage;
import io.turnbase.games.sevenwondersduel.wire.WireAction;
import io.turnbase.gdk.effect.BroadcastEffect;
import io.turnbase.gdk.effect.Effect;
import io.turnbase.gdk.effect.GameOverEffect;
import io.turnbase.gdk.effect.SendToPlayerEffect;
import io.turnbase.gdk.model.Player;
import io.turnbase.gdk.result.ActionResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * The real game on a plain JVM with a fixed seed, delivering effects the way the platform does: a
 * broadcast reaches both seats and the spectator, a send reaches one seat. Viewers are named {@code
 * "1"}, {@code "2"} and {@code "spectator"}. It is the engine of the frontend dev harness ({@link
 * DevServer}); no I/O, so tests drive it directly.
 */
final class DevGame {

  /** What one viewer's client receives: a {@code game_event} or a {@code game_over} payload. */
  record Delivery(String viewer, String channel, Object payload) {}

  static final List<String> VIEWERS = List.of("1", "2", "spectator");

  private static final RuleSet RULES = RuleSet.assemble(List.of(new BasePack(), new BaseContent()));

  private final SevenWondersDuelGame game = new SevenWondersDuelGame();
  private SevenWondersDuelState state;
  private long seed;
  private Random random;
  private int moves;

  DevGame(long seed) {
    restart(seed);
  }

  static List<Player> seats() {
    return List.of(ONE, TWO);
  }

  static Player seat(String viewer) {
    return switch (viewer) {
      case "1" -> ONE;
      case "2" -> TWO;
      default -> throw new IllegalArgumentException("not a seat: " + viewer);
    };
  }

  synchronized void restart(long newSeed) {
    seed = newSeed;
    random = new Random(newSeed);
    moves = 0;
    state = game.onInit(init(newSeed, team(ONE), team(TWO))).state();
  }

  synchronized long seed() {
    return seed;
  }

  synchronized int moves() {
    return moves;
  }

  synchronized SevenWondersDuelState state() {
    return state;
  }

  /** What {@code onResync} gives this viewer: a seat its own view, anybody else the public one. */
  synchronized ServerMessage resync(String viewer) {
    var context = viewer.equals("spectator") ? spectator() : viewer(seat(viewer));
    return (ServerMessage) game.onResync(state, context).view();
  }

  /** Applies a seat's action; refusals are deliveries too. A spectator cannot act. */
  synchronized List<Delivery> act(String viewer, WireAction action) {
    ActionResult<SevenWondersDuelState> result = game.onAction(state, action(seat(viewer), action));
    if (!result.state().equals(state)) {
      moves++;
    }
    state = result.state();
    return fanOut(viewer, result.effects());
  }

  /** Plays up to {@code steps} random legal moves without telling anybody; stops at game over. */
  synchronized void advance(int steps) {
    for (int i = 0; i < steps; i++) {
      DuelState duel = Snapshots.fromSnapshot(state);
      if (duel.phase() == Phase.OVER) {
        return;
      }
      Bot.Move move = Bot.legalActions(RULES, duel);
      PlayerAction chosen = move.actions().get(random.nextInt(move.actions().size()));
      act(move.actor().value().equals(ONE.id()) ? "1" : "2", Moves.wire(chosen));
    }
  }

  private static List<Delivery> fanOut(String actor, List<Effect> effects) {
    List<Delivery> out = new ArrayList<>();
    for (Effect effect : effects) {
      switch (effect) {
        case BroadcastEffect e ->
            VIEWERS.forEach(v -> out.add(new Delivery(v, "game_event", e.payload())));
        case SendToPlayerEffect e ->
            out.add(new Delivery(viewerOf(e.playerId()), "game_event", e.payload()));
        case GameOverEffect e ->
            VIEWERS.forEach(v -> out.add(new Delivery(v, "game_over", e.resultsPayload())));
        default -> throw new IllegalStateException("the dev harness does not deliver " + effect);
      }
    }
    return out;
  }

  private static String viewerOf(UUID player) {
    return player.equals(ONE.id()) ? "1" : "2";
  }
}
