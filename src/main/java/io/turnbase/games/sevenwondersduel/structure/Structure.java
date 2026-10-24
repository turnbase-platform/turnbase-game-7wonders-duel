package io.turnbase.games.sevenwondersduel.structure;

import io.turnbase.games.sevenwondersduel.effect.Resolution;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.ZoneId;
import io.turnbase.games.sevenwondersduel.zone.Location;
import io.turnbase.games.sevenwondersduel.zone.Placement;
import io.turnbase.games.sevenwondersduel.zone.Placements;
import io.turnbase.games.sevenwondersduel.zone.ZoneRegistry;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Operations on the card structure of the current Age, all derived from the placements: a slot is
 * covered while an occupied slot of the next row down has an {@code x} exactly 1 away (base.md
 * §5.2), so no layout and no stored flag is needed after the deal.
 */
public final class Structure {

  public static final ZoneId UP = ZoneId.of("structure.up");
  public static final ZoneId DOWN = ZoneId.of("structure.down");

  private static final Comparator<Slot> ORDER =
      Comparator.comparingInt(Slot::row).thenComparingInt(Slot::x);

  private Structure() {}

  private static final ZoneId DECK = ZoneId.of("deck");

  /**
   * Lays {@code cards} out in {@code layout}, card {@code i} at slot {@code i}, face up or down as
   * the layout says (B-STR-1).
   */
  public static List<Placement> deal(
      List<Placement> all, Layout layout, List<ComponentId> cards, ZoneRegistry zones) {
    if (cards.size() != layout.size()) {
      throw new IllegalArgumentException(
          "layout "
              + layout.id().value()
              + " needs "
              + layout.size()
              + " cards, got "
              + cards.size());
    }
    if (!occupied(all).isEmpty()) {
      throw new IllegalArgumentException("the structure is not empty");
    }
    java.util.Set<ComponentId> seen = new java.util.HashSet<>();
    for (ComponentId card : cards) {
      if (!seen.add(card)) {
        throw new IllegalArgumentException("card is dealt twice: " + card.value());
      }
      if (!Placements.locationOf(all, card).zone().equals(DECK)) {
        throw new IllegalArgumentException("card is not in the deck: " + card.value());
      }
    }
    List<Slot> slots = layout.slots();
    List<Placement> result = all;
    for (int i = 0; i < slots.size(); i++) {
      Slot slot = slots.get(i);
      result =
          Placements.move(
              result, cards.get(i), slot.location(layout.faceUp(slot) ? UP : DOWN), zones);
    }
    return result;
  }

  /** Occupied slots with nothing covering them, by row then x. */
  public static List<Slot> uncovered(List<Placement> all) {
    java.util.Set<Slot> slots = occupied(all).keySet();
    return slots.stream().filter(slot -> !isCovered(slot, slots)).toList();
  }

  /** Face-up cards on uncovered slots, by row then x: what a player may take. */
  public static List<ComponentId> takeable(List<Placement> all) {
    Map<Slot, Placement> occupied = occupied(all);
    return uncovered(all).stream()
        .map(occupied::get)
        .filter(p -> p.at().zone().equals(UP))
        .map(Placement::component)
        .toList();
  }

  /**
   * Turns every face-down card on an uncovered slot face up, by row then x (B-STR-2). The slots are
   * worked out once, before any card moves.
   */
  public static void revealUncovered(Resolution resolution) {
    List<Placement> all = resolution.state().placements();
    Map<Slot, Placement> occupied = occupied(all);
    for (Slot slot : uncovered(all)) {
      Placement placement = occupied.get(slot);
      if (placement.at().zone().equals(DOWN)) {
        resolution.move(placement.component(), slot.location(UP));
      }
    }
  }

  private static boolean isCovered(Slot slot, java.util.Set<Slot> occupied) {
    return occupied.contains(new Slot(slot.row() + 1, slot.x() - 1))
        || occupied.contains(new Slot(slot.row() + 1, slot.x() + 1));
  }

  private static Map<Slot, Placement> occupied(List<Placement> all) {
    Map<Slot, Placement> slots = new TreeMap<>(ORDER);
    for (Placement p : all) {
      Location at = p.at();
      if (at.zone().equals(UP) || at.zone().equals(DOWN)) {
        if (slots.put(Slot.parse(at.key()), p) != null) {
          throw new IllegalStateException("two cards share the structure slot " + at.key());
        }
      }
    }
    return slots;
  }
}
