package io.turnbase.games.sevenwondersduel.content;

import io.turnbase.games.sevenwondersduel.id.BackId;
import java.util.List;

/** The base back groups (B-BACK-1), one per Age plus the Guilds. */
public final class Backs {

  public static final BackId BROWN = BackId.of("brown");
  public static final BackId BLUE = BackId.of("blue");
  public static final BackId VIOLET = BackId.of("violet");
  public static final BackId DARK_PURPLE = BackId.of("dark-purple");

  public static final List<BackId> ALL = List.of(BROWN, BLUE, VIOLET, DARK_PURPLE);

  private Backs() {}
}
