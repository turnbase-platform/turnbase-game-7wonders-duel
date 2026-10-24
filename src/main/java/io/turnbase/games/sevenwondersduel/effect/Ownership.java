package io.turnbase.games.sevenwondersduel.effect;

import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.zone.Placement;
import io.turnbase.games.sevenwondersduel.zone.ZoneRegistry;
import java.util.List;

/**
 * What a player has in play, derived from placements alone (B-EFF-1: static and triggered effects
 * follow their source, so nothing about them is stored).
 */
public final class Ownership {

  private Ownership() {}

  /** Components in live zones keyed by {@code player}, in placement order. */
  public static List<ComponentId> liveComponents(
      DuelState state, ZoneRegistry zones, PlayerId player) {
    String key = player.value().toString();
    return state.placements().stream()
        .filter(p -> p.at().key().equals(key) && zones.kind(p.at().zone()).live())
        .map(Placement::component)
        .toList();
  }

  /** The player's live modifiers of type {@code type}, in component placement order. */
  public static <M extends Modifier> List<M> modifiers(
      DuelState state, RuleSet rules, PlayerId player, Class<M> type) {
    return liveComponents(state, rules.zones(), player).stream()
        .flatMap(id -> rules.catalog().get(id).modifiers().stream())
        .filter(type::isInstance)
        .map(type::cast)
        .toList();
  }
}
