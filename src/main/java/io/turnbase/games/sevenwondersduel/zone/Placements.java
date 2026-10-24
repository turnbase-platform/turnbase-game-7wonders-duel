package io.turnbase.games.sevenwondersduel.zone;

import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Pure operations over the list of {@link Placement}s. Every method returns a new list; the input
 * is never modified. Moving a component appends it to the end of its new zone, which is what gives
 * ordered zones (decks, piles) their order. Components that entered a terminal zone can never leave
 * it.
 */
public final class Placements {

  private Placements() {}

  /** Adds a component that is not yet placed anywhere. */
  public static List<Placement> place(
      List<Placement> all, ComponentId component, Location at, ZoneRegistry zones) {
    zones.kind(at.zone());
    if (find(all, component) >= 0) {
      throw new IllegalStateException("component already placed: " + component.value());
    }
    List<Placement> result = new ArrayList<>(all);
    result.add(new Placement(component, at));
    return List.copyOf(result);
  }

  /** Moves a placed component to {@code to}, appending it to the end of that zone. */
  public static List<Placement> move(
      List<Placement> all, ComponentId component, Location to, ZoneRegistry zones) {
    int index = find(all, component);
    if (index < 0) {
      throw new IllegalArgumentException("component is not placed: " + component.value());
    }
    zones.kind(to.zone());
    ZoneId from = all.get(index).at().zone();
    if (zones.kind(from).terminal()) {
      throw new IllegalStateException(
          "component " + component.value() + " is in terminal zone " + from.value());
    }
    List<Placement> result = new ArrayList<>(all);
    result.remove(index);
    result.add(new Placement(component, to));
    return List.copyOf(result);
  }

  /** Where {@code component} is placed. */
  public static Location locationOf(List<Placement> all, ComponentId component) {
    int index = find(all, component);
    if (index < 0) {
      throw new IllegalArgumentException("component is not placed: " + component.value());
    }
    return all.get(index).at();
  }

  /** The components in {@code zone}, in placement order. */
  public static List<ComponentId> idsIn(List<Placement> all, ZoneId zone) {
    return all.stream().filter(p -> p.at().zone().equals(zone)).map(Placement::component).toList();
  }

  /** The components at exactly {@code at}, in placement order. */
  public static List<ComponentId> idsIn(List<Placement> all, Location at) {
    return all.stream().filter(p -> p.at().equals(at)).map(Placement::component).toList();
  }

  private static int find(List<Placement> all, ComponentId component) {
    for (int i = 0; i < all.size(); i++) {
      if (all.get(i).component().equals(component)) {
        return i;
      }
    }
    return -1;
  }
}
