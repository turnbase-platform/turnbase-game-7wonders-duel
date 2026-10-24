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
