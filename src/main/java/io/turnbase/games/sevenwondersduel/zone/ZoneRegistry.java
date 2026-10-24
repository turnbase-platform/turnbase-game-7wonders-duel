package io.turnbase.games.sevenwondersduel.zone;

import io.turnbase.games.sevenwondersduel.id.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** The zone kinds known to a rule set, looked up by id. */
public final class ZoneRegistry {

  private final Map<ZoneId, ZoneKind> kinds;

  private ZoneRegistry(Map<ZoneId, ZoneKind> kinds) {
    this.kinds = kinds;
  }

  /** Builds a registry; a repeated zone id is rejected. */
  public static ZoneRegistry of(List<ZoneKind> kinds) {
    Map<ZoneId, ZoneKind> byId = new LinkedHashMap<>();
    for (ZoneKind kind : kinds) {
      if (byId.putIfAbsent(kind.id(), kind) != null) {
        throw new IllegalArgumentException("duplicate zone id: " + kind.id().value());
      }
    }
    return new ZoneRegistry(Map.copyOf(byId));
  }

  public ZoneKind kind(ZoneId id) {
    ZoneKind kind = kinds.get(id);
    if (kind == null) {
      throw new IllegalArgumentException("unknown zone: " + id.value());
    }
    return kind;
  }

  public boolean knows(ZoneId id) {
    return kinds.containsKey(id);
  }
}
