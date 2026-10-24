package io.turnbase.games.sevenwondersduel.wire;

import io.turnbase.games.sevenwondersduel.view.DuelView;

/**
 * What the game tells a client. Exactly one of {@code view} and {@code reason} is set.
 *
 * @param kind {@code view} for a picture of the game, {@code rejected} for a refused action
 * @param view the picture, for {@code view}
 * @param reason why the action was refused, for {@code rejected}
 */
public record ServerMessage(String kind, DuelView view, String reason) {

  public static ServerMessage view(DuelView view) {
    return new ServerMessage("view", view, null);
  }

  public static ServerMessage rejected(String reason) {
    return new ServerMessage("rejected", null, reason);
  }
}
