package io.turnbase.games.sevenwondersduel.id;

/** The id that names a kind of place a component can be. */
public record ZoneId(String value) {

  public ZoneId {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("ZoneId must not be blank");
    }
  }

  public static ZoneId of(String value) {
    return new ZoneId(value);
  }
}
