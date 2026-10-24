package io.turnbase.games.sevenwondersduel.scoring;

import io.turnbase.games.sevenwondersduel.id.PlayerId;
import java.util.Map;

/**
 * One player's final score, by named line, so the tie-break and the end-of-game display read the
 * same numbers.
 */
public record Scorecard(PlayerId player, Map<String, Integer> lines) {

  public Scorecard {
    lines = Map.copyOf(lines);
  }

  public int line(String name) {
    return lines.getOrDefault(name, 0);
  }

  public int total() {
    return lines.values().stream().mapToInt(Integer::intValue).sum();
  }
}
