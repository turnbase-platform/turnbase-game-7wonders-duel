package io.turnbase.games.sevenwondersduel.structure;

import io.turnbase.games.sevenwondersduel.id.LayoutId;
import java.util.ArrayList;
import java.util.List;

/**
 * How one Age's cards are laid out (B-STR-1): rows from the top down, each row face up or face
 * down, each card at an {@code x} position in half-card units (base.md §5.2).
 *
 * @param id the layout id
 * @param rows rows top to bottom; the bottom row is accessible first
 */
public record Layout(LayoutId id, List<Row> rows) {

  /**
   * One row of a layout.
   *
   * @param faceUp whether the cards of the row are dealt face up
   * @param xs the x positions, strictly increasing
   */
  public record Row(boolean faceUp, List<Integer> xs) {

    public Row {
      xs = List.copyOf(xs);
      if (xs.isEmpty()) {
        throw new IllegalArgumentException("a row needs at least one slot");
      }
      for (int i = 1; i < xs.size(); i++) {
        if (xs.get(i) <= xs.get(i - 1)) {
          throw new IllegalArgumentException("row positions must be strictly increasing: " + xs);
        }
      }
    }
  }

  public Layout {
    rows = List.copyOf(rows);
    if (rows.isEmpty()) {
      throw new IllegalArgumentException("a layout needs at least one row");
    }
  }

  /** Every slot, top to bottom and left to right: the order cards are dealt in. */
  public List<Slot> slots() {
    List<Slot> slots = new ArrayList<>();
    for (int row = 0; row < rows.size(); row++) {
      for (int x : rows.get(row).xs()) {
        slots.add(new Slot(row, x));
      }
    }
    return List.copyOf(slots);
  }

  public int size() {
    return rows.stream().mapToInt(r -> r.xs().size()).sum();
  }

  /** Whether the card dealt to {@code slot} starts face up. */
  public boolean faceUp(Slot slot) {
    return rows.get(slot.row()).faceUp();
  }
}
