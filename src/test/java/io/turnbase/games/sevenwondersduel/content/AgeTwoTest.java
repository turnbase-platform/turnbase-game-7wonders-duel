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
import java.util.List;
import org.junit.jupiter.api.Test;

class AgeTwoTest {

  private static final List<Card> CARDS = AgeTwo.cards();

  @Test
  void twentyThreeCardsOfAgeTwoWithTheBlueBack() {
    assertThat(CARDS).hasSize(23);
    assertThat(CARDS).allSatisfy(card -> assertThat(card.tags()).contains(Tag.age(2)));
    assertThat(CARDS).allSatisfy(card -> assertThat(card.back()).contains(Backs.BLUE));
  }

  @Test
  void doubleProductionCostsTwoCoins() {
    Card sawmill = named(CARDS, "Sawmill");
    assertThat(sawmill.cost()).isEqualTo(Cost.coins(2));
    assertThat(sawmill.modifiers()).containsExactly(Production.fixed(Resource.WOOD, 2, true));
    assertThat(named(CARDS, "Glass-blower").cost()).isEqualTo(Cost.FREE);
    assertThat(named(CARDS, "Drying Room").modifiers())
        .containsExactly(Production.fixed(Resource.PAPYRUS, 1, true));
  }

  @Test
  void forumAndCaravanseryProduceAChoiceThatDoesNotRaiseThePrice() {
    Card forum = named(CARDS, "Forum");
    assertThat(forum.cost()).isEqualTo(Cost.of(3, Resource.CLAY));
    assertThat(forum.modifiers())
        .containsExactly(Production.choice(false, Resource.GLASS, Resource.PAPYRUS));
    Card caravansery = named(CARDS, "Caravansery");
    assertThat(caravansery.cost()).isEqualTo(Cost.of(2, Resource.GLASS, Resource.PAPYRUS));
    assertThat(caravansery.modifiers())
        .containsExactly(Production.choice(false, Resource.WOOD, Resource.CLAY, Resource.STONE));
  }

  @Test
  void customsHouseFixesPapyrusAndGlassAtOne() {
    Card house = named(CARDS, "Customs House");
    assertThat(house.cost()).isEqualTo(Cost.coins(4));
    assertThat(priceOf(house, Resource.PAPYRUS)).isEqualTo(1);
    assertThat(priceOf(house, Resource.GLASS)).isEqualTo(1);
    assertThat(priceOf(house, Resource.STONE)).isEqualTo(2);
  }

  @Test
  void redCardsFollowTheirChains() {
    Card barracks = named(CARDS, "Barracks");
    assertThat(barracks.cost()).isEqualTo(Cost.coins(3).withFreedBy(Chains.SWORD));
    assertThat(barracks.effects()).containsExactly(new Shields(1));
    Card range = named(CARDS, "Archery Range");
    assertThat(range.cost()).isEqualTo(Cost.of(0, Resource.STONE, Resource.WOOD, Resource.PAPYRUS));
    assertThat(range.effects()).containsExactly(new Shields(2));
    assertThat(range.modifiers()).containsExactly(Chains.gives(Chains.TARGET));
    assertThat(named(CARDS, "Walls").cost()).isEqualTo(Cost.of(0, Resource.STONE, Resource.STONE));
    assertThat(named(CARDS, "Horse Breeders").cost().freedBy()).containsExactly(Chains.HORSESHOE);
    assertThat(named(CARDS, "Parade Ground").modifiers())
        .containsExactly(Chains.gives(Chains.HELMET));
  }

  @Test
  void greenCardsShowSymbolsAndFollowChains() {
    Card library = named(CARDS, "Library");
    assertThat(library.cost().freedBy()).containsExactly(Chains.BOOK);
    assertThat(library.modifiers()).contains(new Symbol(ScienceSymbol.QUILL));
    assertThat(named(CARDS, "Dispensary").cost().freedBy()).containsExactly(Chains.GEAR);
    assertThat(named(CARDS, "School").modifiers()).contains(Chains.gives(Chains.LYRE));
    assertThat(named(CARDS, "Laboratory").modifiers()).contains(Chains.gives(Chains.LAMP));
    assertThat(named(CARDS, "School").cost())
        .isEqualTo(Cost.of(0, Resource.WOOD, Resource.PAPYRUS, Resource.PAPYRUS));
  }

  @Test
  void blueCardsLinkTheChainsAcrossAges() {
    Card statue = named(CARDS, "Statue");
    assertThat(statue.cost())
        .isEqualTo(Cost.of(0, Resource.CLAY, Resource.CLAY).withFreedBy(Chains.THEATER));
    assertThat(statue.modifiers()).contains(Chains.gives(Chains.COLUMNS));
    Card temple = named(CARDS, "Temple");
    assertThat(temple.cost().freedBy()).containsExactly(Chains.MOON);
    assertThat(temple.modifiers()).contains(Chains.gives(Chains.SUN));
    assertThat(named(CARDS, "Aqueduct").cost().freedBy()).containsExactly(Chains.WATER);
    assertThat(named(CARDS, "Rostrum").modifiers()).contains(Chains.gives(Chains.TEMPLE));
    assertThat(named(CARDS, "Courthouse").cost())
        .isEqualTo(Cost.of(0, Resource.WOOD, Resource.WOOD, Resource.GLASS));
  }

  @Test
  void breweryPaysSixCoinsAndGivesTheBarrel() {
    Card brewery = named(CARDS, "Brewery");
    assertThat(brewery.cost()).isEqualTo(Cost.FREE);
    assertThat(brewery.effects()).containsExactly(new GainCoins(6));
    assertThat(brewery.modifiers()).containsExactly(Chains.gives(Chains.BARREL));
  }
}
