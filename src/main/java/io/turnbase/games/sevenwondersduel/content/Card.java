package io.turnbase.games.sevenwondersduel.content;

import io.turnbase.games.sevenwondersduel.catalog.Component;
import io.turnbase.games.sevenwondersduel.catalog.ComponentType;
import io.turnbase.games.sevenwondersduel.catalog.Tag;
import io.turnbase.games.sevenwondersduel.cost.ChainSymbol;
import io.turnbase.games.sevenwondersduel.cost.Cost;
import io.turnbase.games.sevenwondersduel.effect.Effect;
import io.turnbase.games.sevenwondersduel.effect.Modifier;
import io.turnbase.games.sevenwondersduel.id.BackId;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * A building card as declarative data: the name, Age and colour are fixed, and costs, effects and
 * modifiers are added with the {@code with}-style methods, each returning a new card.
 */
public record Card(
    ComponentId id,
    String name,
    Set<Tag> tags,
    BackId backGroup,
    Cost cost,
    List<Effect> effects,
    List<Modifier> modifiers)
    implements Component {

  public Card {
    tags = Set.copyOf(tags);
    effects = List.copyOf(effects);
    modifiers = List.copyOf(modifiers);
  }

  /** A free card with no effects, id slugged from {@code name}. */
  public static Card of(String name, int age, Tag colour, BackId back) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("card name must not be blank");
    }
    String slug = name.trim().toLowerCase(Locale.ROOT).replace(' ', '-');
    return new Card(
        ComponentId.of(slug),
        name.trim(),
        Set.of(Tag.age(age), colour),
        back,
        Cost.FREE,
        List.of(),
        List.of());
  }

  public Card costing(Cost cost) {
    return new Card(id, name, tags, backGroup, cost, effects, modifiers);
  }

  /** Makes the card free for a city that holds any of {@code symbols}. */
  public Card freedBy(ChainSymbol... symbols) {
    return costing(cost.withFreedBy(symbols));
  }

  public Card effects(Effect... more) {
    return new Card(id, name, tags, backGroup, cost, append(effects, more), modifiers);
  }

  public Card modifiers(Modifier... more) {
    return new Card(id, name, tags, backGroup, cost, effects, append(modifiers, more));
  }

  @Override
  public ComponentType type() {
    return ComponentType.CARD;
  }

  @Override
  public Optional<BackId> back() {
    return Optional.of(backGroup);
  }

  private static <T> List<T> append(List<T> list, T[] more) {
    List<T> all = new ArrayList<>(list);
    all.addAll(List.of(more));
    return all;
  }
}
