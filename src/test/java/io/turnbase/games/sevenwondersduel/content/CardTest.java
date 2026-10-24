package io.turnbase.games.sevenwondersduel.content;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.turnbase.games.sevenwondersduel.catalog.ComponentType;
import io.turnbase.games.sevenwondersduel.catalog.Tag;
import io.turnbase.games.sevenwondersduel.cost.ChainSource;
import io.turnbase.games.sevenwondersduel.cost.Cost;
import io.turnbase.games.sevenwondersduel.cost.Resource;
import io.turnbase.games.sevenwondersduel.effect.Shields;
import org.junit.jupiter.api.Test;

class CardTest {

  private static Card stable() {
    return Card.of("Glass-blower Guild", 2, Colors.RED, Backs.BLUE);
  }

  @Test
  void aCardIsAComponentWithSluggedIdAgeColourAndBack() {
    Card card = stable();

    assertThat(card.id().value()).isEqualTo("glass-blower-guild");
    assertThat(card.name()).isEqualTo("Glass-blower Guild");
    assertThat(card.type()).isEqualTo(ComponentType.CARD);
    assertThat(card.tags()).containsExactlyInAnyOrder(Tag.age(2), Colors.RED);
    assertThat(card.back()).contains(Backs.BLUE);
    assertThat(card.cost()).isEqualTo(Cost.FREE);
  }

  @Test
  void costingEffectsAndModifiersAreAddedWithoutChangingTheCard() {
    Card base = stable();
    Card built =
        base.costing(Cost.of(0, Resource.WOOD))
            .effects(new Shields(1))
            .modifiers(Chains.gives(Chains.SWORD));

    assertThat(base.effects()).isEmpty();
    assertThat(built.cost()).isEqualTo(Cost.of(0, Resource.WOOD));
    assertThat(built.effects()).containsExactly(new Shields(1));
    assertThat(built.modifiers()).containsExactly(new ChainSource(java.util.Set.of(Chains.SWORD)));
  }

  @Test
  void freedByKeepsTheCostAndAddsTheChainSymbols() {
    Card card = stable().costing(Cost.coins(3)).freedBy(Chains.SWORD);

    assertThat(card.cost()).isEqualTo(Cost.coins(3).withFreedBy(Chains.SWORD));
  }

  @Test
  void aCardShowingAScienceSymbolChecksScienceWhenBuilt() {
    Card card = stable().showing(io.turnbase.games.sevenwondersduel.science.ScienceSymbol.WHEEL);

    assertThat(card.modifiers())
        .containsExactly(
            new io.turnbase.games.sevenwondersduel.science.Symbol(
                io.turnbase.games.sevenwondersduel.science.ScienceSymbol.WHEEL));
    assertThat(card.effects())
        .containsExactly(new io.turnbase.games.sevenwondersduel.effect.ScienceCheck());
  }

  @Test
  void aBlankNameIsRejected() {
    assertThatThrownBy(() -> Card.of(" ", 1, Colors.RED, Backs.BROWN))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void theBaseColoursAreTheSevenPrintedOnes() {
    assertThat(Colors.ALL)
        .extracting(Tag::value)
        .containsExactly(
            "color:brown",
            "color:grey",
            "color:blue",
            "color:green",
            "color:yellow",
            "color:red",
            "color:purple");
  }
}
