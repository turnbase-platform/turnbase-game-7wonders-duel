package io.turnbase.games.sevenwondersduel.content;

import io.turnbase.games.sevenwondersduel.effect.LoseCoins;
import io.turnbase.games.sevenwondersduel.military.MilitaryTrack;
import java.util.List;

/**
 * The 4 military tokens (base.md §3.9): one of each loot on each side of the track. The zone and
 * seat tags say where Setup lays the token; the effect hits the player the pawn is advancing
 * against.
 */
public final class MilitaryTokens {

  public static List<Token> all() {
    return List.of(loot(2, 2, 0, "a"), loot(2, 2, 1, "b"), loot(5, 3, 0, "a"), loot(5, 3, 1, "b"));
  }

  private static Token loot(int coins, int zone, int seat, String copy) {
    return Token.of(
            "Loot " + coins + " " + copy, MilitaryTrack.zoneTag(zone), MilitaryTrack.seatTag(seat))
        .effects(new LoseCoins(coins));
  }

  private MilitaryTokens() {}
}
