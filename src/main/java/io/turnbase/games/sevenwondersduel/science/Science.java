package io.turnbase.games.sevenwondersduel.science;

import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.effect.Ownership;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import java.util.LinkedHashMap;
import java.util.Map;

/** What a player's scientific symbols add up to, derived from what they own (B-EFF-1). */
public final class Science {

  /** How many of each symbol the player holds. */
  public static Map<ScienceSymbol, Integer> counts(
      DuelState state, RuleSet rules, PlayerId player) {
    Map<ScienceSymbol, Integer> counts = new LinkedHashMap<>();
    for (Symbol symbol : Ownership.modifiers(state, rules, player, Symbol.class)) {
      counts.merge(symbol.symbol(), 1, Integer::sum);
    }
    return counts;
  }

  /** Different symbols that win the game at once (B-SCI-3). */
  public static final int SUPREMACY = 6;

  /** How many different symbols the player holds (B-SCI-3). */
  public static int distinct(DuelState state, RuleSet rules, PlayerId player) {
    return counts(state, rules, player).size();
  }

  private Science() {}
}
