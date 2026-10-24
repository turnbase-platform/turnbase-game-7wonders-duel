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

  /**
   * The pawn one space closer to {@code target}'s capital (B-MIL-2): out along the target's side,
   * back along the other player's side, and onto the target's side from the centre. A pawn on a
   * capital stays there.
   *
   * @param target the player whose capital is approached
   */
  public Pawn stepToward(PlayerId target) {
    if (side == null) {
      return new Pawn(target, 1);
    }
    if (side.equals(target)) {
      return new Pawn(side, Math.min(space + 1, CAPITAL));
    }
    return space == 1 ? CENTRE : new Pawn(side, space - 1);
  }
}
