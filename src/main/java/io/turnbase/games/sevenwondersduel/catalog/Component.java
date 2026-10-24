package io.turnbase.games.sevenwondersduel.catalog;

import io.turnbase.games.sevenwondersduel.id.ComponentId;

/** Static definition of one game component: a card, a Wonder or a token. */
public interface Component {

  ComponentId id();

  String name();

  ComponentType type();
}
