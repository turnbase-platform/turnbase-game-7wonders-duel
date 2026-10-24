package io.turnbase.games.sevenwondersduel.view;

import java.util.List;

/** Every component of the rule set, in catalog order: the client's lookup for any id it meets. */
public record CatalogView(List<CatalogEntry> entries) {

  public CatalogView {
    entries = List.copyOf(entries);
  }
}
