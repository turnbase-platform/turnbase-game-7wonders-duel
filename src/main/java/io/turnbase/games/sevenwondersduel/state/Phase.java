package io.turnbase.games.sevenwondersduel.state;

/** Coarse stage of the game; each stage owns its own turn rules. */
public enum Phase {
  SETUP,
  WONDER_DRAFT,
  AGE,
  OVER
}
