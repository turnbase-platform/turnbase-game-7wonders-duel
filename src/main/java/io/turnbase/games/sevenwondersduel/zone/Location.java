package io.turnbase.games.sevenwondersduel.zone;

import io.turnbase.games.sevenwondersduel.id.ZoneId;

/**
 * A place within a zone, e.g. {@code (city, <player uuid>)} or {@code (structure.up, "3:4")}.
 *
 * @param zone the zone
 * @param key the slot within the zone, empty when the zone has no sub-places
 */
public record Location(ZoneId zone, String key) {

  public Location {
    if (zone == null || key == null) {
      throw new IllegalArgumentException("zone and key must not be null");
    }
  }

  public static Location of(ZoneId zone) {
    return new Location(zone, "");
  }
}
