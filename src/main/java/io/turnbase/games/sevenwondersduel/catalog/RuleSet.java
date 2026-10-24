package io.turnbase.games.sevenwondersduel.catalog;

import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.LayoutId;
import io.turnbase.games.sevenwondersduel.id.PackId;
import io.turnbase.games.sevenwondersduel.id.ZoneId;
import io.turnbase.games.sevenwondersduel.structure.Layout;
import io.turnbase.games.sevenwondersduel.zone.ZoneKind;
import io.turnbase.games.sevenwondersduel.zone.ZoneRegistry;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** The game's rules as data: the zones and components contributed by the chosen content packs. */
public final class RuleSet {

  private final ZoneRegistry zones;
  private final Catalog catalog;
  private final Map<LayoutId, Layout> layouts;
  private final Map<Integer, LayoutId> ageLayouts;
  private final List<PackId> packIds;

  private RuleSet(
      ZoneRegistry zones,
      Catalog catalog,
      Map<LayoutId, Layout> layouts,
      Map<Integer, LayoutId> ageLayouts,
      List<PackId> packIds) {
    this.zones = zones;
    this.catalog = catalog;
    this.layouts = layouts;
    this.ageLayouts = ageLayouts;
    this.packIds = packIds;
  }

  /** Merges packs in order; a zone or component id defined by two packs is rejected. */
  public static RuleSet assemble(List<ContentPack> packs) {
    List<ZoneKind> kinds = new ArrayList<>();
    Map<ZoneId, PackId> zoneOwner = new LinkedHashMap<>();
    Map<ComponentId, Component> components = new LinkedHashMap<>();
    Map<ComponentId, PackId> componentOwner = new LinkedHashMap<>();
    Map<LayoutId, Layout> layouts = new LinkedHashMap<>();
    Map<LayoutId, PackId> layoutOwner = new LinkedHashMap<>();
    Map<Integer, LayoutId> ageLayouts = new LinkedHashMap<>();
    Map<Integer, PackId> ageOwner = new LinkedHashMap<>();
    for (ContentPack pack : packs) {
      for (ZoneKind kind : pack.zoneKinds()) {
        PackId previous = zoneOwner.putIfAbsent(kind.id(), pack.id());
        if (previous != null) {
          throw duplicate("zone", kind.id().value(), previous, pack.id());
        }
        kinds.add(kind);
      }
      for (Component component : pack.components()) {
        PackId previous = componentOwner.putIfAbsent(component.id(), pack.id());
        if (previous != null) {
          throw duplicate("component", component.id().value(), previous, pack.id());
        }
        components.put(component.id(), component);
      }
      for (Layout layout : pack.layouts()) {
        PackId previous = layoutOwner.putIfAbsent(layout.id(), pack.id());
        if (previous != null) {
          throw duplicate("layout", layout.id().value(), previous, pack.id());
        }
        layouts.put(layout.id(), layout);
      }
      pack.ageLayouts()
          .forEach(
              (age, layout) -> {
                PackId previous = ageOwner.putIfAbsent(age, pack.id());
                if (previous != null) {
                  throw duplicate("age", String.valueOf(age), previous, pack.id());
                }
                ageLayouts.put(age, layout);
              });
    }
    ageLayouts.forEach(
        (age, layout) -> {
          if (!layouts.containsKey(layout)) {
            throw new IllegalArgumentException(
                "age " + age + " maps to unknown layout: " + layout.value());
          }
        });
    return new RuleSet(
        ZoneRegistry.of(kinds),
        new Catalog(components),
        Map.copyOf(layouts),
        Map.copyOf(ageLayouts),
        packs.stream().map(ContentPack::id).toList());
  }

  public ZoneRegistry zones() {
    return zones;
  }

  public Catalog catalog() {
    return catalog;
  }

  /** The layout registered under {@code id}. */
  public Layout layout(LayoutId id) {
    Layout layout = layouts.get(id);
    if (layout == null) {
      throw new IllegalArgumentException("unknown layout: " + id.value());
    }
    return layout;
  }

  /** The layout Age {@code age} is dealt into. */
  public Layout layoutForAge(int age) {
    LayoutId id = ageLayouts.get(age);
    if (id == null) {
      throw new IllegalArgumentException("no layout for age " + age);
    }
    return layout(id);
  }

  public List<PackId> packIds() {
    return packIds;
  }

  private static IllegalArgumentException duplicate(
      String what, String id, PackId first, PackId second) {
    return new IllegalArgumentException(
        "duplicate "
            + what
            + " '"
            + id
            + "' defined by packs '"
            + first.value()
            + "' and '"
            + second.value()
            + "'");
  }
}
