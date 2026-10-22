package io.turnbase.games.sevenwondersduel;

import java.util.UUID;

/**
 * The event payloads this game sends to its frontend. {@code type} discriminates them, so the
 * frontend's {@code TEvent} is a discriminated union (frontend-gdk-api.md §3).
 */
public final class GameMessage {

  private GameMessage() {}

  /** The full position, sent as the first event to every viewer and again on reconnection. */
  public record View(String type, UUID turn, int turnNumber, boolean over) {
    static View of(SevenWondersDuelState s) {
      return new View("view", s.turn(), s.turnNumber(), s.over());
    }
  }

  /** One accepted turn, broadcast to everyone. */
  public record TurnEnded(String type, UUID by, UUID next, int turnNumber) {
    static TurnEnded of(UUID by, UUID next, int turnNumber) {
      return new TurnEnded("turn", by, next, turnNumber);
    }
  }

  /** A refused action, sent to the actor alone. The game state is unchanged. */
  public record Rejected(String type, String reason) {
    static Rejected of(String reason) {
      return new Rejected("rejected", reason);
    }
  }

  /** The final result, delivered as the game-over results payload. */
  public record Result(String type, UUID winner, String reason) {
    static Result of(UUID winner, String reason) {
      return new Result("result", winner, reason);
    }
  }
}
