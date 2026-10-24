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
  private final List<PackId> packIds;

  private RuleSet(
      ZoneRegistry zones, Catalog catalog, Map<LayoutId, Layout> layouts, List<PackId> packIds) {
    this.zones = zones;
    this.catalog = catalog;
    this.layouts = layouts;
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
    }
    return new RuleSet(
        ZoneRegistry.of(kinds),
        new Catalog(components),
        Map.copyOf(layouts),
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
