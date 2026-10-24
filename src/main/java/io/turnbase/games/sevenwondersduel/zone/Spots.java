package io.turnbase.games.sevenwondersduel.zone;

import io.turnbase.games.sevenwondersduel.id.PlayerId;

/** The public places effects move components to, named once. */
public final class Spots {

  public static final Location DISCARD = Location.of(Zones.DISCARD);
  public static final Location BOX = Location.of(Zones.BOX);

  private Spots() {}

  public static Location city(PlayerId player) {
    return new Location(Zones.CITY, player.value().toString());
  }
}
