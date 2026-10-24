package io.turnbase.games.sevenwondersduel.view;

import io.turnbase.games.sevenwondersduel.agenda.ComponentOption;
import io.turnbase.games.sevenwondersduel.agenda.DecisionStep;
import io.turnbase.games.sevenwondersduel.agenda.PlayerOption;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.zone.Location;
import io.turnbase.games.sevenwondersduel.zone.Placement;
import io.turnbase.games.sevenwondersduel.zone.Placements;
import io.turnbase.games.sevenwondersduel.zone.Visibility;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** What each viewer may see of a game (base.md §10.8), derived from the state alone. */
public final class Views {

  private static final Comparator<Location> PLACE_ORDER =
      Comparator.comparing((Location l) -> l.zone().value()).thenComparing(Location::key);

  private Views() {}

  /** The game as {@code viewer} may see it. */
  public static DuelView of(DuelState state, RuleSet rules, Viewer viewer) {
    List<PlacedView> visible = new ArrayList<>();
    Map<Location, Integer> hidden = new TreeMap<>(PLACE_ORDER);
    for (Placement placement : state.placements()) {
      if (canSee(state, rules, viewer, placement.component())) {
        Location at = placement.at();
        visible.add(new PlacedView(placement.component().value(), at.zone().value(), at.key()));
      } else {
        hidden.merge(placement.at(), 1, Integer::sum);
      }
    }
    List<HiddenView> collapsed =
        hidden.entrySet().stream()
            .map(e -> new HiddenView(e.getKey().zone().value(), e.getKey().key(), e.getValue()))
            .toList();
    return new DuelView(
        state.phase().name(),
        state.active().value(),
        state.age(),
        state.first().value(),
        state.deciding() == null ? null : state.deciding().value(),
        players(state),
        state.pawn().side() == null ? null : state.pawn().side().value(),
        state.pawn().space(),
        state.victory() == null || state.victory().winner() == null
            ? null
            : state.victory().winner().value(),
        state.victory() == null ? null : state.victory().reason().name(),
        visible,
        collapsed,
        pending(state));
  }

  private static List<PlayerView> players(DuelState state) {
    return state.players().entrySet().stream()
        .map(
            e -> new PlayerView(e.getKey().value(), e.getValue().coins(), e.getValue().playAgain()))
        .sorted(Comparator.comparing(PlayerView::id))
        .toList();
  }

  private static PendingView pending(DuelState state) {
    if (state.agenda().isEmpty() || !(state.agenda().top() instanceof DecisionStep decision)) {
      return null;
    }
    List<String> options =
        decision.options().stream()
            .map(
                option ->
                    switch (option) {
                      case ComponentOption c -> c.component().value();
                      case PlayerOption p -> p.player().value().toString();
                    })
            .toList();
    return new PendingView(decision.decider().value(), options);
  }

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
