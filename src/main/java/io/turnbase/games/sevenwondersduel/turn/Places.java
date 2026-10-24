package io.turnbase.games.sevenwondersduel.turn;

import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.id.ZoneId;
import io.turnbase.games.sevenwondersduel.zone.Location;

/** Where the turn actions put things: one name per place, shared by every action. */
final class Places {

  static final Location DISCARD = Location.of(ZoneId.of("discard"));

  private Places() {}

  static Location city(PlayerId player) {
    return new Location(ZoneId.of("city"), player.value().toString());
  }
}
