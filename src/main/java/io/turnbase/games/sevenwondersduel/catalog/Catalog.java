package io.turnbase.games.sevenwondersduel.catalog;

import io.turnbase.games.sevenwondersduel.id.ComponentId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Lookup of component definitions by id, kept in registration order. */
public final class Catalog {

  private final Map<ComponentId, Component> byId;

  Catalog(Map<ComponentId, Component> byId) {
    this.byId = new LinkedHashMap<>(byId);
  }

  public Component get(ComponentId id) {
    Component component = byId.get(id);
    if (component == null) {
      throw new IllegalArgumentException("unknown component: " + id.value());
    }
    return component;
  }

  public List<Component> ofType(ComponentType type) {
    return byId.values().stream().filter(c -> c.type() == type).toList();
  }
}
