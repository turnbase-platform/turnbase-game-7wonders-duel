package io.turnbase.games.sevenwondersduel.turn;

import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.catalog.Tag;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.id.ZoneId;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.zone.Location;
import io.turnbase.games.sevenwondersduel.zone.Placements;
import io.turnbase.games.sevenwondersduel.zone.Zones;

/** What discarding a card for coins pays (B-ACT-2): 2 coins, plus 1 per yellow card owned. */
final class DiscardValue {

  static final int BASE = 2;
  private static final ZoneId CITY = Zones.CITY;
  private static final Tag YELLOW_CARD = Tag.color("yellow");

  private DiscardValue() {}

  static int of(RuleSet rules, DuelState state, PlayerId player) {
    Location city = new Location(CITY, player.value().toString());
    long yellow =
        Placements.idsIn(state.placements(), city).stream()
            .filter(id -> rules.catalog().get(id).tags().contains(YELLOW_CARD))
            .count();
    return BASE + (int) yellow;
  }
}
