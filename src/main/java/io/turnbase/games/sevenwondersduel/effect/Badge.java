package io.turnbase.games.sevenwondersduel.effect;

import java.util.List;

/**
 * What a mechanic tells a client about itself, so a card can be drawn without the client knowing
 * the mechanic: a {@code kind} the client has an icon for, an {@code amount}, what it is about
 * ({@code of}: tags, resources or symbols) and, for counted mechanics, how many of {@code of} make
 * one unit ({@code per}) and whether the larger of both cities counts ({@code larger}).
 */
public record Badge(String kind, int amount, List<String> of, int per, boolean larger) {

  public Badge {
    of = List.copyOf(of);
  }

  public static Badge of(String kind) {
    return of(kind, 0);
  }

  public static Badge of(String kind, int amount, String... of) {
    return new Badge(kind, amount, List.of(of), 1, false);
  }
}
