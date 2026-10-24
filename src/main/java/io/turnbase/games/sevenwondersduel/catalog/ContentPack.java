package io.turnbase.games.sevenwondersduel.catalog;

import io.turnbase.games.sevenwondersduel.id.PackId;
import io.turnbase.games.sevenwondersduel.zone.ZoneKind;
import java.util.List;

/**
 * A unit of game content: the base game and each expansion is one pack. Packs only add zones and
 * components; the engine itself never changes when a pack is added.
 */
public interface ContentPack {

  PackId id();

  List<ZoneKind> zoneKinds();

  List<Component> components();
}
