package io.turnbase.games.sevenwondersduel.id;

/** The id that names a structure layout. */
public record LayoutId(String value) {

  public LayoutId {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("LayoutId must not be blank");
    }
  }

  public static LayoutId of(String value) {
    return new LayoutId(value);
  }
}
