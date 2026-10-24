package io.turnbase.games.sevenwondersduel.effect;

import static org.assertj.core.api.Assertions.assertThat;

import io.turnbase.games.sevenwondersduel.catalog.Tag;
import io.turnbase.games.sevenwondersduel.content.Colors;
import io.turnbase.games.sevenwondersduel.cost.ChainBonus;
import io.turnbase.games.sevenwondersduel.cost.ChainSource;
import io.turnbase.games.sevenwondersduel.cost.ChainSymbol;
import io.turnbase.games.sevenwondersduel.cost.PriceModifier;
import io.turnbase.games.sevenwondersduel.cost.Production;
import io.turnbase.games.sevenwondersduel.cost.Reduction;
import io.turnbase.games.sevenwondersduel.cost.Resource;
import io.turnbase.games.sevenwondersduel.cost.Selector;
import io.turnbase.games.sevenwondersduel.cost.TradeRevenue;
import io.turnbase.games.sevenwondersduel.military.ShieldBonus;
import io.turnbase.games.sevenwondersduel.science.ScienceSymbol;
import io.turnbase.games.sevenwondersduel.science.Symbol;
import io.turnbase.games.sevenwondersduel.scoring.Points;
import java.util.List;
import org.junit.jupiter.api.Test;

/** What each base mechanic tells a client about itself (plan 11). */
class BadgesTest {

  @Test
  void simpleEffectsNameTheirAmount() {
    assertThat(new Shields(2).badges()).containsExactly(Badge.of("shields", 2));
    assertThat(new GainCoins(6).badges()).containsExactly(Badge.of("coins", 6));
    assertThat(new TakeCoins(3).badges()).containsExactly(Badge.of("takeCoins", 3));
    assertThat(new LoseCoins(3).badges()).containsExactly(Badge.of("loseCoins", 3));
    assertThat(new DrawProgressToken(3).badges()).containsExactly(Badge.of("drawProgress", 3));
  }

  @Test
  void effectsWithoutNumbersNameThemselves() {
    assertThat(new GrantPlayAgain().badges()).containsExactly(Badge.of("playAgain"));
    assertThat(new BuildFromDiscard().badges()).containsExactly(Badge.of("buildFromDiscard"));
    assertThat(new WonderBonusTurn().badges()).containsExactly(Badge.of("playAgainOnWonder"));
    assertThat(new TradeRevenue().badges()).containsExactly(Badge.of("tradeRevenue"));
  }

  @Test
  void theScienceCheckIsLeftToTheSymbol() {
    assertThat(new ScienceCheck().badges()).isEmpty();
    assertThat(new Symbol(ScienceSymbol.WHEEL).badges())
        .containsExactly(Badge.of("science", 0, "wheel"));
  }

  @Test
  void discardingNamesWhatIsDiscarded() {
    assertThat(new DiscardOpponentCard(Colors.GREY).badges())
        .containsExactly(Badge.of("discardOpponent", 0, "color:grey"));
  }

  @Test
  void aCensusNamesWhatItCounts() {
    assertThat(new GainCoinsPer(Census.of(Colors.BROWN), 2).badges())
        .containsExactly(new Badge("coinsPer", 2, List.of("color:brown"), 1, false));
    assertThat(
            new GainCoinsPer(
                    Census.largerOfBoth(
                        Census.sum(Census.of(Colors.BROWN), Census.of(Colors.GREY))),
                    1)
                .badges())
        .containsExactly(new Badge("coinsPer", 1, List.of("color:brown", "color:grey"), 1, true));
  }

  @Test
  void pointsAreFixedOrPerCount() {
    assertThat(Points.fixed(7).badges()).containsExactly(Badge.of("points", 7));
    assertThat(Points.per(Census.every(3, Census.largerOfBoth(Census.coins())), 1).badges())
        .containsExactly(new Badge("pointsPer", 1, List.of("coins"), 3, true));
    assertThat(Points.per(Census.of(Tag.WONDER), 2).badges())
        .containsExactly(new Badge("pointsPer", 2, List.of("wonder"), 1, false));
  }

  @Test
  void productionCountsFixedUnitsAndNamesAChoice() {
    assertThat(Production.fixed(Resource.WOOD, 2, true).badges())
        .containsExactly(Badge.of("produces", 2, "wood"));
    assertThat(Production.choice(false, Resource.STONE, Resource.CLAY, Resource.WOOD).badges())
        .containsExactly(Badge.of("producesOneOf", 1, "clay", "stone", "wood"));
  }

  @Test
  void pricesReductionsAndShieldsNameTheirTargets() {
    assertThat(PriceModifier.fixedAt(1, Resource.GLASS, Resource.PAPYRUS).badges())
        .containsExactly(Badge.of("fixedPrice", 1, "glass", "papyrus"));
    assertThat(new Reduction(2, Selector.wonders()).badges())
        .containsExactly(Badge.of("reduction", 2, "wonder"));
    assertThat(new Reduction(2, Selector.tagged(Colors.BLUE)).badges())
        .containsExactly(Badge.of("reduction", 2, "color:blue"));
    assertThat(new ShieldBonus(Colors.RED, 1).badges())
        .containsExactly(Badge.of("shieldBonus", 1, "color:red"));
  }

  @Test
  void chainsNameTheirSymbols() {
    assertThat(new ChainSource(java.util.Set.of(new ChainSymbol("horseshoe"))).badges())
        .containsExactly(Badge.of("chain", 0, "horseshoe"));
    assertThat(new ChainBonus(4).badges()).containsExactly(Badge.of("chainBonus", 4));
  }
}
