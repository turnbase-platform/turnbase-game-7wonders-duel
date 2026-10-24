package io.turnbase.games.sevenwondersduel.zone;

/** Who may see the identity of a component in a zone (base.md §10.8). */
public enum Visibility {
  /** Both players. */
  PUBLIC,
  /** Only the player the zone belongs to. */
  OWNER,
  /** Nobody; the identity is hidden from both players. */
  NONE
}
