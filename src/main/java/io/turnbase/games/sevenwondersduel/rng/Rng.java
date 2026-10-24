package io.turnbase.games.sevenwondersduel.rng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Pure draws from the seeded stream: every call takes a cursor and returns the value together with
 * the advanced cursor, so all randomness — setup shuffles and random draws during play — consumes
 * one stream and stays replayable (base.md §10.8).
 */
public final class Rng {

  private Rng() {}

  /** A drawn value and the cursor positioned after it. */
  public record Drawn<T>(T value, RngCursor next) {}

  /** A uniformly distributed integer in {@code [0, bound)}; consumes one draw. */
  public static Drawn<Integer> nextInt(RngCursor cursor, int bound) {
    Random random = new Random(cursor.seed());
    for (long i = 0; i < cursor.draws(); i++) {
      random.nextInt();
    }
    return new Drawn<>(random.nextInt(bound), new RngCursor(cursor.seed(), cursor.draws() + 1));
  }

  /** A shuffled copy of {@code items} (Fisher–Yates); the input is not modified. */
  public static <T> Drawn<List<T>> shuffle(List<T> items, RngCursor cursor) {
    List<T> result = new ArrayList<>(items);
    RngCursor current = cursor;
    for (int i = result.size() - 1; i > 0; i--) {
      Drawn<Integer> pick = nextInt(current, i + 1);
      Collections.swap(result, i, pick.value());
      current = pick.next();
    }
    return new Drawn<>(List.copyOf(result), current);
  }
}
