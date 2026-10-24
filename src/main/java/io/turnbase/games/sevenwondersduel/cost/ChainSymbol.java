package io.turnbase.games.sevenwondersduel.cost;

/** A symbol that, once in a city, makes the cards it frees free to construct (B-COST-5). */
public record ChainSymbol(String id) {

  public ChainSymbol {
    if (id == null || id.isBlank()) {
      throw new IllegalArgumentException("ChainSymbol id must not be blank");
    }
  }
}
