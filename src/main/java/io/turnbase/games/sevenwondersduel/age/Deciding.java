package io.turnbase.games.sevenwondersduel.age;

import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.state.DuelState;

/** Who chooses the starting player of the next Age (B-AGE-3). */
final class Deciding {

  private Deciding() {}

  /**
   * The player the military pawn is on the side of, the weaker one; with the pawn at the centre,
   * the player who acted last, who is still the active player when an Age ends.
   */
  static PlayerId forNextAge(DuelState state) {
    PlayerId weaker = state.pawn().side();
    return weaker != null ? weaker : state.active();
  }
}
