package io.turnbase.games.sevenwondersduel.view;

import java.util.List;

/**
 * A cost for a client to draw. Lists, not maps, and sorted, so that the message is the same every
 * time.
 *
 * @param coins coins paid to the bank
 * @param resources resource symbols needed, by resource id
 * @param freedBy chain symbols that waive the cost
 */
public record CostView(int coins, List<ResourceCount> resources, List<String> freedBy) {

  public CostView {
    resources = List.copyOf(resources);
    freedBy = List.copyOf(freedBy);
  }
}
