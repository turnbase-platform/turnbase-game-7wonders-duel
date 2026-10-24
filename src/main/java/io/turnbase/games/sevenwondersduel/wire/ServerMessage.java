package io.turnbase.games.sevenwondersduel.wire;

import io.turnbase.games.sevenwondersduel.view.DuelView;
import io.turnbase.games.sevenwondersduel.view.EventView;
import java.util.List;

/**
 * What the game tells a client. Exactly one of {@code view} and {@code reason} is set.
 *
 * @param kind {@code view} for a picture of the game, {@code rejected} for a refused action
 * @param view the picture, for {@code view}
 * @param reason why the action was refused, for {@code rejected}
 * @param events what happened to bring the game to {@code view}, as this client may know it; empty
 *     for a resync and for a refusal. The view is the truth, the events only explain it.
 */
public record ServerMessage(String kind, DuelView view, String reason, List<EventView> events) {

  public ServerMessage {
    events = List.copyOf(events);
  }

  /** A picture with nothing to explain, e.g. for a client that has just (re)connected. */
  public static ServerMessage view(DuelView view) {
    return view(view, List.of());
  }

  public static ServerMessage view(DuelView view, List<EventView> events) {
    return new ServerMessage("view", view, null, events);
  }

  public static ServerMessage rejected(String reason) {
    return new ServerMessage("rejected", null, reason, List.of());
  }
}
