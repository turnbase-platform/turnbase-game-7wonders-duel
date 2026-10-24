package io.turnbase.games.sevenwondersduel.turn;

import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.id.ZoneId;
import io.turnbase.games.sevenwondersduel.zone.Location;

/** Where the turn actions put things: one name per place, shared by every action. */
final class Places {

  static final Location DISCARD = Location.of(ZoneId.of("discard"));

  static final ZoneId UNBUILT = ZoneId.of("wonders.unbuilt");
  static final ZoneId BUILT = ZoneId.of("wonders.built");
  static final ZoneId DESTROYED = ZoneId.of("destroyed");
  private static final ZoneId UNDER_WONDER = ZoneId.of("underWonder");

  private Places() {}

  static Location city(PlayerId player) {
    return new Location(ZoneId.of("city"), player.value().toString());
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
