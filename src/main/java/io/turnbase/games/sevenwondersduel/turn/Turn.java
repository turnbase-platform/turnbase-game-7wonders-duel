package io.turnbase.games.sevenwondersduel.turn;

import io.turnbase.games.sevenwondersduel.effect.Resolution;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.id.ZoneId;
import io.turnbase.games.sevenwondersduel.structure.Structure;
import io.turnbase.games.sevenwondersduel.zone.Location;

/**
 * What happens around the action a player takes: the Discard action itself (B-ACT-2) and the end of
 * the turn that follows every action (B-TURN-3).
 */
public final class Turn {

  private static final Location DISCARD = Location.of(ZoneId.of("discard"));

  private Turn() {}

  /** The actor sells {@code card} for coins and the turn ends. The caller has checked legality. */
  public static void discard(Resolution resolution, PlayerId actor, ComponentId card) {
    int value = DiscardValue.of(resolution.rules(), resolution.state(), actor);
    resolution.move(card, DISCARD);
    resolution.changeCoins(actor, value);
    end(resolution);
  }

  /** Turns newly uncovered cards face up, then gives the move to the opponent. */
  public static void end(Resolution resolution) {
    Structure.revealUncovered(resolution);
    PlayerId next = resolution.state().opponentOf(resolution.state().active());
    resolution.advance(s -> s.withActive(next));
  }
}
