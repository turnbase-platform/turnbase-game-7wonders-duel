package io.turnbase.games.sevenwondersduel.cost;

import io.turnbase.games.sevenwondersduel.catalog.Component;
import io.turnbase.games.sevenwondersduel.catalog.ComponentType;
import io.turnbase.games.sevenwondersduel.catalog.Tag;
import java.util.List;
import java.util.function.Predicate;

/** Picks the components a {@link Reduction} applies to, and says in words which ones. */
@FunctionalInterface
public interface Selector extends Predicate<Component> {

  /** What is selected, for a client to show; empty for a selector that does not describe itself. */
  default List<String> labels() {
    return List.of();
  }

  /** Every Wonder. */
  static Selector wonders() {
    return new Wonders();
  }

  /** Every component carrying {@code tag}. */
  static Selector tagged(Tag tag) {
    return new Tagged(tag);
  }

  /** Wonders; a record so that equal cards compare equal. */
  record Wonders() implements Selector {
    @Override
    public boolean test(Component component) {
      return component.type() == ComponentType.WONDER;
    }

    @Override
    public List<String> labels() {
      return List.of(Tag.WONDER.value());
    }
  }

  /** Components by tag; a record so that equal cards compare equal. */
  record Tagged(Tag tag) implements Selector {
    @Override
    public boolean test(Component component) {
      return component.tags().contains(tag);
    }

    @Override
    public List<String> labels() {
      return List.of(tag.value());
    }
  }
}
