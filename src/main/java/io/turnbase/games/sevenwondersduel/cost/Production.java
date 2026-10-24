package io.turnbase.games.sevenwondersduel.cost;

import io.turnbase.games.sevenwondersduel.effect.Badge;
import io.turnbase.games.sevenwondersduel.effect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Resource production while the owner's component is live (B-PROD-1..4). Each unit is one symbol's
 * worth; a unit with several alternatives is a choice and serves one of them per construction.
 *
 * @param units one entry per unit, holding the resources that unit may stand for
 * @param raisesOpponentPrice whether the opponent pays more to buy these resources (B-TRADE-2);
 *     only a unit with a single alternative can raise it
 */
public record Production(List<Set<Resource>> units, boolean raisesOpponentPrice)
    implements Modifier {

  public Production {
    for (Set<Resource> unit : units) {
      if (unit.isEmpty()) {
        throw new IllegalArgumentException("a production unit needs at least one resource");
      }
    }
    units = List.copyOf(units);
  }

  /** {@code n} units that each always stand for {@code resource}. */
  public static Production fixed(Resource resource, int n, boolean raisesOpponentPrice) {
    List<Set<Resource>> units = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      units.add(Set.of(resource));
    }
    return new Production(units, raisesOpponentPrice);
  }

  /** One unit that stands for any one of {@code alternatives}. */
  public static Production choice(boolean raisesOpponentPrice, Resource... alternatives) {
    return new Production(List.of(Set.of(alternatives)), raisesOpponentPrice);
  }

  @Override
  public List<Badge> badges() {
    Map<String, Integer> fixed = new TreeMap<>();
    List<Badge> choices = new ArrayList<>();
    for (Set<Resource> unit : units) {
      String[] ids = unit.stream().map(Resource::id).sorted().toArray(String[]::new);
      if (ids.length == 1) {
        fixed.merge(ids[0], 1, Integer::sum);
      } else {
        choices.add(Badge.of("producesOneOf", 1, ids));
      }
    }
    List<Badge> badges = new ArrayList<>();
    fixed.forEach((id, n) -> badges.add(Badge.of("produces", n, id)));
    badges.addAll(choices);
    return badges;
  }
}
