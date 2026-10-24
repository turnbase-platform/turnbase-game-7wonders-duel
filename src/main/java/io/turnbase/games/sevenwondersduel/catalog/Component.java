package io.turnbase.games.sevenwondersduel.catalog;

import io.turnbase.games.sevenwondersduel.effect.Effect;
import io.turnbase.games.sevenwondersduel.effect.Modifier;
import io.turnbase.games.sevenwondersduel.effect.Trigger;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import java.util.List;
import java.util.Set;

/** Static definition of one game component: a card, a Wonder or a token. */
public interface Component {

  ComponentId id();

  String name();

  ComponentType type();

  /** Labels by which rules find this component (Age, guild, progress token...). */
  default Set<Tag> tags() {
    return Set.of();
  }

  /** Instant effects applied once, in printed order, when the component is acquired (B-EFF-1). */
  default List<Effect> effects() {
    return List.of();
  }

  /** Static effects, queried while the component is live for a player. */
  default List<Modifier> modifiers() {
    return List.of();
  }

  /** Reactions to events, active while the component is live for a player. */
  default List<Trigger<?>> triggers() {
    return List.of();
  }
}
