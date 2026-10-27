package io.turnbase.games.sevenwondersduel.content;

import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P1;
import static org.assertj.core.api.Assertions.assertThat;

import io.turnbase.games.sevenwondersduel.catalog.BasePack;
import io.turnbase.games.sevenwondersduel.catalog.ComponentType;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.catalog.Tag;
import io.turnbase.games.sevenwondersduel.cost.ChainBonus;
import io.turnbase.games.sevenwondersduel.cost.Reduction;
import io.turnbase.games.sevenwondersduel.cost.TradeRevenue;
import io.turnbase.games.sevenwondersduel.effect.Fixtures;
import io.turnbase.games.sevenwondersduel.effect.GainCoins;
import io.turnbase.games.sevenwondersduel.effect.ScienceCheck;
import io.turnbase.games.sevenwondersduel.effect.WonderBonusTurn;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.military.ShieldBonus;
import io.turnbase.games.sevenwondersduel.science.ScienceSymbol;
import io.turnbase.games.sevenwondersduel.science.Symbol;
import io.turnbase.games.sevenwondersduel.scoring.Points;
import io.turnbase.games.sevenwondersduel.scoring.ScoreContext;
import java.util.List;
import org.junit.jupiter.api.Test;

class ProgressTokensTest {

  private static final List<Token> TOKENS = ProgressTokens.all();
  private static final RuleSet RULES = RuleSet.assemble(List.of(new BasePack(), new BaseContent()));

  private static Token token(String name) {
    return TOKENS.stream().filter(t -> t.name().equals(name)).findFirst().orElseThrow();
  }

  @Test
  void everyTokenSaysInWordsWhatItDoes() {
    assertThat(TOKENS).allSatisfy(t -> assertThat(t.description()).isNotBlank());
  }

  private static int points(Token t, ScoreContext context) {
    return t.modifiers().stream()
        .filter(Points.class::isInstance)
        .mapToInt(m -> ((Points) m).points(context))
        .sum();
  }

  private static int pointsAlone(Token t) {
    return points(t, new ScoreContext(Fixtures.state(RULES, P1, 7, 7), RULES, P1));
  }

  private static boolean has(List<?> list, Class<?> type) {
    return list.stream().anyMatch(type::isInstance);
  }

  @Test
  void tenDistinctProgressTokensWithNoBackOrCost() {
    assertThat(TOKENS).hasSize(10);
    assertThat(TOKENS.stream().map(Token::id).distinct()).hasSize(10);
    assertThat(TOKENS)
        .allSatisfy(
            t -> {
              assertThat(t.type()).isEqualTo(ComponentType.TOKEN);
              assertThat(t.tags()).containsExactly(Tag.PROGRESS);
              assertThat(t.back()).isEmpty();
            });
    assertThat(TOKENS.stream().map(Token::name))
        .containsExactlyInAnyOrder(
            "Agriculture",
            "Architecture",
            "Economy",
            "Law",
            "Masonry",
            "Mathematics",
            "Philosophy",
            "Strategy",
            "Theology",
            "Urbanism");
  }

  @Test
  void agricultureIsSixCoinsAndFourPoints() {
    Token t = token("Agriculture");

    assertThat(t.effects()).isEqualTo(List.of(new GainCoins(6)));
    assertThat(pointsAlone(t)).isEqualTo(4);
  }

  @Test
  void philosophyIsSevenPoints() {
    assertThat(pointsAlone(token("Philosophy"))).isEqualTo(7);
  }

  @Test
  void mathematicsIsThreePointsPerProgressTokenItselfIncluded() {
    Token maths = token("Mathematics");
    ScoreContext alone =
        new ScoreContext(Fixtures.state(RULES, P1, 7, 7, maths.id(), Fixtures.city(P1)), RULES, P1);
    ScoreContext three =
        new ScoreContext(
            Fixtures.state(
                RULES,
                P1,
                7,
                7,
                maths.id(),
                Fixtures.city(P1),
                token("Law").id(),
                Fixtures.city(P1),
                token("Economy").id(),
                Fixtures.city(P1)),
            RULES,
            P1);

    assertThat(points(maths, alone)).isEqualTo(3);
    assertThat(points(maths, three)).isEqualTo(9);
  }

  @Test
  void lawGrantsScalesAndChecksForAPair() {
    Token law = token("Law");

    assertThat(law.modifiers()).contains(new Symbol(ScienceSymbol.SCALES));
    assertThat(law.effects()).isEqualTo(List.of(new ScienceCheck()));
  }

  @Test
  void architectureAndMasonryCutTwoSymbolsOffTheirTargets() {
    Reduction architecture = (Reduction) token("Architecture").modifiers().get(0);
    Reduction masonry = (Reduction) token("Masonry").modifiers().get(0);
    Wonder wonder = Wonders.all().get(0);
    Card blue =
        AgeOne.cards().stream()
            .filter(c -> c.tags().contains(Colors.BLUE))
            .findFirst()
            .orElseThrow();
    Card brown =
        AgeOne.cards().stream()
            .filter(c -> c.tags().contains(Colors.BROWN))
            .findFirst()
            .orElseThrow();

    assertThat(architecture.symbols()).isEqualTo(2);
    assertThat(architecture.appliesTo().test(wonder)).isTrue();
    assertThat(architecture.appliesTo().test(blue)).isFalse();
    assertThat(masonry.symbols()).isEqualTo(2);
    assertThat(masonry.appliesTo().test(blue)).isTrue();
    assertThat(masonry.appliesTo().test(wonder)).isFalse();
    assertThat(masonry.appliesTo().test(brown)).isFalse();
  }

  @Test
  void economyStrategyAndTheologyCarryTheirHooks() {
    assertThat(has(token("Economy").triggers(), TradeRevenue.class)).isTrue();
    assertThat(token("Strategy").modifiers()).containsExactly(new ShieldBonus(Colors.RED, 1));
    assertThat(has(token("Theology").triggers(), WonderBonusTurn.class)).isTrue();
  }

  @Test
  void urbanismIsSixCoinsAndAFourCoinChainBonus() {
    Token t = token("Urbanism");

    assertThat(t.effects()).isEqualTo(List.of(new GainCoins(6)));
    assertThat(t.triggers()).containsExactly(new ChainBonus(4));
  }

  @Test
  void idsAreSluggedFromNames() {
    assertThat(token("Law").id()).isEqualTo(ComponentId.of("law"));
  }
}
