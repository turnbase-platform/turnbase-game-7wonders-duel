package io.turnbase.games.sevenwondersduel.content;

import static io.turnbase.games.sevenwondersduel.content.CardAssertions.named;
import static org.assertj.core.api.Assertions.assertThat;

import io.turnbase.games.sevenwondersduel.catalog.Tag;
import io.turnbase.games.sevenwondersduel.cost.Cost;
import io.turnbase.games.sevenwondersduel.cost.Resource;
import io.turnbase.games.sevenwondersduel.effect.Census;
import io.turnbase.games.sevenwondersduel.effect.GainCoinsPer;
import io.turnbase.games.sevenwondersduel.effect.Shields;
import io.turnbase.games.sevenwondersduel.science.ScienceSymbol;
import io.turnbase.games.sevenwondersduel.science.Symbol;
import io.turnbase.games.sevenwondersduel.scoring.Points;
import java.util.List;
import org.junit.jupiter.api.Test;

class AgeThreeTest {

  private static final List<Card> CARDS = AgeThree.cards();

  @Test
  void twentyCardsOfAgeThreeWithTheVioletBack() {
    assertThat(CARDS).hasSize(20);
    assertThat(CARDS).allSatisfy(card -> assertThat(card.tags()).contains(Tag.age(3)));
    assertThat(CARDS).allSatisfy(card -> assertThat(card.back()).contains(Backs.VIOLET));
  }

  @Test
  void pretoriumCostsEightCoinsAndPushesThreeShields() {
    Card pretorium = named(CARDS, "Pretorium");
    assertThat(pretorium.cost()).isEqualTo(Cost.coins(8));
    assertThat(pretorium.effects()).containsExactly(new Shields(3));
  }

  @Test
  void theTwoFortificationsAreOnlyOne() {
    assertThat(CARDS).filteredOn(c -> c.name().equals("Fortifications")).hasSize(1);
    assertThat(named(CARDS, "Fortifications").cost().freedBy()).containsExactly(Chains.FORTRESS);
    Card circus = named(CARDS, "Circus");
    assertThat(circus.cost())
        .isEqualTo(
            Cost.of(0, Resource.CLAY, Resource.CLAY, Resource.STONE, Resource.STONE)
                .withFreedBy(Chains.HELMET));
    assertThat(circus.effects()).containsExactly(new Shields(2));
    assertThat(named(CARDS, "Siege Workshop").cost().freedBy()).containsExactly(Chains.TARGET);
  }

  @Test
  void academyAndStudyShowTheSundial() {
    Card academy = named(CARDS, "Academy");
    assertThat(academy.cost())
        .isEqualTo(Cost.of(0, Resource.STONE, Resource.WOOD, Resource.GLASS, Resource.GLASS));
    assertThat(academy.modifiers()).contains(new Symbol(ScienceSymbol.SUNDIAL));
    assertThat(named(CARDS, "Study").modifiers()).contains(new Symbol(ScienceSymbol.SUNDIAL));
    assertThat(named(CARDS, "University").cost().freedBy()).containsExactly(Chains.LYRE);
    assertThat(named(CARDS, "Observatory").cost().freedBy()).containsExactly(Chains.LAMP);
    assertThat(named(CARDS, "Observatory").modifiers())
        .contains(new Symbol(ScienceSymbol.ASTROLABE));
  }

  @Test
  void blueCardsEndingAChain() {
    assertThat(named(CARDS, "Gardens").cost().freedBy()).containsExactly(Chains.COLUMNS);
    assertThat(named(CARDS, "Pantheon").cost().freedBy()).containsExactly(Chains.SUN);
    assertThat(named(CARDS, "Senate").cost().freedBy()).containsExactly(Chains.TEMPLE);
    assertThat(named(CARDS, "Palace").modifiers()).hasSize(1).allMatch(Points.class::isInstance);
    assertThat(named(CARDS, "Palace").cost().symbols()).isEqualTo(5);
  }

  @Test
  void commercialCardsPayPerColourOnceAndScoreThree() {
    assertThat(named(CARDS, "Chamber of Commerce").effects())
        .containsExactly(new GainCoinsPer(Census.of(Colors.GREY), 3));
    assertThat(named(CARDS, "Port").effects())
        .containsExactly(new GainCoinsPer(Census.of(Colors.BROWN), 2));
    assertThat(named(CARDS, "Armory").effects())
        .containsExactly(new GainCoinsPer(Census.of(Colors.RED), 1));
    assertThat(named(CARDS, "Lighthouse").effects())
        .containsExactly(new GainCoinsPer(Census.of(Colors.YELLOW), 1));
    assertThat(named(CARDS, "Arena").effects())
        .containsExactly(new GainCoinsPer(Census.of(Tag.WONDER), 2));
    assertThat(named(CARDS, "Lighthouse").cost().freedBy()).containsExactly(Chains.VASE);
    assertThat(named(CARDS, "Arena").cost().freedBy()).containsExactly(Chains.BARREL);
    assertThat(named(CARDS, "Port").modifiers()).hasSize(1).allMatch(Points.class::isInstance);
  }
}
