package io.turnbase.games.sevenwondersduel.id;

import java.util.UUID;

/** The id of a seat in the match; the platform player's UUID. */
public record PlayerId(UUID value) {

  public PlayerId {
    if (value == null) {
      throw new IllegalArgumentException("PlayerId must not be null");
    }
  }

  public static PlayerId of(UUID value) {
    return new PlayerId(value);
  }
}
