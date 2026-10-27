package io.turnbase.games.sevenwondersduel.content;

import io.turnbase.games.sevenwondersduel.catalog.Tag;
import io.turnbase.games.sevenwondersduel.cost.ChainBonus;
import io.turnbase.games.sevenwondersduel.cost.Reduction;
import io.turnbase.games.sevenwondersduel.cost.Selector;
import io.turnbase.games.sevenwondersduel.cost.TradeRevenue;
import io.turnbase.games.sevenwondersduel.effect.Census;
import io.turnbase.games.sevenwondersduel.effect.GainCoins;
import io.turnbase.games.sevenwondersduel.effect.ScienceCheck;
import io.turnbase.games.sevenwondersduel.effect.WonderBonusTurn;
import io.turnbase.games.sevenwondersduel.military.ShieldBonus;
import io.turnbase.games.sevenwondersduel.science.ScienceSymbol;
import io.turnbase.games.sevenwondersduel.science.Symbol;
import io.turnbase.games.sevenwondersduel.scoring.Points;
import java.util.List;

/** The 10 progress tokens (base.md §3.8). */
public final class ProgressTokens {

  public static List<Token> all() {
    return List.of(
        token("Agriculture", "Gain 6 coins now. Worth 4 victory points.")
            .effects(new GainCoins(6))
            .modifiers(Points.fixed(4)),
        token("Architecture", "Your future Wonders cost 2 fewer resources, of your choice.")
            .modifiers(new Reduction(2, Selector.wonders())),
        token(
                "Economy",
                "The coins your opponent spends on trading come to you instead of the bank.")
            .triggers(new TradeRevenue()),
        token("Law", "Counts as a Scales science symbol.")
            .modifiers(new Symbol(ScienceSymbol.SCALES))
            .effects(new ScienceCheck()),
        token("Masonry", "Your future blue cards cost 2 fewer resources, of your choice.")
            .modifiers(new Reduction(2, Selector.tagged(Colors.BLUE))),
        token("Mathematics", "3 victory points for each progress token you own, this one included.")
            .modifiers(Points.per(Census.of(Tag.PROGRESS), 3)),
        token("Philosophy", "Worth 7 victory points.").modifiers(Points.fixed(7)),
        token("Strategy", "Your future red cards give 1 extra shield.")
            .modifiers(new ShieldBonus(Colors.RED, 1)),
        token("Theology", "Your future Wonders let you play again.")
            .triggers(new WonderBonusTurn()),
        token(
                "Urbanism",
                "Gain 6 coins now, and 4 coins each time you build a Building free through a chain.")
            .effects(new GainCoins(6))
            .triggers(new ChainBonus(4)));
  }

  private static Token token(String name, String description) {
    return Token.of(name, Tag.PROGRESS).described(description);
  }

  private ProgressTokens() {}
}
