package io.turnbase.games.sevenwondersduel.scoring;

import io.turnbase.games.sevenwondersduel.catalog.Component;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.catalog.Tag;
import io.turnbase.games.sevenwondersduel.effect.Ownership;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.military.MilitaryTrack;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.state.Pawn;
import java.util.LinkedHashMap;
import java.util.Map;

/** The final score (B-END-2), derived from what each player has in play. */
public final class Scoring {

  public static final String MILITARY = "military";
  public static final String COINS = "coins";
  public static final String BLUE = "blue";
  public static final String BUILDINGS = "buildings";
  public static final String WONDERS = "wonders";
  public static final String PROGRESS = "progress";

  private Scoring() {}

  /** The score of {@code player} in {@code state}. */
  public static Scorecard score(DuelState state, RuleSet rules, PlayerId player) {
    ScoreContext context = new ScoreContext(state, rules, player);
    Map<String, Integer> lines = new LinkedHashMap<>();
    for (ComponentId id : Ownership.liveComponents(state, rules.zones(), player)) {
      Component component = rules.catalog().get(id);
      for (var modifier : component.modifiers()) {
        if (modifier instanceof Points points) {
          lines.merge(lineOf(component), points.points(context), Integer::sum);
        }
      }
    }
    lines.put(MILITARY, military(state, player));
    lines.put(COINS, state.player(player).coins() / COINS_PER_POINT);
    return new Scorecard(player, lines);
  }

  private static final int COINS_PER_POINT = 3;

  /** The pawn leans towards a player's capital; the other player scores its zone. */
  private static int military(DuelState state, PlayerId player) {
    Pawn pawn = state.pawn();
    if (pawn.side() == null || pawn.side().equals(player)) {
      return 0;
    }
    return MilitaryTrack.victoryPoints(MilitaryTrack.zoneOf(pawn.space()));
  }

  /** The line a component's points are counted in. */
  static String lineOf(Component component) {
    return switch (component.type()) {
      case WONDER -> WONDERS;
      case TOKEN -> PROGRESS;
      case CARD -> component.tags().contains(Tag.BLUE) ? BLUE : BUILDINGS;
    };
  }
}
