package io.turnbase.games.sevenwondersduel.id;

/** The id that names a content pack. */
public record PackId(String value) {

  public PackId {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("PackId must not be blank");
    }
  }

  public static PackId of(String value) {
    return new PackId(value);
  }
}
