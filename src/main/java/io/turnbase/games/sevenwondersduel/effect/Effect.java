package io.turnbase.games.sevenwondersduel.effect;

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
}
