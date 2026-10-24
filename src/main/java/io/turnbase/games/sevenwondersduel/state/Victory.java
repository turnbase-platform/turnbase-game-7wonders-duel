package io.turnbase.games.sevenwondersduel.state;

import io.turnbase.games.sevenwondersduel.id.PlayerId;

/**
 * How a game ended (B-END-1..3).
 *
 * @param winner the player who won; {@code null} only for a shared victory on points (B-END-3)
 * @param reason what ended the game
 */
public record Victory(PlayerId winner, Reason reason) {

  /** The ways a game ends: a supremacy at once, or the score after Age III. */
  public enum Reason {
    MILITARY,
    SCIENCE,
    POINTS,
    /** The other player left the game; the opponent wins. */
    FORFEIT
  }

  public Victory {
    if (reason == null) {
      throw new IllegalArgumentException("a victory needs a reason");
    }
    if (winner == null && reason != Reason.POINTS) {
      throw new IllegalArgumentException("only a victory on points can be shared");
    }
  }
}
