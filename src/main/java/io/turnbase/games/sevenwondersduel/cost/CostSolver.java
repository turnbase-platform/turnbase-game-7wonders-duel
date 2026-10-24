package io.turnbase.games.sevenwondersduel.cost;

import io.turnbase.games.sevenwondersduel.catalog.Component;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.effect.Ownership;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Prices the construction of a component for a player from what both cities hold (B-COST, B-PROD,
 * B-TRADE). Nothing is stored: a quote is recomputed from placements, so it can never go stale.
 */
public final class CostSolver {

  private final RuleSet rules;

  public CostSolver(RuleSet rules) {
    this.rules = rules;
  }

  public Quote quote(DuelState state, PlayerId player, ComponentId target) {
    Component component = rules.catalog().get(target);
    Cost cost = component.cost();
    if (chained(state, player, cost)) {
      return new Quote(0, Map.of(), 0, true);
    }
    List<Set<Resource>> units = productionUnits(state, player);
    Map<Resource, Integer> needs = new HashMap<>(cost.resources());
    List<Set<Resource>> choices = new ArrayList<>();
    for (Set<Resource> unit : units) {
      if (unit.size() == 1) {
        cover(needs, unit.iterator().next());
      } else {
        choices.add(unit);
      }
    }
    Map<Resource, Integer> prices = new HashMap<>();
    for (Resource resource : needs.keySet()) {
      prices.put(resource, unitPrice(state, player, resource));
    }
    int removals = removals(state, player, component);
    Map<Resource, Integer> bought = cheapest(needs, choices, 0, removals, prices);
    return new Quote(cost.coins(), bought, tradeCost(bought, prices), false);
  }

  private boolean chained(DuelState state, PlayerId player, Cost cost) {
    return Ownership.modifiers(state, rules, player, ChainSource.class).stream()
        .anyMatch(source -> !Collections.disjoint(source.symbols(), cost.freedBy()));
  }

  private int removals(DuelState state, PlayerId player, Component target) {
    return Ownership.modifiers(state, rules, player, Reduction.class).stream()
        .filter(reduction -> reduction.appliesTo().test(target))
        .mapToInt(Reduction::symbols)
        .sum();
  }

  /** What {@code player} pays for one symbol of {@code resource} from the bank (B-TRADE-1..3). */
  public int unitPrice(DuelState state, PlayerId player, Resource resource) {
    int price = 2 + opponentFixedUnits(state, player, resource);
    for (PriceModifier modifier : Ownership.modifiers(state, rules, player, PriceModifier.class)) {
      price = modifier.price(resource, price);
    }
    return price;
  }

  private int opponentFixedUnits(DuelState state, PlayerId player, Resource resource) {
    int count = 0;
    for (Production production :
        Ownership.modifiers(state, rules, state.opponentOf(player), Production.class)) {
      if (production.raisesOpponentPrice()) {
        count +=
            (int)
                production.units().stream()
                    .filter(unit -> unit.size() == 1 && unit.contains(resource))
                    .count();
      }
    }
    return count;
  }

  private List<Set<Resource>> productionUnits(DuelState state, PlayerId player) {
    return Ownership.modifiers(state, rules, player, Production.class).stream()
        .flatMap(production -> production.units().stream())
        .toList();
  }

  private static void cover(Map<Resource, Integer> needs, Resource resource) {
    needs.computeIfPresent(resource, (r, n) -> n > 1 ? n - 1 : null);
  }

  /**
   * The symbols left to buy after the best assignment of choice units to resources and the removal
   * of the dearest {@code removals} symbols.
   */
  private static Map<Resource, Integer> cheapest(
      Map<Resource, Integer> needs,
      List<Set<Resource>> choices,
      int next,
      int removals,
      Map<Resource, Integer> prices) {
    if (next == choices.size()) {
      return remove(needs, removals, prices);
    }
    Map<Resource, Integer> best = null;
    for (Resource option : inIdOrder(choices.get(next))) {
      Map<Resource, Integer> after = new HashMap<>(needs);
      cover(after, option);
      Map<Resource, Integer> candidate = cheapest(after, choices, next + 1, removals, prices);
      if (best == null || tradeCost(candidate, prices) < tradeCost(best, prices)) {
        best = candidate;
      }
    }
    return best;
  }

  /** Alternatives in a fixed order, so equal-priced assignments resolve the same way every run. */
  private static List<Resource> inIdOrder(Set<Resource> alternatives) {
    return alternatives.stream().sorted(Comparator.comparing(Resource::id)).toList();
  }

  private static Map<Resource, Integer> remove(
      Map<Resource, Integer> needs, int removals, Map<Resource, Integer> prices) {
    Map<Resource, Integer> left = new HashMap<>(needs);
    for (int i = 0; i < removals && !left.isEmpty(); i++) {
      Resource dearest =
          left.keySet().stream()
              .min(
                  Comparator.comparingInt((Resource r) -> -prices.get(r))
                      .thenComparing(Resource::id))
              .orElseThrow();
      cover(left, dearest);
    }
    return Map.copyOf(left);
  }

  private static int tradeCost(Map<Resource, Integer> bought, Map<Resource, Integer> prices) {
    return bought.entrySet().stream().mapToInt(e -> e.getValue() * prices.get(e.getKey())).sum();
  }
}
