package io.turnbase.games.sevenwondersduel.wire;

import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.scoring.Scorecard;
import io.turnbase.games.sevenwondersduel.scoring.Scoring;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.state.Victory;
import io.turnbase.gdk.model.GameOutcome;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** The platform's record of a finished game: who placed where, and the numbers behind it. */
public final class Outcomes {

  private Outcomes() {}

  /**
   * One team result per seat: first place for the winner and second for the loser, or first for
   * both with {@code draw} set when the victory is shared (B-END-3).
   */
  public static GameOutcome of(DuelState state, RuleSet rules, Map<UUID, UUID> teams) {
    Victory victory = state.victory();
    if (victory == null) {
      throw new IllegalStateException("the game is not over");
    }
    GameOutcome.Builder outcome = GameOutcome.builder().draw(victory.winner() == null);
    state.players().keySet().stream()
        .sorted(Comparator.comparing(PlayerId::value))
        .forEach(
            id -> {
              Map<String, String> stats = stats(state, rules, victory, id);
              int placement = victory.winner() == null || victory.winner().equals(id) ? 1 : 2;
              outcome.teamResult(teams.get(id.value()), placement, stats);
              outcome.playerResult(id.value(), stats);
            });
    return outcome.build();
  }

  private static Map<String, String> stats(
      DuelState state, RuleSet rules, Victory victory, PlayerId id) {
    Scorecard card = Scoring.score(state, rules, id);
    Map<String, String> stats = new LinkedHashMap<>();
    stats.put("reason", victory.reason().name());
    card.lines().forEach((line, points) -> stats.put(line, String.valueOf(points)));
    stats.put("total", String.valueOf(card.total()));
    return stats;
  }
}
