package io.turnbase.games.sevenwondersduel.content;

import io.turnbase.games.sevenwondersduel.catalog.Component;
import io.turnbase.games.sevenwondersduel.catalog.ComponentType;
import io.turnbase.games.sevenwondersduel.catalog.Tag;
import io.turnbase.games.sevenwondersduel.effect.Effect;
import io.turnbase.games.sevenwondersduel.effect.Modifier;
import io.turnbase.games.sevenwondersduel.effect.Trigger;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import java.util.ArrayList;
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

  /** A token with no behaviour yet, id slugged from {@code name}. */
  public static Token of(String name, Tag tag) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("token name must not be blank");
    }
    String slug = name.trim().toLowerCase(Locale.ROOT).replace(' ', '-');
    return new Token(
        ComponentId.of(slug), name.trim(), Set.of(tag), List.of(), List.of(), List.of());
  }

  public Token effects(Effect... more) {
    return new Token(id, name, tags, append(effects, more), modifiers, triggers);
  }

  public Token modifiers(Modifier... more) {
    return new Token(id, name, tags, effects, append(modifiers, more), triggers);
  }

  public Token triggers(Trigger<?>... more) {
    return new Token(id, name, tags, effects, modifiers, append(triggers, more));
  }

  @Override
  public ComponentType type() {
    return ComponentType.TOKEN;
  }

  private static <T> List<T> append(List<T> list, T[] more) {
    List<T> all = new ArrayList<>(list);
    all.addAll(List.of(more));
    return all;
  }
}
