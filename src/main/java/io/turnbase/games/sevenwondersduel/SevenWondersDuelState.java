package io.turnbase.games.sevenwondersduel;

import io.turnbase.gdk.GameState;
import java.util.UUID;

/**
 * The whole game. A value type — records, {@code List}, {@code String} and {@code UUID} only — so
 * it round-trips through the platform's serializer (backend-gdk-api.md §5.1).
 *
 * <p>Skeleton: two seats, whose turn it is and a turn counter. The cards, wonders, military track,
 * science tokens and coins belong here.
 */
public record SevenWondersDuelState(
    UUID firstPlayer,
    UUID firstTeam,
    UUID secondPlayer,
    UUID secondTeam,
    UUID turn,
    int turnNumber,
    boolean over)
    implements GameState {

  /** The seat opposite the given player. */
  UUID other(UUID player) {
    return player.equals(firstPlayer) ? secondPlayer : firstPlayer;
  }

  UUID teamOf(UUID player) {
    return player.equals(firstPlayer) ? firstTeam : secondTeam;
  }

  boolean isSeated(UUID player) {
    return player.equals(firstPlayer) || player.equals(secondPlayer);
  }
}
