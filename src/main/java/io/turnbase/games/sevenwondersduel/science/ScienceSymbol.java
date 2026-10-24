package io.turnbase.games.sevenwondersduel.science;

/** A scientific symbol, such as the wheel or the scales (B-SCI-2). */
public record ScienceSymbol(String id) {

  public ScienceSymbol {
    if (id == null || id.isBlank()) {
      throw new IllegalArgumentException("ScienceSymbol id must not be blank");
    }
  }
}
