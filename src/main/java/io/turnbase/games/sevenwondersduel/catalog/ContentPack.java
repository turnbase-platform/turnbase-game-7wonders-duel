package io.turnbase.games.sevenwondersduel.catalog;

import io.turnbase.games.sevenwondersduel.id.LayoutId;
import io.turnbase.games.sevenwondersduel.id.PackId;
import io.turnbase.games.sevenwondersduel.structure.Layout;
import io.turnbase.games.sevenwondersduel.zone.ZoneKind;
import java.util.List;
import java.util.Map;

/**
 * A unit of game content: the base game and each expansion is one pack. Packs only add zones and
 * components; the engine itself never changes when a pack is added.
 */
public interface ContentPack {

  PackId id();

  List<ZoneKind> zoneKinds();

  List<Component> components();

  /** Structure layouts this pack adds; most packs add none. */
  default List<Layout> layouts() {
    return List.of();
  }

  /** The layout each Age number is dealt into; most packs map none. */
  default Map<Integer, LayoutId> ageLayouts() {
    return Map.of();
  }
}
