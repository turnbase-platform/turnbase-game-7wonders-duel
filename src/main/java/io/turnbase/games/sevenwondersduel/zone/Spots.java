package io.turnbase.games.sevenwondersduel.zone;

import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PlayerId;

/** The places effects and turn actions move components to, named once. */
public final class Spots {

  public static final Location DISCARD = Location.of(Zones.DISCARD);
  public static final Location BOX = Location.of(Zones.BOX);

  private Spots() {}

  public static Location city(PlayerId player) {
    return new Location(Zones.CITY, player.value().toString());
  }

  public static Location built(PlayerId player) {
    return new Location(Zones.WONDERS_BUILT, player.value().toString());
  }

  public static Location unbuilt(PlayerId player) {
    return new Location(Zones.WONDERS_UNBUILT, player.value().toString());
  }

  /** Where the Wonders {@code player} never built end up (B-ACT-5). */
  public static Location destroyed(PlayerId owner) {
    return new Location(Zones.DESTROYED, owner.value().toString());
  }

  /** The card a Wonder was built with lies face down beneath it. */
  public static Location underWonder(ComponentId wonder) {
    return new Location(Zones.UNDER_WONDER, wonder.value());
  }
}
