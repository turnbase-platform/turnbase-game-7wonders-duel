package io.turnbase.games.sevenwondersduel.turn;

import io.turnbase.games.sevenwondersduel.age.AgeEnd;
import io.turnbase.games.sevenwondersduel.agenda.Agenda;
import io.turnbase.games.sevenwondersduel.agenda.TurnEndStep;
import io.turnbase.games.sevenwondersduel.effect.Resolution;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.state.Phase;
import io.turnbase.games.sevenwondersduel.structure.Structure;
import io.turnbase.games.sevenwondersduel.zone.Spots;

/**
 * What happens around the action a player takes: the Discard action itself (B-ACT-2) and the end of
 * the turn that follows every action (B-TURN-3).
 */
public final class Turn {

  private Turn() {}

  /** The actor sells {@code card} for coins and the turn ends. The caller has checked legality. */
  public static void discard(Resolution resolution, PlayerId actor, ComponentId card) {
    int value = DiscardValue.of(resolution.rules(), resolution.state(), actor);
    resolution.move(card, Spots.DISCARD);
    resolution.changeCoins(actor, value);
    end(resolution);
  }

  /**
   * Applies pending effects until the agenda is empty or waits for a decision. If the action's
   * {@link TurnEndStep} is then on top, the action is done and the turn ends.
   */
  public static void settle(Resolution resolution) {
    resolution.drain();
    Agenda agenda = resolution.state().agenda();
    if (!agenda.isEmpty() && agenda.top() instanceof TurnEndStep) {
      resolution.advance(s -> s.withAgenda(s.agenda().pop()));
      end(resolution);
    }
  }

  /**
   * Turns newly uncovered cards face up, ends the Age if the structure is empty (B-AGE-1), else
   * gives the move to the opponent, unless the active player has a play-again flag, which is used
   * up (B-TURN-4, B-TURN-6).
   */
  public static void end(Resolution resolution) {
    if (resolution.state().phase() == Phase.OVER) {
      return;
    }
    if (!resolution.state().agenda().isEmpty()) {
      throw new IllegalStateException("a turn cannot end while steps are pending");
    }
    Structure.revealUncovered(resolution);
    if (Structure.isEmpty(resolution.state().placements())) {
      AgeEnd.finish(resolution);
      return;
    }
    PlayerId active = resolution.state().active();
    if (resolution.state().player(active).playAgain()) {
      resolution.setPlayAgain(active, false);
      return;
    }
    PlayerId next = resolution.state().opponentOf(resolution.state().active());
    resolution.advance(s -> s.withActive(next));
  }
}
