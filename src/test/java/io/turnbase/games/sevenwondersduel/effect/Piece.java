package io.turnbase.games.sevenwondersduel.effect;

import io.turnbase.games.sevenwondersduel.catalog.Component;
import io.turnbase.games.sevenwondersduel.catalog.ComponentType;
import io.turnbase.games.sevenwondersduel.catalog.Tag;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import java.util.List;
import java.util.Set;

/** A test component with tags and effects, for effects that find their targets by tag. */
record Piece(ComponentId id, Set<Tag> tags, List<Effect> effects) implements Component {

  static Piece of(String id, Tag... tags) {
    return new Piece(ComponentId.of(id), Set.of(tags), List.of());
  }

  Piece with(Effect... effects) {
    return new Piece(id, tags, List.of(effects));
  }

  @Override
  public String name() {
    return id.value();
  }

  @Override
  public ComponentType type() {
    return ComponentType.CARD;
  }
}
