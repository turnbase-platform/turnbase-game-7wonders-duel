package io.turnbase.games.sevenwondersduel.content;

import static io.turnbase.games.sevenwondersduel.content.CardAssertions.named;
import static io.turnbase.games.sevenwondersduel.content.CardAssertions.priceOf;
import static org.assertj.core.api.Assertions.assertThat;

import io.turnbase.games.sevenwondersduel.catalog.Tag;
import io.turnbase.games.sevenwondersduel.cost.Cost;
import io.turnbase.games.sevenwondersduel.cost.Production;
import io.turnbase.games.sevenwondersduel.cost.Resource;
import io.turnbase.games.sevenwondersduel.effect.GainCoins;
import io.turnbase.games.sevenwondersduel.effect.Shields;
import io.turnbase.games.sevenwondersduel.science.ScienceSymbol;
import io.turnbase.games.sevenwondersduel.science.Symbol;
import io.turnbase.games.sevenwondersduel.scoring.Points;
import java.util.List;
import org.junit.jupiter.api.Test;

class AgeOneTest {

  private static final List<Card> CARDS = AgeOne.cards();

  @Test
  void twentyThreeCardsOfAgeOneWithTheBrownBack() {
    assertThat(CARDS).hasSize(23);
    assertThat(CARDS).allSatisfy(card -> assertThat(card.tags()).contains(Tag.age(1)));
    assertThat(CARDS).allSatisfy(card -> assertThat(card.back()).contains(Backs.BROWN));
  }

  @Test
  void brownAndGreyCardsProduceAndRaiseTheOpponentsPrice() {
    assertThat(named(CARDS, "Lumber Yard").cost()).isEqualTo(Cost.FREE);
    assertThat(named(CARDS, "Lumber Yard").modifiers())
        .containsExactly(Production.fixed(Resource.WOOD, 1, true));
    assertThat(named(CARDS, "Logging Camp").cost()).isEqualTo(Cost.coins(1));
    assertThat(named(CARDS, "Glassworks").tags()).contains(Colors.GREY);
    assertThat(named(CARDS, "Glassworks").modifiers())
        .containsExactly(Production.fixed(Resource.GLASS, 1, true));
  }

  @Test
  void redCardsPushOneShieldAndSomeGiveAChain() {
    Card stable = named(CARDS, "Stable");
    assertThat(stable.cost()).isEqualTo(Cost.of(0, Resource.WOOD));
    assertThat(stable.effects()).containsExactly(new Shields(1));
    assertThat(stable.modifiers()).containsExactly(Chains.gives(Chains.HORSESHOE));
    assertThat(named(CARDS, "Guard Tower").modifiers()).isEmpty();
    assertThat(named(CARDS, "Palisade").modifiers()).containsExactly(Chains.gives(Chains.FORTRESS));
  }

  @Test
  void greenCardsShowASymbolAndSomeScoreOnePoint() {
    Card workshop = named(CARDS, "Workshop");
    assertThat(workshop.cost()).isEqualTo(Cost.of(0, Resource.PAPYRUS));
    assertThat(workshop.modifiers())
        .hasSize(2)
        .contains(new Symbol(ScienceSymbol.PLUMB_BOB))
        .anyMatch(Points.class::isInstance);
    assertThat(named(CARDS, "Scriptorium").modifiers())
        .containsExactly(new Symbol(ScienceSymbol.QUILL), Chains.gives(Chains.BOOK));
  }

  @Test
  void blueCardsScoreThreeAndGiveTheirChain() {
    Card baths = named(CARDS, "Baths");
    assertThat(baths.cost()).isEqualTo(Cost.of(0, Resource.STONE));
    assertThat(baths.modifiers()).hasSize(2).contains(Chains.gives(Chains.WATER));
    assertThat(named(CARDS, "Theater").modifiers()).contains(Chains.gives(Chains.THEATER));
    assertThat(named(CARDS, "Altar").modifiers()).contains(Chains.gives(Chains.MOON));
  }

  @Test
  void reservesFixTheirResourceAtOneCoin() {
    Card reserve = named(CARDS, "Stone Reserve");
    assertThat(reserve.cost()).isEqualTo(Cost.coins(3));
    assertThat(priceOf(reserve, Resource.STONE)).isEqualTo(1);
    assertThat(priceOf(reserve, Resource.WOOD)).isEqualTo(2);
  }

  @Test
  void tavernPaysFourCoinsAndGivesTheVase() {
    Card tavern = named(CARDS, "Tavern");
    assertThat(tavern.cost()).isEqualTo(Cost.FREE);
    assertThat(tavern.effects()).containsExactly(new GainCoins(4));
    assertThat(tavern.modifiers()).containsExactly(Chains.gives(Chains.VASE));
  }

  @Test
  void ageOneIsFreedByNoChain() {
    assertThat(CARDS).allSatisfy(card -> assertThat(card.cost().freedBy()).isEmpty());
  }
}
