package io.turnbase.games.sevenwondersduel.turn;

import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.id.ZoneId;
import io.turnbase.games.sevenwondersduel.zone.Location;
import io.turnbase.games.sevenwondersduel.zone.Zones;

/** Where the turn actions put things: one name per place, shared by every action. */
final class Places {

  static final Location DISCARD = Location.of(Zones.DISCARD);

  static final ZoneId UNBUILT = Zones.WONDERS_UNBUILT;
  static final ZoneId BUILT = Zones.WONDERS_BUILT;
  static final ZoneId DESTROYED = Zones.DESTROYED;
  private static final ZoneId UNDER_WONDER = Zones.UNDER_WONDER;

  private Places() {}

  static Location city(PlayerId player) {
    return new Location(Zones.CITY, player.value().toString());
  }

  static Location built(PlayerId player) {
    return new Location(BUILT, player.value().toString());
  }

  static Location unbuilt(PlayerId player) {
    return new Location(UNBUILT, player.value().toString());
  }

  /** The card a Wonder was built with lies face down beneath it. */
  static Location underWonder(ComponentId wonder) {
    return new Location(UNDER_WONDER, wonder.value());
  }
}
