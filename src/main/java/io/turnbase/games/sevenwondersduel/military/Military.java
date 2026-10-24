package io.turnbase.games.sevenwondersduel.military;

import io.turnbase.games.sevenwondersduel.effect.Resolution;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.id.ZoneId;
import io.turnbase.games.sevenwondersduel.state.Pawn;
import io.turnbase.games.sevenwondersduel.state.Victory;
import io.turnbase.games.sevenwondersduel.zone.Location;
import io.turnbase.games.sevenwondersduel.zone.Placements;
import java.util.ArrayList;
import java.util.List;

/** Moving the military pawn (B-MIL-2..4). */
public final class Military {

  private static final ZoneId DESTROYED = ZoneId.of("destroyed");

  private record Spent(ComponentId token, PlayerId side) {}

  /**
   * Moves the pawn {@code shields} spaces toward the opponent of {@code mover}, one space at a
   * time. Entering a zone spends its token: the token goes out of the game and its effects are
   * queued, first entered on top, for the opponent of the side's player, so the player who lost
   * ground is the one the token's effects hit. Reaching the capital ends the game in the mover's
   * favour and drops everything else.
   */
  public static void advance(Resolution resolution, PlayerId mover, int shields) {
    PlayerId rival = resolution.state().opponentOf(mover);
    List<Spent> spent = new ArrayList<>();
    int zone = MilitaryTrack.zoneOf(resolution.state().pawn().space());
    for (int i = 0; i < shields; i++) {
      Pawn pawn = resolution.state().pawn().stepToward(rival);
      resolution.setPawn(pawn);
      int entered = MilitaryTrack.zoneOf(pawn.space());
      if (entered == zone) {
        continue;
      }
      zone = entered;
      if (zone == MilitaryTrack.CAPITAL_ZONE) {
        resolution.declareVictory(mover, Victory.Reason.MILITARY);
        return;
      }
      if (zone > 0) {
        spend(resolution, pawn.side(), zone, spent);
      }
    }
    for (int i = spent.size() - 1; i >= 0; i--) {
      Spent s = spent.get(i);
      resolution.queueEffects(s.token(), resolution.state().opponentOf(s.side()));
    }
  }

  private static void spend(Resolution resolution, PlayerId side, int zone, List<Spent> spent) {
    Location spot = MilitaryTrack.tokenSpot(side, zone);
    for (ComponentId token : Placements.idsIn(resolution.state().placements(), spot)) {
      resolution.move(token, new Location(DESTROYED, side.value().toString()));
      spent.add(new Spent(token, side));
    }
  }

  private Military() {}
}
