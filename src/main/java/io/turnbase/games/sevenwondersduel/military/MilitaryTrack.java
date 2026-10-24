package io.turnbase.games.sevenwondersduel.military;

import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.id.ZoneId;
import io.turnbase.games.sevenwondersduel.state.Pawn;
import io.turnbase.games.sevenwondersduel.zone.Location;

/**
 * The geometry of the military track (B-MIL-1): which spaces form a zone, and where the token of a
 * zone lies. Each side of the track belongs to one player and has three token zones; the ninth
 * space is that player's capital.
 */
public final class MilitaryTrack {

  /** Where unspent military tokens lie, keyed by {@link #tokenSpot}. */
  public static final ZoneId TOKENS = ZoneId.of("military");

  /** The zone number of a capital space. */
  public static final int CAPITAL_ZONE = 4;

  /** The first space of zones 1, 2 and 3 (the capital starts zone 4). */
  private static final int[] ZONE_STARTS = {1, 3, 6, Pawn.CAPITAL};

  /**
   * The zone of {@code space} on one side: 0 for the centre, 1-3 for token zones, 4 the capital.
   */
  public static int zoneOf(int space) {
    int zone = 0;
    for (int start : ZONE_STARTS) {
      if (space >= start) {
        zone++;
      }
    }
    return zone;
  }

  /** Where the token of {@code zone} on {@code side}'s part of the track lies. */
  public static Location tokenSpot(PlayerId side, int zone) {
    return new Location(TOKENS, side.value() + ":" + zone);
  }

  private MilitaryTrack() {}
}
