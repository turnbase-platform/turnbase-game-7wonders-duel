package io.turnbase.games.sevenwondersduel.catalog;

import io.turnbase.games.sevenwondersduel.id.PackId;
import io.turnbase.games.sevenwondersduel.id.ZoneId;
import io.turnbase.games.sevenwondersduel.zone.Visibility;
import io.turnbase.games.sevenwondersduel.zone.ZoneKind;
import java.util.List;

/** The base game's content. Zones only for now; card, Wonder and token data arrive later. */
public final class BasePack implements ContentPack {

  @Override
  public PackId id() {
    return PackId.of("base");
  }

  @Override
  public List<ZoneKind> zoneKinds() {
    return List.of(
        zone("deck", Visibility.NONE, true, false),
        zone("structure.up", Visibility.PUBLIC, false, false),
        // Face-down cards live apart so visibility stays a per-zone property; revealing is a move.
        zone("structure.down", Visibility.NONE, false, false),
        live("city"),
        zone("underWonder", Visibility.NONE, false, false),
        zone("discard", Visibility.PUBLIC, true, false),
        zone("draft", Visibility.PUBLIC, false, false),
        zone("wonders.unbuilt", Visibility.PUBLIC, false, false),
        live("wonders.built"),
        zone("board", Visibility.PUBLIC, false, false),
        zone("box", Visibility.NONE, false, false),
        zone("destroyed", Visibility.PUBLIC, false, true));
  }

  @Override
  public List<Component> components() {
    return List.of();
  }

  private static ZoneKind live(String id) {
    return new ZoneKind(ZoneId.of(id), Visibility.PUBLIC, false, false, true);
  }

  private static ZoneKind zone(
      String id, Visibility visibility, boolean ordered, boolean terminal) {
    return new ZoneKind(ZoneId.of(id), visibility, ordered, terminal);
  }
}
