package io.turnbase.games.sevenwondersduel.zone;

import io.turnbase.games.sevenwondersduel.id.ZoneId;
import java.util.List;

/** The zones the base pack defines, named once so no class spells an id twice. */
public final class Zones {

  public static final ZoneId DECK = ZoneId.of("deck");
  public static final ZoneId STRUCTURE_UP = ZoneId.of("structure.up");
  public static final ZoneId STRUCTURE_DOWN = ZoneId.of("structure.down");
  public static final ZoneId CITY = ZoneId.of("city");
  public static final ZoneId UNDER_WONDER = ZoneId.of("underWonder");
  public static final ZoneId DISCARD = ZoneId.of("discard");
  public static final ZoneId DRAFT = ZoneId.of("draft");
  public static final ZoneId WONDERS_UNBUILT = ZoneId.of("wonders.unbuilt");
  public static final ZoneId WONDERS_BUILT = ZoneId.of("wonders.built");
  public static final ZoneId BOARD = ZoneId.of("board");
  public static final ZoneId BOX = ZoneId.of("box");
  public static final ZoneId MILITARY = ZoneId.of("military");
  public static final ZoneId DESTROYED = ZoneId.of("destroyed");

  /** Every base zone, in the order the base pack defines them. */
  public static final List<ZoneId> ALL =
      List.of(
          DECK,
          STRUCTURE_UP,
          STRUCTURE_DOWN,
          CITY,
          UNDER_WONDER,
          DISCARD,
          DRAFT,
          WONDERS_UNBUILT,
          WONDERS_BUILT,
          BOARD,
          BOX,
          MILITARY,
          DESTROYED);

  private Zones() {}
}
