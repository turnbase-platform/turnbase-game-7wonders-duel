package io.turnbase.games.sevenwondersduel.content;

import io.turnbase.games.sevenwondersduel.catalog.Component;
import io.turnbase.games.sevenwondersduel.catalog.ComponentType;
import io.turnbase.games.sevenwondersduel.catalog.Tag;
import io.turnbase.games.sevenwondersduel.cost.ChainSymbol;
import io.turnbase.games.sevenwondersduel.cost.Cost;
import io.turnbase.games.sevenwondersduel.cost.Production;
import io.turnbase.games.sevenwondersduel.cost.Resource;
import io.turnbase.games.sevenwondersduel.effect.Effect;
import io.turnbase.games.sevenwondersduel.effect.GainCoins;
import io.turnbase.games.sevenwondersduel.effect.Modifier;
import io.turnbase.games.sevenwondersduel.effect.Shields;
import io.turnbase.games.sevenwondersduel.id.BackId;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.science.ScienceSymbol;
import io.turnbase.games.sevenwondersduel.science.Symbol;
import io.turnbase.games.sevenwondersduel.scoring.Points;
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
    return free(name, Set.of(Tag.age(age), colour), back);
  }

  /** A free guild with no effects: purple, tagged {@link Tag#GUILD}, and in no Age (B-SETUP). */
  public static Card guild(String name) {
    return free(name, Set.of(Tag.GUILD, Colors.PURPLE), Backs.DARK_PURPLE);
  }

  private static Card free(String name, Set<Tag> tags, BackId back) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("card name must not be blank");
    }
    String slug = name.trim().toLowerCase(Locale.ROOT).replace(' ', '-');
    return new Card(ComponentId.of(slug), name.trim(), tags, back, Cost.FREE, List.of(), List.of());
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

  /** {@code count} units of {@code resource}, which raise the opponent's price (B-TRADE-2). */
  public Card producing(Resource resource, int count) {
    return modifiers(Production.fixed(resource, count, true));
  }

  public Card shields(int count) {
    return effects(new Shields(count));
  }

  public Card coins(int amount) {
    return effects(new GainCoins(amount));
  }

  public Card worth(int points) {
    return modifiers(Points.fixed(points));
  }

  public Card showing(ScienceSymbol symbol) {
    return modifiers(new Symbol(symbol));
  }

  /** The card shows these chain symbols, freeing the cards that name them. */
  public Card giving(ChainSymbol... symbols) {
    return modifiers(Chains.gives(symbols));
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
