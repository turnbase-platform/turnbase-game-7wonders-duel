package io.turnbase.games.sevenwondersduel.rng;

/**
 * A position in the seeded random stream (base.md §10.8). The stream is fully determined by the
 * session seed ({@code InitContext.seed()}); {@code draws} is how many values have been consumed,
 * so persisting the cursor persists the position.
 *
 * @param seed the session seed
 * @param draws values consumed from the stream so far
 */
public record RngCursor(long seed, long draws) {

  public RngCursor {
    if (draws < 0) {
      throw new IllegalArgumentException("draws must not be negative: " + draws);
    }
  }

  /** The beginning of the stream for {@code seed}. */
  public static RngCursor start(long seed) {
    return new RngCursor(seed, 0);
  }
}
