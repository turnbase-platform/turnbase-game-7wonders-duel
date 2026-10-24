package io.turnbase.games.sevenwondersduel.cost;

import io.turnbase.games.sevenwondersduel.effect.Badge;
import java.util.List;
import java.util.Set;

/** Buying any of {@code resources} costs exactly {@code price}. */
public record FixedPrice(int price, Set<Resource> resources) implements PriceModifier {

  public FixedPrice {
    resources = Set.copyOf(resources);
  }

  @Override
  public int price(Resource resource, int current) {
    return resources.contains(resource) ? price : current;
  }

  @Override
  public List<Badge> badges() {
    return List.of(
        Badge.of(
            "fixedPrice",
            price,
            resources.stream().map(Resource::id).sorted().toArray(String[]::new)));
  }
}
