package io.turnbase.games.sevenwondersduel.age;

import io.turnbase.games.sevenwondersduel.effect.Resolution;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.scoring.Scoring;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import java.util.List;

/**
 * Ending an Age: the next Age begins, or the game is scored after the last one (B-AGE-1, B-AGE-3,
 * B-END-1).
 */
public final class AgeEnd {

  private AgeEnd() {}

  /**
   * Called when the last card of the structure has been taken, before the turn passes. A play-again
   * flag does not survive into another Age (B-TURN-5).
   */
  public static void finish(Resolution resolution) {
    DuelState state = resolution.state();
    PlayerId deciding = Deciding.forNextAge(state);
    for (PlayerId player : List.of(state.active(), state.opponentOf(state.active()))) {
      resolution.setPlayAgain(player, false);
    }
    int next = state.age() + 1;
    if (resolution.rules().ages().contains(next)) {
      AgeStart.begin(resolution, next, deciding);
    } else {
      Scoring.conclude(resolution);
    }
  }
}
