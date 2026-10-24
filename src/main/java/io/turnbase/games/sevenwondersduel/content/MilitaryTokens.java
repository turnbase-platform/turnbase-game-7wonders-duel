package io.turnbase.games.sevenwondersduel.content;

import io.turnbase.games.sevenwondersduel.effect.LoseCoins;
import io.turnbase.games.sevenwondersduel.military.MilitaryTrack;
import java.util.List;

/**
 * The 4 military tokens (base.md §3.9): one of each loot on each side of the track. The zone tag
 * says where Setup lays the token; the effect hits the player the pawn is advancing against.
 */
public final class MilitaryTokens {

  public static List<Token> all() {
    return List.of(loot(2, 2, "a"), loot(2, 2, "b"), loot(5, 3, "a"), loot(5, 3, "b"));
  }

  private static Token loot(int coins, int zone, String copy) {
    return Token.of("Loot " + coins + " " + copy, MilitaryTrack.zoneTag(zone))
        .effects(new LoseCoins(coins));
  }

  private MilitaryTokens() {}
}
