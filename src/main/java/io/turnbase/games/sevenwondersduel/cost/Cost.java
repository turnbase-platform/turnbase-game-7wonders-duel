package io.turnbase.games.sevenwondersduel.cost;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * What it takes to construct a card or Wonder (B-COST-1..5): coins to the bank, resource symbols
 * that must be produced or bought, and the chain symbols that make it free.
 *
 * @param coins coins paid regardless of resources
 * @param resources how many symbols of each resource are needed
 * @param freedBy chain symbols that, held in the city, waive the whole cost
 */
public record Cost(int coins, Map<Resource, Integer> resources, Set<ChainSymbol> freedBy) {

  public static final Cost FREE = new Cost(0, Map.of(), Set.of());

  public Cost {
    if (coins < 0) {
      throw new IllegalArgumentException("coins must not be negative: " + coins);
    }
    for (Map.Entry<Resource, Integer> need : resources.entrySet()) {
      if (need.getValue() <= 0) {
        throw new IllegalArgumentException(
            "resource count must be positive: " + need.getKey().id() + " " + need.getValue());
      }
    }
    resources = Map.copyOf(resources);
    freedBy = Set.copyOf(freedBy);
  }

  public static Cost coins(int coins) {
    return new Cost(coins, Map.of(), Set.of());
  }

  /** {@code coins} plus one symbol per listed resource; repeat a resource to need several. */
  public static Cost of(int coins, Resource... symbols) {
    Map<Resource, Integer> counts = new HashMap<>();
    for (Resource symbol : symbols) {
      counts.merge(symbol, 1, Integer::sum);
    }
    return new Cost(coins, counts, Set.of());
  }

  public Cost withFreedBy(ChainSymbol... symbols) {
    return new Cost(coins, resources, Set.of(symbols));
  }

  /** Total resource symbols asked for. */
  public int symbols() {
    return resources.values().stream().mapToInt(Integer::intValue).sum();
  }
}
