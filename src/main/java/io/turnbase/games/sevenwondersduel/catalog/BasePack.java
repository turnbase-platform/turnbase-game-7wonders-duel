package io.turnbase.games.sevenwondersduel.catalog;

import io.turnbase.games.sevenwondersduel.id.LayoutId;
import io.turnbase.games.sevenwondersduel.id.PackId;
import io.turnbase.games.sevenwondersduel.id.ZoneId;
import io.turnbase.games.sevenwondersduel.structure.Layout;
import io.turnbase.games.sevenwondersduel.zone.Visibility;
import io.turnbase.games.sevenwondersduel.zone.ZoneKind;
import io.turnbase.games.sevenwondersduel.zone.Zones;
import java.util.List;
import java.util.Map;

/**
 * The base game's content. Zones and Age layouts for now; card, Wonder and token data arrive later.
 */
public final class BasePack implements ContentPack {

  @Override
  public PackId id() {
    return PackId.of("base");
  }

  @Override
  public List<ZoneKind> zoneKinds() {
    return List.of(
        zone(Zones.DECK, Visibility.NONE, true, false),
        zone(Zones.STRUCTURE_UP, Visibility.PUBLIC, false, false),
        // Face-down cards live apart so visibility stays a per-zone property; revealing is a move.
        zone(Zones.STRUCTURE_DOWN, Visibility.NONE, false, false),
        live(Zones.CITY),
        zone(Zones.UNDER_WONDER, Visibility.NONE, false, false),
        zone(Zones.DISCARD, Visibility.PUBLIC, true, false),
        zone(Zones.DRAFT, Visibility.PUBLIC, false, false),
        zone(Zones.WONDERS_UNBUILT, Visibility.PUBLIC, false, false),
        live(Zones.WONDERS_BUILT),
        zone(Zones.BOARD, Visibility.PUBLIC, false, false),
        zone(Zones.BOX, Visibility.NONE, false, false),
        zone(Zones.MILITARY, Visibility.PUBLIC, false, false),
        zone(Zones.DESTROYED, Visibility.PUBLIC, false, true));
  }

  @Override
  public List<Component> components() {
    return List.of();
  }

  @Override
  public List<Layout> layouts() {
    return List.of(
        layout(
            "age1",
            up(4, 6),
            down(3, 5, 7),
            up(2, 4, 6, 8),
            down(1, 3, 5, 7, 9),
            up(0, 2, 4, 6, 8, 10)),
        layout(
            "age2",
            up(0, 2, 4, 6, 8, 10),
            down(1, 3, 5, 7, 9),
            up(2, 4, 6, 8),
            down(3, 5, 7),
            up(4, 6)),
        layout(
            "age3",
            up(2, 4),
            down(1, 3, 5),
            up(0, 2, 4, 6),
            down(1, 5),
            up(0, 2, 4, 6),
            down(1, 3, 5),
            up(2, 4)));
  }

  @Override
  public Map<Integer, LayoutId> ageLayouts() {
    return Map.of(1, LayoutId.of("age1"), 2, LayoutId.of("age2"), 3, LayoutId.of("age3"));
  }

  private static Layout layout(String id, Layout.Row... rows) {
    return new Layout(LayoutId.of(id), List.of(rows));
  }

  private static Layout.Row up(Integer... xs) {
    return new Layout.Row(true, List.of(xs));
  }

  private static Layout.Row down(Integer... xs) {
    return new Layout.Row(false, List.of(xs));
  }

  private static ZoneKind live(ZoneId id) {
    return new ZoneKind(id, Visibility.PUBLIC, false, false, true);
  }

  private static ZoneKind zone(
      ZoneId id, Visibility visibility, boolean ordered, boolean terminal) {
    return new ZoneKind(id, visibility, ordered, terminal);
  }
}
