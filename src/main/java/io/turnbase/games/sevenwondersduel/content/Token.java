package io.turnbase.games.sevenwondersduel.content;

import io.turnbase.games.sevenwondersduel.catalog.Component;
import io.turnbase.games.sevenwondersduel.catalog.ComponentType;
import io.turnbase.games.sevenwondersduel.catalog.Tag;
import io.turnbase.games.sevenwondersduel.effect.Effect;
import io.turnbase.games.sevenwondersduel.effect.Modifier;
import io.turnbase.games.sevenwondersduel.effect.Trigger;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * A token as declarative data: a progress token or a military token, told apart by its tag.
 * Effects, modifiers and triggers are added with the fluent methods, each returning a new Token.
 */
public record Token(
    ComponentId id,
    String name,
    String description,
    Set<Tag> tags,
    List<Effect> effects,
    List<Modifier> modifiers,
    List<Trigger<?>> triggers)
    implements Component {

  public Token {
    tags = Set.copyOf(tags);
    effects = List.copyOf(effects);
    modifiers = List.copyOf(modifiers);
    triggers = List.copyOf(triggers);
  }

  /** A token with no behaviour yet, id slugged from {@code name}, carrying {@code tags}. */
  public static Token of(String name, Tag... tags) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("token name must not be blank");
    }
    String slug = name.trim().toLowerCase(Locale.ROOT).replace(' ', '-');
    return new Token(
        ComponentId.of(slug), name.trim(), "", Set.of(tags), List.of(), List.of(), List.of());
  }

  /** The token with the sentence that tells a player what it does. */
  public Token described(String text) {
    return new Token(id, name, text, tags, effects, modifiers, triggers);
  }

  public Token effects(Effect... more) {
    return new Token(
        id, name, description, tags, Fluent.append(effects, more), modifiers, triggers);
  }

  public Token modifiers(Modifier... more) {
    return new Token(
        id, name, description, tags, effects, Fluent.append(modifiers, more), triggers);
  }

  public Token triggers(Trigger<?>... more) {
    return new Token(
        id, name, description, tags, effects, modifiers, Fluent.append(triggers, more));
  }

  @Override
  public ComponentType type() {
    return ComponentType.TOKEN;
  }
}
