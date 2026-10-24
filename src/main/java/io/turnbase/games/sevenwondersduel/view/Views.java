package io.turnbase.games.sevenwondersduel.view;

import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.zone.Location;
import io.turnbase.games.sevenwondersduel.zone.Placements;
import io.turnbase.games.sevenwondersduel.zone.Visibility;

/** What each viewer may see of a game (base.md §10.8), derived from the state alone. */
public final class Views {

  private Views() {}

  /**
   * Whether {@code viewer} may know which component {@code component} is: it lies in a public zone,
   * or in an owner zone of the viewer, or the viewer has seen it before.
   */
  public static boolean canSee(
      DuelState state, RuleSet rules, Viewer viewer, ComponentId component) {
    Location at = Placements.locationOf(state.placements(), component);
    Visibility visibility = rules.zones().kind(at.zone()).visibility();
    if (visibility == Visibility.PUBLIC) {
      return true;
    }
    if (!(viewer instanceof Viewer.Seat seat)) {
      return false;
    }
    if (visibility == Visibility.OWNER && at.key().equals(seat.player().value().toString())) {
      return true;
    }
    return state.knowledge().getOrDefault(seat.player(), java.util.Set.of()).contains(component);
  }
}
