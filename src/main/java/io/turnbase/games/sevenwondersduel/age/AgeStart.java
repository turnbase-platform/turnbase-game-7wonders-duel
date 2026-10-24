package io.turnbase.games.sevenwondersduel.age;

import io.turnbase.games.sevenwondersduel.effect.Resolution;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.setup.Setup;
import io.turnbase.games.sevenwondersduel.state.Phase;
import io.turnbase.games.sevenwondersduel.structure.Structure;
import io.turnbase.games.sevenwondersduel.zone.Placements;

/** Starting an Age: set up its structure (B-AGE-2) and ask the deciding player who starts. */
public final class AgeStart {

  private AgeStart() {}

  /**
   * Deals the deck of {@code age} into that Age's layout, turns the uncovered face-down cards up
   * and hands the choice of the starting player to {@code deciding} (B-AGE-0).
   */
  public static void begin(Resolution resolution, int age, PlayerId deciding) {
    Structure.deal(
        resolution,
        resolution.rules().layoutForAge(age),
        Placements.idsIn(resolution.state().placements(), Setup.ageDeck(age)));
    Structure.revealUncovered(resolution);
    resolution.advance(s -> s.withPhase(Phase.AGE).withAge(age).withDeciding(deciding));
  }
}
