package io.turnbase.games.sevenwondersduel.wire;

import io.turnbase.games.sevenwondersduel.view.CatalogView;
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
 * @param catalog every component's static data, sent with a resync and null otherwise: ids in the
 *     view and the events are looked up here
 * @param broadcast true for the public message that went to every client; the GDK delivers a
 *     broadcast to the seats as well, so a seat skips it and applies the message addressed to it
 */
public record ServerMessage(
    String kind,
    DuelView view,
    String reason,
    List<EventView> events,
    CatalogView catalog,
    boolean broadcast) {

  public ServerMessage {
    events = List.copyOf(events);
  }

  /** A picture with nothing to explain, e.g. for a client that has just (re)connected. */
  public static ServerMessage view(DuelView view) {
    return view(view, List.of());
  }

  /** A picture for a client that has just (re)connected, with the catalog its ids refer to. */
  public static ServerMessage resync(DuelView view, CatalogView catalog) {
    return new ServerMessage("view", view, null, List.of(), catalog, false);
  }

  public static ServerMessage view(DuelView view, List<EventView> events) {
    return new ServerMessage("view", view, null, events, null, false);
  }

  /** The public picture sent to everybody; a seat also gets its own and prefers that. */
  public static ServerMessage broadcast(DuelView view, List<EventView> events) {
    return new ServerMessage("view", view, null, events, null, true);
  }

  public static ServerMessage rejected(String reason) {
    return new ServerMessage("rejected", null, reason, List.of(), null, false);
  }
}
