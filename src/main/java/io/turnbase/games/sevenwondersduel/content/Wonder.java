package io.turnbase.games.sevenwondersduel.content;

import io.turnbase.games.sevenwondersduel.catalog.Component;
import io.turnbase.games.sevenwondersduel.catalog.ComponentType;
import io.turnbase.games.sevenwondersduel.catalog.Tag;
import io.turnbase.games.sevenwondersduel.cost.Cost;
import io.turnbase.games.sevenwondersduel.cost.Production;
import io.turnbase.games.sevenwondersduel.cost.Resource;
import io.turnbase.games.sevenwondersduel.effect.Effect;
import io.turnbase.games.sevenwondersduel.effect.GainCoins;
import io.turnbase.games.sevenwondersduel.effect.GrantPlayAgain;
import io.turnbase.games.sevenwondersduel.effect.Modifier;
import io.turnbase.games.sevenwondersduel.effect.Shields;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.scoring.Points;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * A Wonder as declarative data: tagged {@link Tag#WONDER}, with no back (it is never hidden).
 * Costs, effects and modifiers are added with the fluent methods, each returning a new Wonder.
 */
public record Wonder(
    ComponentId id, String name, Cost cost, List<Effect> effects, List<Modifier> modifiers)
    implements Component {

  public Wonder {
    effects = List.copyOf(effects);
    modifiers = List.copyOf(modifiers);
  }

  /** A free Wonder with no effects, id slugged from {@code name}. */
  public static Wonder of(String name) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("wonder name must not be blank");
    }
    String slug = name.trim().toLowerCase(Locale.ROOT).replace(' ', '-');
    return new Wonder(ComponentId.of(slug), name.trim(), Cost.FREE, List.of(), List.of());
  }

  public Wonder costing(Cost cost) {
    return new Wonder(id, name, cost, effects, modifiers);
  }

  public Wonder effects(Effect... more) {
    return new Wonder(id, name, cost, Fluent.append(effects, more), modifiers);
  }

  public Wonder modifiers(Modifier... more) {
    return new Wonder(id, name, cost, effects, Fluent.append(modifiers, more));
  }

  public Wonder shields(int count) {
    return effects(new Shields(count));
  }

  public Wonder coins(int amount) {
    return effects(new GainCoins(amount));
  }

  public Wonder playAgain() {
    return effects(new GrantPlayAgain());
  }

  public Wonder worth(int points) {
    return modifiers(Points.fixed(points));
  }

  /** One unit of any one of {@code alternatives}; a Wonder never raises the opponent's price. */
  public Wonder producingOneOf(Resource... alternatives) {
    return modifiers(Production.choice(false, alternatives));
  }

  @Override
  public ComponentType type() {
    return ComponentType.WONDER;
  }

  @Override
  public Set<Tag> tags() {
    return Set.of(Tag.WONDER);
  }
}
