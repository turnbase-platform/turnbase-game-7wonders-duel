package io.turnbase.games.sevenwondersduel.playthrough;

import io.turnbase.games.sevenwondersduel.catalog.Component;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.catalog.Tag;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.zone.Placement;
import io.turnbase.games.sevenwondersduel.zone.Placements;
import io.turnbase.games.sevenwondersduel.zone.Zones;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Properties that hold in every state of every game, whatever the players do. */
final class Invariants {

  private static final int MAX_WONDERS = 7;

  private Invariants() {}

  /** Checks one state; {@code previous} is the state one action earlier, or null. */
  static void check(RuleSet rules, DuelState previous, DuelState state) {
    conservation(rules, state);
    for (var entry : state.players().entrySet()) {
      require(entry.getValue().coins() >= 0, "negative coins for " + entry.getKey());
    }
    require(
        Placements.idsIn(state.placements(), Zones.WONDERS_BUILT).size() <= MAX_WONDERS,
        "more than " + MAX_WONDERS + " Wonders built");
    if (previous != null) {
      onlyGrows(rules, previous, state, Zones.BOX.value(), true);
      onlyGrows(rules, previous, state, Zones.DESTROYED.value(), false);
    }
  }

  private static void conservation(RuleSet rules, DuelState state) {
    Set<ComponentId> expected = new HashSet<>();
    for (var type : io.turnbase.games.sevenwondersduel.catalog.ComponentType.values()) {
      rules.catalog().ofType(type).stream().map(Component::id).forEach(expected::add);
    }
    List<ComponentId> placed = state.placements().stream().map(Placement::component).toList();
    require(placed.size() == expected.size(), "placed " + placed.size() + " of " + expected.size());
    require(new HashSet<>(placed).equals(expected), "placed ids differ from the catalogue");
  }

  /** Nothing leaves {@code zone}, except progress tokens when the Great Library draws them. */
  private static void onlyGrows(
      RuleSet rules, DuelState before, DuelState after, String zone, boolean tokensMayLeave) {
    Set<ComponentId> gone = idsIn(before, zone);
    gone.removeAll(idsIn(after, zone));
    if (tokensMayLeave) {
      gone.removeIf(id -> rules.catalog().get(id).tags().contains(Tag.PROGRESS));
    }
    require(gone.isEmpty(), "components left the " + zone + " zone: " + gone);
  }

  private static Set<ComponentId> idsIn(DuelState state, String zone) {
    Set<ComponentId> ids = new HashSet<>();
    for (Placement p : state.placements()) {
      if (p.at().zone().value().equals(zone)) {
        ids.add(p.component());
      }
    }
    return ids;
  }

  private static void require(boolean ok, String message) {
    if (!ok) {
      throw new AssertionError(message);
    }
  }
}
