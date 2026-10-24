package io.turnbase.games.sevenwondersduel.catalog;

import io.turnbase.games.sevenwondersduel.cost.Cost;
import io.turnbase.games.sevenwondersduel.effect.Effect;
import io.turnbase.games.sevenwondersduel.effect.Modifier;
import io.turnbase.games.sevenwondersduel.effect.Trigger;
import io.turnbase.games.sevenwondersduel.id.BackId;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import java.util.List;
import java.util.Optional;
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

  /**
   * The back group of a card, public while it lies face down (B-BACK-1). Fixed, like the name and
   * the colour; a different attribute from the colour (B-BACK-2).
   */
  default Optional<BackId> back() {
    return Optional.empty();
  }

  /** What constructing this component costs (B-COST-1..5); free unless stated. */
  default Cost cost() {
    return Cost.FREE;
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
