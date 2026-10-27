package io.turnbase.games.sevenwondersduel.view;

import io.turnbase.games.sevenwondersduel.catalog.Component;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.catalog.Tag;
import io.turnbase.games.sevenwondersduel.cost.ChainSymbol;
import io.turnbase.games.sevenwondersduel.cost.Cost;
import io.turnbase.games.sevenwondersduel.cost.Resource;
import io.turnbase.games.sevenwondersduel.effect.Badge;
import io.turnbase.games.sevenwondersduel.effect.Badged;
import io.turnbase.games.sevenwondersduel.id.BackId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/** Builds the {@link CatalogView} of a rule set. */
public final class CatalogViews {

  private CatalogViews() {}

  public static CatalogView of(RuleSet rules) {
    return new CatalogView(rules.catalog().all().stream().map(CatalogViews::entry).toList());
  }

  private static CatalogEntry entry(Component component) {
    return new CatalogEntry(
        component.id().value(),
        component.name(),
        component.description(),
        component.type().name(),
        component.tags().stream().map(Tag::value).sorted().toList(),
        component.back().map(BackId::value).orElse(null),
        cost(component.cost()),
        badges(component));
  }

  private static CostView cost(Cost cost) {
    return new CostView(
        cost.coins(),
        cost.resources().entrySet().stream()
            .sorted(Map.Entry.comparingByKey(Comparator.comparing(Resource::id)))
            .map(e -> new ResourceCount(e.getKey().id(), e.getValue()))
            .toList(),
        cost.freedBy().stream().map(ChainSymbol::id).sorted().toList());
  }

  private static List<Badge> badges(Component component) {
    return Stream.<List<? extends Badged>>of(
            component.effects(), component.modifiers(), component.triggers())
        .flatMap(List::stream)
        .flatMap(mechanic -> mechanic.badges().stream())
        .toList();
  }
}
