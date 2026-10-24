package io.turnbase.games.sevenwondersduel.cost;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CostTest {

  @Test
  void idsMustNotBeBlank() {
    assertThatThrownBy(() -> new Resource(" ")).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new ChainSymbol("")).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void baseResourcesAreDistinct() {
    assertThat(
            java.util.Set.of(
                Resource.WOOD, Resource.STONE, Resource.CLAY, Resource.GLASS, Resource.PAPYRUS))
        .hasSize(5);
  }

  @Test
  void ofCountsRepeatedResources() {
    Cost cost = Cost.of(2, Resource.CLAY, Resource.CLAY, Resource.GLASS);

    assertThat(cost.coins()).isEqualTo(2);
    assertThat(cost.resources()).containsEntry(Resource.CLAY, 2).containsEntry(Resource.GLASS, 1);
    assertThat(cost.symbols()).isEqualTo(3);
  }

  @Test
  void freeCostsNothing() {
    assertThat(Cost.FREE.coins()).isZero();
    assertThat(Cost.FREE.symbols()).isZero();
    assertThat(Cost.FREE.freedBy()).isEmpty();
  }

  @Test
  void withFreedByNamesTheChainSymbols() {
    ChainSymbol horseshoe = new ChainSymbol("horseshoe");

    assertThat(Cost.of(0, Resource.WOOD).withFreedBy(horseshoe).freedBy())
        .containsExactly(horseshoe);
  }

  @Test
  void negativeCoinsAndNonPositiveCountsAreRejected() {
    assertThatThrownBy(() -> Cost.coins(-1)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new Cost(0, Map.of(Resource.WOOD, 0), java.util.Set.of()))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new Cost(0, Map.of(Resource.WOOD, -1), java.util.Set.of()))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void costCopiesItsInputs() {
    Map<Resource, Integer> mutable = new HashMap<>(Map.of(Resource.WOOD, 1));
    Cost cost = new Cost(0, mutable, java.util.Set.of());

    mutable.put(Resource.WOOD, 5);

    assertThat(cost.resources()).containsEntry(Resource.WOOD, 1);
  }
}
