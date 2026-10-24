package io.turnbase.games.sevenwondersduel.content;

import io.turnbase.games.sevenwondersduel.catalog.Tag;
import java.util.List;

/** The colours printed on the base cards (B-BACK-2: not the back group). */
public final class Colors {

  public static final Tag BROWN = Tag.color("brown");
  public static final Tag GREY = Tag.color("grey");
  public static final Tag BLUE = Tag.color("blue");
  public static final Tag GREEN = Tag.color("green");
  public static final Tag YELLOW = Tag.color("yellow");
  public static final Tag RED = Tag.color("red");
  public static final Tag PURPLE = Tag.color("purple");

  public static final List<Tag> ALL = List.of(BROWN, GREY, BLUE, GREEN, YELLOW, RED, PURPLE);

  private Colors() {}
}
