package io.turnbase.games.sevenwondersduel.id;

/** The id that names a card-back group (B-BACK-1); packs declare them, cards point at one. */
public record BackId(String value) {

  public BackId {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("BackId must not be blank");
    }
  }

  public static BackId of(String value) {
    return new BackId(value);
  }
}
