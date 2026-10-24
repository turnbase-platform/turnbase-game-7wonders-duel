package io.turnbase.games.sevenwondersduel.catalog;

/**
 * A label on a component by which rules find content without naming it: setup picks cards by Age
 * and kind, never by id, so an expansion adds its own tags without touching core code.
 */
public record Tag(String value) {

  public static final Tag GUILD = new Tag("guild");
  public static final Tag WONDER = new Tag("wonder");
  public static final Tag PROGRESS = new Tag("progress");

  public Tag {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Tag must not be blank");
    }
  }

  public static Tag of(String value) {
    return new Tag(value);
  }

  /** The tag of the cards printed in {@code colour}; an expansion adds its own colours. */
  public static Tag color(String colour) {
    return new Tag("color:" + colour);
  }

  /** The tag of the cards that belong to Age {@code number}. */
  public static Tag age(int number) {
    return new Tag("age:" + number);
  }
}
