package io.turnbase.games.sevenwondersduel.catalog;

import io.turnbase.games.sevenwondersduel.id.LayoutId;
import io.turnbase.games.sevenwondersduel.id.PackId;
import io.turnbase.games.sevenwondersduel.id.ZoneId;
import io.turnbase.games.sevenwondersduel.structure.Layout;
import io.turnbase.games.sevenwondersduel.zone.Visibility;
import io.turnbase.games.sevenwondersduel.zone.ZoneKind;
import java.util.List;

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

  private static Layout layout(String id, Layout.Row... rows) {
    return new Layout(LayoutId.of(id), List.of(rows));
  }

  private static Layout.Row up(Integer... xs) {
    return new Layout.Row(true, List.of(xs));
  }

  private static Layout.Row down(Integer... xs) {
    return new Layout.Row(false, List.of(xs));
  }

  private static ZoneKind live(String id) {
    return new ZoneKind(ZoneId.of(id), Visibility.PUBLIC, false, false, true);
  }

  private static ZoneKind zone(
      String id, Visibility visibility, boolean ordered, boolean terminal) {
    return new ZoneKind(ZoneId.of(id), visibility, ordered, terminal);
  }
}
