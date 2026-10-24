package io.turnbase.games.sevenwondersduel.effect;

import io.turnbase.games.sevenwondersduel.agenda.Option;
import io.turnbase.games.sevenwondersduel.id.PlayerId;

/**
 * An instant effect: applied once and permanent (B-EFF-1). It changes the game only through the
 * {@link Resolution} primitives.
 */
public interface Effect {

  /**
   * Applies the effect.
   *
   * @param resolution the running resolution
   * @param owner the player who acquired the effect's source
   */
  void apply(Resolution resolution, PlayerId owner);

  /**
   * Continues after a decision this effect asked through {@link Resolution#decide}.
   *
   * @param resolution the running resolution
   * @param owner the player who acquired the effect's source
   * @param answer the option the decider picked, one of those offered
   */
  default void resume(Resolution resolution, PlayerId owner, Option answer) {
    throw new IllegalStateException("effect takes no answer: " + getClass().getSimpleName());
  }
}
