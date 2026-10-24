package io.turnbase.games.sevenwondersduel;

import io.turnbase.gdk.TurnBaseGame;
import io.turnbase.gdk.context.ActionContext;
import io.turnbase.gdk.context.InitContext;
import io.turnbase.gdk.context.PlayerLeftContext;
import io.turnbase.gdk.context.ResyncContext;
import io.turnbase.gdk.result.ActionResult;
import io.turnbase.gdk.result.InitResult;
import io.turnbase.gdk.result.PlayerLeftResult;
import io.turnbase.gdk.result.ViewResult;

public final class SevenWondersDuelGame implements TurnBaseGame<SevenWondersDuelState> {
  @Override
  public InitResult<SevenWondersDuelState> onInit(InitContext ctx) {
    return null;
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
