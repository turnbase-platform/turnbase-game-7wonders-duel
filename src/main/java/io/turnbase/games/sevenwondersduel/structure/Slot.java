package io.turnbase.games.sevenwondersduel.structure;

import io.turnbase.games.sevenwondersduel.id.ZoneId;
import io.turnbase.games.sevenwondersduel.zone.Location;

/**
 * A position in the card structure (B-STR-1): a row counted from the top and a horizontal position
 * {@code x} in half-card units. Its {@link #key()} is the {@link Location} key in the structure
 * zones, so the geometry travels with the card.
 */
public record Slot(int row, int x) {

  public String key() {
    return row + ":" + x;
  }

  public Location location(ZoneId zone) {
    return new Location(zone, key());
  }

  public static Slot parse(String key) {
    String[] parts = key.split(":", -1);
    try {
      if (parts.length == 2) {
        return new Slot(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
      }
    } catch (NumberFormatException e) {
      // falls through to the named error
    }
    throw new IllegalArgumentException("not a structure slot key: " + key);
  }
}
