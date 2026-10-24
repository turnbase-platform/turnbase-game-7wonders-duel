package io.turnbase.games.sevenwondersduel.state;

import io.turnbase.games.sevenwondersduel.id.PlayerId;

/**
 * The military pawn (B-MIL-1): how far it has moved from the centre and towards which player's
 * capital. Movement belongs to the military rules; this is only where it stands.
 *
 * @param side the player whose side of the track the pawn is on, null exactly at the centre
 * @param space distance from the centre, 0 to {@link #CAPITAL}
 */
public record Pawn(PlayerId side, int space) {

  /** The space of a capital; a pawn there ends the game. */
  public static final int CAPITAL = 9;

  /** The starting position (B-SET-1). */
  public static final Pawn CENTRE = new Pawn(null, 0);

  public Pawn {
    if (space < 0 || space > CAPITAL) {
      throw new IllegalArgumentException("pawn space outside the track: " + space);
    }
    if (space == 0 && side != null) {
      throw new IllegalArgumentException("a pawn at the centre has no side");
    }
    if (space > 0 && side == null) {
      throw new IllegalArgumentException("a pawn off the centre needs a side");
    }
  }
}
