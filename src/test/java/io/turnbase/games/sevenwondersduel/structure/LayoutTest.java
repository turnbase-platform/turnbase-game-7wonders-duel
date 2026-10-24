package io.turnbase.games.sevenwondersduel.structure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.turnbase.games.sevenwondersduel.catalog.BasePack;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.id.LayoutId;
import io.turnbase.games.sevenwondersduel.structure.Layout.Row;
import java.util.List;
import org.junit.jupiter.api.Test;

class LayoutTest {

  private static final RuleSet RULES = RuleSet.assemble(List.of(new BasePack()));

  private static Layout age(int n) {
    return RULES.layout(LayoutId.of("age" + n));
  }

  private static List<String> shape(Layout layout) {
    return layout.rows().stream().map(r -> r.xs().size() + (r.faceUp() ? "U" : "D")).toList();
  }

  @Test
  void everyBaseLayoutHasTwentySlots() {
    for (int n = 1; n <= 3; n++) {
      assertThat(age(n).size()).isEqualTo(20);
      assertThat(age(n).slots()).hasSize(20).doesNotHaveDuplicates();
    }
  }

  @Test
  void rowShapesMatchTheRulebook() {
    assertThat(shape(age(1))).containsExactly("2U", "3D", "4U", "5D", "6U");
    assertThat(shape(age(2))).containsExactly("6U", "5D", "4U", "3D", "2U");
    assertThat(shape(age(3))).containsExactly("2U", "3D", "4U", "2D", "4U", "3D", "2U");
  }

  @Test
  void rowPositionsMatchTheRulebookTable() {
    assertThat(xs(age(1)))
        .containsExactly(
            List.of(4, 6),
            List.of(3, 5, 7),
            List.of(2, 4, 6, 8),
            List.of(1, 3, 5, 7, 9),
            List.of(0, 2, 4, 6, 8, 10));
    assertThat(xs(age(2)))
        .containsExactly(
            List.of(0, 2, 4, 6, 8, 10),
            List.of(1, 3, 5, 7, 9),
            List.of(2, 4, 6, 8),
            List.of(3, 5, 7),
            List.of(4, 6));
    assertThat(xs(age(3)))
        .containsExactly(
            List.of(2, 4),
            List.of(1, 3, 5),
            List.of(0, 2, 4, 6),
            List.of(1, 5),
            List.of(0, 2, 4, 6),
            List.of(1, 3, 5),
            List.of(2, 4));
  }

  @Test
  void everySlotAboveTheBottomRowIsCoveredByTheRowBelow() {
    for (int n = 1; n <= 3; n++) {
      Layout layout = age(n);
      int bottom = layout.rows().size() - 1;
      for (Slot slot : layout.slots()) {
        if (slot.row() < bottom) {
          assertThat(layout.rows().get(slot.row() + 1).xs())
              .as("age %d slot %s", n, slot.key())
              .anyMatch(x -> Math.abs(x - slot.x()) == 1);
        }
      }
    }
  }

  private static List<List<Integer>> xs(Layout layout) {
    return layout.rows().stream().map(Row::xs).toList();
  }

  @Test
  void ageThreeGapRowSitsAtOneAndFive() {
    assertThat(age(3).rows().get(3).xs()).containsExactly(1, 5);
    assertThat(age(3).faceUp(new Slot(3, 1))).isFalse();
    assertThat(age(3).faceUp(new Slot(4, 0))).isTrue();
  }

  @Test
  void slotsRunTopToBottomLeftToRight() {
    assertThat(age(1).slots().subList(0, 3))
        .containsExactly(new Slot(0, 4), new Slot(0, 6), new Slot(1, 3));
  }

  @Test
  void slotKeyRoundTrips() {
    assertThat(new Slot(3, 7).key()).isEqualTo("3:7");
    assertThat(Slot.parse("3:7")).isEqualTo(new Slot(3, 7));
  }

  @Test
  void badSlotKeyIsRejectedByName() {
    assertThatThrownBy(() -> Slot.parse("nope"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("nope");
  }

  @Test
  void nonCanonicalSlotKeyIsRejected() {
    assertThatThrownBy(() -> Slot.parse("01:2")).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> Slot.parse("+1:2")).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void unsortedOrEmptyRowIsRejected() {
    assertThatThrownBy(() -> new Row(true, List.of(2, 1)))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new Row(true, List.of(1, 1)))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new Row(true, List.of())).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new Layout(LayoutId.of("x"), List.of()))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
