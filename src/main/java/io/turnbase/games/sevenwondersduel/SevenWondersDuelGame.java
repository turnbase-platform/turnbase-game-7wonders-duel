package io.turnbase.games.sevenwondersduel;

import io.turnbase.games.sevenwondersduel.catalog.BasePack;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.engine.Engine;
import io.turnbase.games.sevenwondersduel.engine.IllegalActionException;
import io.turnbase.games.sevenwondersduel.engine.PlayerAction;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.setup.Setup;
import io.turnbase.games.sevenwondersduel.snapshot.Snapshots;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.view.Viewer;
import io.turnbase.games.sevenwondersduel.view.Views;
import io.turnbase.games.sevenwondersduel.wire.ServerMessage;
import io.turnbase.games.sevenwondersduel.wire.WireAction;
import io.turnbase.games.sevenwondersduel.wire.WireActions;
import io.turnbase.gdk.TurnBaseGame;
import io.turnbase.gdk.context.ActionContext;
import io.turnbase.gdk.context.ActionDecodeException;
import io.turnbase.gdk.context.InitContext;
import io.turnbase.gdk.context.PlayerLeftContext;
import io.turnbase.gdk.context.ResyncContext;
import io.turnbase.gdk.context.ViewerRole;
import io.turnbase.gdk.effect.Effect;
import io.turnbase.gdk.effect.Effects;
import io.turnbase.gdk.model.Player;
import io.turnbase.gdk.result.ActionResult;
import io.turnbase.gdk.result.InitResult;
import io.turnbase.gdk.result.PlayerLeftResult;
import io.turnbase.gdk.result.ViewResult;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** 7 Wonders Duel over the GDK: every hook is snapshot → {@link DuelState} → engine → snapshot. */
public final class SevenWondersDuelGame implements TurnBaseGame<SevenWondersDuelState> {

  private final RuleSet rules;

  /** The base game. */
  public SevenWondersDuelGame() {
    this(RuleSet.assemble(List.of(new BasePack())));
  }

  /** A game over any assembled rule set, so tests and expansions bring their own content. */
  public SevenWondersDuelGame(RuleSet rules) {
    this.rules = rules;
  }

  @Override
  public InitResult<SevenWondersDuelState> onInit(InitContext ctx) {
    Map<UUID, UUID> teams = new HashMap<>();
    List<Player> seated = ctx.teams().stream().flatMap(team -> team.players().stream()).toList();
    if (seated.size() != 2) {
      throw new IllegalArgumentException("a duel needs exactly two players, got " + seated.size());
    }
    seated.forEach(player -> teams.put(player.id(), player.teamId()));
    DuelState state =
        Setup.newGame(
            rules, ctx.seed(), PlayerId.of(seated.get(0).id()), PlayerId.of(seated.get(1).id()));
    return InitResult.of(Snapshots.toSnapshot(state).withTeams(teams));
  }

  @Override
  public ActionResult<SevenWondersDuelState> onAction(
      SevenWondersDuelState state, ActionContext ctx) {
    DuelState duel = Snapshots.fromSnapshot(state);
    PlayerId actor = PlayerId.of(ctx.player().id());
    try {
      if (!duel.players().containsKey(actor)) {
        throw new IllegalActionException("you are not playing in this game");
      }
      PlayerAction action = WireActions.decode(ctx.action(WireAction.class));
      DuelState next = new Engine(rules).apply(duel, actor, action).state();
      return ActionResult.from(Snapshots.toSnapshot(next).withTeams(state.teams()))
          .effects(shown(next))
          .build();
    } catch (IllegalActionException | ActionDecodeException e) {
      return ActionResult.from(state)
          .effect(Effects.sendToPlayer(actor.value(), ServerMessage.rejected(e.getMessage())))
          .build();
    }
  }

  /** Each seat its own view, in id order, then the public one for everybody. */
  private List<Effect> shown(DuelState state) {
    List<Effect> effects = new ArrayList<>();
    state.players().keySet().stream()
        .sorted(Comparator.comparing(PlayerId::value))
        .forEach(
            id ->
                effects.add(
                    Effects.sendToPlayer(
                        id.value(),
                        ServerMessage.view(Views.of(state, rules, new Viewer.Seat(id))))));
    effects.add(
        Effects.broadcast(ServerMessage.view(Views.of(state, rules, new Viewer.Spectator()))));
    return effects;
  }

  @Override
  public PlayerLeftResult<SevenWondersDuelState> onPlayerLeft(
      SevenWondersDuelState state, PlayerLeftContext ctx) {
    return null;
  }

  @Override
  public ViewResult onResync(SevenWondersDuelState state, ResyncContext ctx) {
    DuelState duel = Snapshots.fromSnapshot(state);
    Viewer viewer = new Viewer.Spectator();
    if (ctx.role() == ViewerRole.PLAYER && ctx.viewer().isPresent()) {
      PlayerId id = PlayerId.of(ctx.viewer().get().id());
      if (duel.players().containsKey(id)) {
        viewer = new Viewer.Seat(id);
      }
    }
    return ViewResult.of(ServerMessage.view(Views.of(duel, rules, viewer)));
  }
}
