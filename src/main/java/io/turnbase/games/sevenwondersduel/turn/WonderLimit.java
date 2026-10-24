package io.turnbase.games.sevenwondersduel.turn;

import io.turnbase.games.sevenwondersduel.effect.Resolution;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.ZoneId;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.zone.Location;
import io.turnbase.games.sevenwondersduel.zone.Placement;
import java.util.List;

/**
 * At most seven Wonders are built in a game; building the seventh destroys the Wonders still
 * unbuilt (B-ACT-5).
 */
final class WonderLimit {

  static final int MAX = 7;

  private WonderLimit() {}

  static int built(DuelState state) {
    return (int) state.placements().stream().filter(p -> inZone(p, Places.BUILT)).count();
  }

  static boolean allows(DuelState state) {
    return built(state) < MAX;
  }

  /** Destroys every unbuilt Wonder, each recorded under its former owner, once the limit is hit. */
  static void enforce(Resolution resolution) {
    if (built(resolution.state()) < MAX) {
      return;
    }
    List<Placement> left =
        resolution.state().placements().stream().filter(p -> inZone(p, Places.UNBUILT)).toList();
    for (Placement p : left) {
      ComponentId wonder = p.component();
      resolution.move(wonder, new Location(Places.DESTROYED, p.at().key()));
    }
  }

  private static boolean inZone(Placement placement, ZoneId zone) {
    return placement.at().zone().equals(zone);
  }
}
