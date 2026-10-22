package io.turnbase.games.sevenwondersduel;

import io.turnbase.gdk.TurnBaseGame;
import io.turnbase.gdk.context.ActionContext;
import io.turnbase.gdk.context.ActionDecodeException;
import io.turnbase.gdk.context.InitContext;
import io.turnbase.gdk.context.PlayerLeftContext;
import io.turnbase.gdk.context.ResyncContext;
import io.turnbase.gdk.effect.Effect;
import io.turnbase.gdk.effect.Effects;
import io.turnbase.gdk.model.GameOutcome;
import io.turnbase.gdk.model.Player;
import io.turnbase.gdk.model.Team;
import io.turnbase.gdk.result.ActionResult;
import io.turnbase.gdk.result.InitResult;
import io.turnbase.gdk.result.PlayerLeftResult;
import io.turnbase.gdk.result.ViewResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 7 Wonders Duel for two players, one per team (the manifest's two single-seat team templates).
 *
 * <p>This is a skeleton: players alternate {@code END_TURN} until {@link #MAX_TURNS} turns have
 * been played, then the game is a draw. It exists to prove the wiring — manifest, bundle, GDK
 * hooks, frontend — before the rules are written. Replace {@link #onAction} with the real ones.
 */
public final class SevenWondersDuelGame implements TurnBaseGame<SevenWondersDuelState> {

  /** Placeholder game length. */
  static final int MAX_TURNS = 6;

  @Override
  public InitResult<SevenWondersDuelState> onInit(InitContext ctx) {
    List<Team> teams = ctx.teams();
    Team first = teams.get(0);
    Team second = teams.get(1);
    Player a = first.players().get(0);
    Player b = second.players().get(0);
    var state =
        new SevenWondersDuelState(a.id(), first.id(), b.id(), second.id(), a.id(), 0, false);
    return InitResult.from(state)
        .effect(
            Effects.log(
                "**" + a.displayName() + "** and **" + b.displayName() + "** begin the duel"))
        .build();
  }

  @Override
  public ActionResult<SevenWondersDuelState> onAction(SevenWondersDuelState s, ActionContext ctx) {
    UUID actor = ctx.player().id();
    if (s.over()) {
      return rejected(s, actor, "The game is over.");
    }
    if (!actor.equals(s.turn())) {
      return rejected(s, actor, "It is not your turn.");
    }
    if (!Action.END_TURN.equals(decodeType(ctx))) {
      return rejected(s, actor, "Unknown action.");
    }

    int turnNumber = s.turnNumber() + 1;
    boolean over = turnNumber >= MAX_TURNS;
    UUID next = over ? actor : s.other(actor);
    var nextState =
        new SevenWondersDuelState(
            s.firstPlayer(),
            s.firstTeam(),
            s.secondPlayer(),
            s.secondTeam(),
            next,
            turnNumber,
            over);

    List<Effect> effects = new ArrayList<>();
    effects.add(Effects.log("**" + ctx.player().displayName() + "** ended their turn"));
    effects.add(Effects.broadcast(GameMessage.TurnEnded.of(actor, next, turnNumber)));
    if (over) {
      effects.add(Effects.gameOver(outcome(nextState, null), GameMessage.Result.of(null, "draw")));
    }
    return ActionResult.from(nextState).effects(effects).build();
  }

  @Override
  public PlayerLeftResult<SevenWondersDuelState> onPlayerLeft(
      SevenWondersDuelState s, PlayerLeftContext ctx) {
    UUID leaver = ctx.player().id();
    if (s.over() || !s.isSeated(leaver)) {
      return PlayerLeftResult.of(s);
    }
    UUID winner = s.other(leaver);
    var nextState =
        new SevenWondersDuelState(
            s.firstPlayer(),
            s.firstTeam(),
            s.secondPlayer(),
            s.secondTeam(),
            winner,
            s.turnNumber(),
            true);
    return PlayerLeftResult.from(nextState)
        .effect(Effects.log("**" + ctx.player().displayName() + "** left — the opponent wins"))
        .effect(
            Effects.gameOver(outcome(nextState, winner), GameMessage.Result.of(winner, "forfeit")))
        .build();
  }

  @Override
  public ViewResult onResync(SevenWondersDuelState s, ResyncContext ctx) {
    return ViewResult.of(GameMessage.View.of(s));
  }

  /** The action's {@code type}, or {@code null} when the payload is absent or unparsable. */
  private static String decodeType(ActionContext ctx) {
    try {
      Action action = ctx.action(Action.class);
      return action == null ? null : action.type();
    } catch (ActionDecodeException e) {
      return null;
    }
  }

  private static ActionResult<SevenWondersDuelState> rejected(
      SevenWondersDuelState s, UUID actor, String reason) {
    return ActionResult.from(s)
        .effect(Effects.sendToPlayer(actor, GameMessage.Rejected.of(reason)))
        .build();
  }

  /** {@code winner == null} is a draw: both teams share placement 1. */
  private static GameOutcome outcome(SevenWondersDuelState s, UUID winner) {
    var b = GameOutcome.builder();
    if (winner == null) {
      b.teamResult(s.firstTeam(), 1, Map.of()).teamResult(s.secondTeam(), 1, Map.of()).draw(true);
    } else {
      UUID loser = s.other(winner);
      b.teamResult(s.teamOf(winner), 1, Map.of())
          .teamResult(s.teamOf(loser), 2, Map.of())
          .draw(false);
    }
    return b.playerResult(s.firstPlayer(), Map.of())
        .playerResult(s.secondPlayer(), Map.of())
        .build();
  }
}
