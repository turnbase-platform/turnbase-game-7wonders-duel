package io.turnbase.games.sevenwondersduel.effect;

import java.util.List;

/** A mechanic that can describe itself to a client as {@link Badge}s. */
public interface Badged {

  /** What to show for this mechanic; empty when it has nothing to show. */
  default List<Badge> badges() {
    return List.of();
  }
}
