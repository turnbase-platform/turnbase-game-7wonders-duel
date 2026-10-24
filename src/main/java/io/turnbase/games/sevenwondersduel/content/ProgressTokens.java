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
        token("Agriculture").effects(new GainCoins(6)).modifiers(Points.fixed(4)),
        token("Architecture").modifiers(new Reduction(2, Selector.wonders())),
        token("Economy").triggers(new TradeRevenue()),
        token("Law").modifiers(new Symbol(ScienceSymbol.SCALES)).effects(new ScienceCheck()),
        token("Masonry").modifiers(new Reduction(2, Selector.tagged(Colors.BLUE))),
        token("Mathematics").modifiers(Points.per(Census.of(Tag.PROGRESS), 3)),
        token("Philosophy").modifiers(Points.fixed(7)),
        token("Strategy").modifiers(new ShieldBonus(Colors.RED, 1)),
        token("Theology").triggers(new WonderBonusTurn()),
        token("Urbanism").effects(new GainCoins(6)).triggers(new ChainBonus(4)));
  }

  private static Token token(String name) {
    return Token.of(name, Tag.PROGRESS);
  }

  private ProgressTokens() {}
}
