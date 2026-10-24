package io.turnbase.games.sevenwondersduel.turn;

import io.turnbase.games.sevenwondersduel.catalog.Component;
import io.turnbase.games.sevenwondersduel.catalog.ComponentType;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.catalog.Tag;
import io.turnbase.games.sevenwondersduel.cost.Cost;
import io.turnbase.games.sevenwondersduel.effect.Effect;
import io.turnbase.games.sevenwondersduel.effect.Modifier;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.setup.SetupFixtures;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Base-sized catalogs in which a few chosen components carry cost, effects and modifiers. */
public final class TurnFixtures {

  public record Part(
      ComponentId id,
      ComponentType type,
      Set<Tag> tags,
      Cost cost,
      List<Effect> effects,
      List<Modifier> modifiers)
      implements Component {

    public static Part of(String id, ComponentType type) {
      return new Part(ComponentId.of(id), type, Set.of(), Cost.FREE, List.of(), List.of());
    }

    public static Part card(String id) {
      return of(id, ComponentType.CARD);
    }

    public static Part wonder(String id) {
      return of(id, ComponentType.WONDER);
    }

    public Part costing(Cost cost) {
      return new Part(id, type, tags, cost, effects, modifiers);
    }

    public Part withEffects(Effect... effects) {
      return new Part(id, type, tags, cost, List.of(effects), modifiers);
    }

    public Part withModifiers(Modifier... modifiers) {
      return new Part(id, type, tags, cost, effects, List.of(modifiers));
    }

    public Part tagged(Tag... tags) {
      return new Part(id, type, Set.of(tags), cost, effects, modifiers);
    }

    @Override
    public String name() {
      return id.value();
    }
  }

  /** The base-sized fixture catalog plus {@code extra} components that are never dealt. */
  public static RuleSet rulesWith(Component... extra) {
    List<Component> all = new ArrayList<>(SetupFixtures.pieces(23, 23, 20, 7, 12, 10));
    all.addAll(List.of(extra));
    return SetupFixtures.rules(all);
  }

  private TurnFixtures() {}
}
