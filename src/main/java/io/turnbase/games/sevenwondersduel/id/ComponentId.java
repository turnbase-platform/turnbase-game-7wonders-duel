package io.turnbase.games.sevenwondersduel.id;

/** The id that identifies a card, Wonder or token in the catalog. */
public record ComponentId(String value) {

  public ComponentId {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("ComponentId must not be blank");
    }
  }

  public static ComponentId of(String value) {
    return new ComponentId(value);
  }
}
