package io.turnbase.games.sevenwondersduel.state;

import io.turnbase.games.sevenwondersduel.id.PlayerId;

/**
 * How a game ended before scoring (B-END-1).
 *
 * @param winner the player who won
 * @param reason the supremacy that ended the game
 */
public record Victory(PlayerId winner, Reason reason) {

  /** The ways a game ends at once. Later plans add science and the score. */
  public enum Reason {
    MILITARY
  }

  public Victory {
    if (winner == null || reason == null) {
      throw new IllegalArgumentException("a victory needs a winner and a reason");
    }
  }
}
