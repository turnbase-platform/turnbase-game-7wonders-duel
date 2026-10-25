package io.turnbase.games.sevenwondersduel.cost;

import java.util.List;
import java.util.Map;

/**
 * What constructing a component costs one player right now.
 *
 * @param coins the printed coin cost
 * @param bought symbols the player must buy, per resource
 * @param tradeCost coins paid for {@code bought}
 * @param chained whether a chain symbol waived the whole cost (B-COST-5)
 * @param lines one per resource of the cost, in resource id order; empty when chained
 */
public record Quote(
    int coins,
    Map<Resource, Integer> bought,
    int tradeCost,
    boolean chained,
    List<QuoteLine> lines) {

  public Quote {
    bought = Map.copyOf(bought);
    lines = List.copyOf(lines);
  }

  public int total() {
    return coins + tradeCost;
  }

  public boolean payableWith(int treasury) {
    return total() <= treasury;
  }
}
