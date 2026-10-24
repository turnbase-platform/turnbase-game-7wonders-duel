package io.turnbase.games.sevenwondersduel.turn;

import io.turnbase.games.sevenwondersduel.catalog.Component;
import io.turnbase.games.sevenwondersduel.catalog.ComponentType;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.catalog.Tag;
import io.turnbase.games.sevenwondersduel.cost.Cost;
import io.turnbase.games.sevenwondersduel.effect.Effect;
import io.turnbase.games.sevenwondersduel.effect.Modifier;
import io.turnbase.games.sevenwondersduel.effect.Trigger;
import io.turnbase.games.sevenwondersduel.engine.Discard;
import io.turnbase.games.sevenwondersduel.engine.Engine;
import io.turnbase.games.sevenwondersduel.id.BackId;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.setup.SetupFixtures;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.structure.Structure;
import io.turnbase.games.sevenwondersduel.zone.Placements;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Base-sized catalogs in which a few chosen components carry cost, effects and modifiers. */
public final class TurnFixtures {

  public record Part(
      ComponentId id,
      ComponentType type,
      Set<Tag> tags,
      Cost cost,
      List<Effect> effects,
      List<Modifier> modifiers,
      List<Trigger<?>> triggers,
      Optional<BackId> back)
      implements Component {

    public static Part of(String id, ComponentType type) {
      return new Part(
          ComponentId.of(id),
          type,
          Set.of(),
          Cost.FREE,
          List.of(),
          List.of(),
          List.of(),
          Optional.empty());
    }

    public static Part card(String id) {
      return of(id, ComponentType.CARD);
    }

    public static Part wonder(String id) {
      return of(id, ComponentType.WONDER);
    }

    public Part costing(Cost cost) {
      return new Part(id, type, tags, cost, effects, modifiers, triggers, back);
    }

    public Part withEffects(Effect... effects) {
      return new Part(id, type, tags, cost, List.of(effects), modifiers, triggers, back);
    }

    public Part withModifiers(Modifier... modifiers) {
      return new Part(id, type, tags, cost, effects, List.of(modifiers), triggers, back);
    }

    public Part withTriggers(Trigger<?>... triggers) {
      return new Part(id, type, tags, cost, effects, modifiers, List.of(triggers), back);
    }

    public Part tagged(Tag... tags) {
      return new Part(id, type, Set.of(tags), cost, effects, modifiers, triggers, back);
    }

    @Override
    public String name() {
      return id.value();
    }
  }

  /**
   * The base-sized fixture catalog in which components with the same id as one of {@code parts} are
   * replaced in place (so dealing is unchanged) and the others are added, never dealt.
   */
  public static RuleSet rulesWith(Component... parts) {
    List<Component> all = new ArrayList<>(SetupFixtures.pieces(23, 23, 20, 7, 12, 10));
    for (Component part : parts) {
      int at = indexOf(all, part.id());
      if (at >= 0) {
        all.set(at, inheritingBack(part, all.get(at)));
      } else {
        all.add(part);
      }
    }
    return SetupFixtures.rules(all);
  }

  private static Component inheritingBack(Component part, Component replaced) {
    if (part instanceof Part p && p.back().isEmpty()) {
      return new Part(
          p.id(),
          p.type(),
          p.tags(),
          p.cost(),
          p.effects(),
          p.modifiers(),
          p.triggers(),
          replaced.back());
    }
    return part;
  }

  /** Cards still laid out in the structure, face up or down. */
  public static int inStructure(DuelState state) {
    return Placements.idsIn(state.placements(), Structure.UP).size()
        + Placements.idsIn(state.placements(), Structure.DOWN).size();
  }

  private static int indexOf(List<Component> all, ComponentId id) {
    for (int i = 0; i < all.size(); i++) {
      if (all.get(i).id().equals(id)) {
        return i;
      }
    }
    return -1;
  }

  /** An Age I card as the fixture catalog declares it, to be replaced by a richer {@link Part}. */
  public static Part ageOne(ComponentId id) {
    return Part.card(id.value()).tagged(Tag.age(1));
  }

  public static ComponentId takeable(DuelState state) {
    return Structure.takeable(state.placements()).get(0);
  }

  /** The seed-{@code seed} Age I card that is taken last when everyone discards the first. */
  public static ComponentId lastCard(long seed) {
    RuleSet plain = rulesWith();
    Engine engine = new Engine(plain);
    DuelState state = SetupFixtures.startedAgeOne(plain, seed);
    while (inStructure(state) > 1) {
      state = engine.apply(state, state.active(), new Discard(takeable(state))).state();
    }
    return takeable(state);
  }

  private TurnFixtures() {}
}
