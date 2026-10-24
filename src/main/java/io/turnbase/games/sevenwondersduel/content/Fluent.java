package io.turnbase.games.sevenwondersduel.content;

import java.util.ArrayList;
import java.util.List;

/** Shared plumbing of the fluent content records ({@link Card}, {@link Wonder}, {@link Token}). */
final class Fluent {

  /** A new list holding {@code list} followed by {@code more}; neither argument is modified. */
  static <T> List<T> append(List<T> list, T[] more) {
    List<T> all = new ArrayList<>(list);
    all.addAll(List.of(more));
    return all;
  }

  private Fluent() {}
}
