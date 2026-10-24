package io.turnbase.games.sevenwondersduel;

import io.turnbase.games.sevenwondersduel.catalog.BasePack;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.setup.Setup;
import io.turnbase.games.sevenwondersduel.snapshot.Snapshots;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.gdk.TurnBaseGame;
import io.turnbase.gdk.context.ActionContext;
import io.turnbase.gdk.context.InitContext;
import io.turnbase.gdk.context.PlayerLeftContext;
import io.turnbase.gdk.context.ResyncContext;
import io.turnbase.gdk.model.Player;
import io.turnbase.gdk.result.ActionResult;
import io.turnbase.gdk.result.InitResult;
import io.turnbase.gdk.result.PlayerLeftResult;
import io.turnbase.gdk.result.ViewResult;
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
    return null;
  }

  @Override
  public PlayerLeftResult<SevenWondersDuelState> onPlayerLeft(
      SevenWondersDuelState state, PlayerLeftContext ctx) {
    return null;
  }

  @Override
  public ViewResult onResync(SevenWondersDuelState state, ResyncContext ctx) {
    return null;
  }
}
