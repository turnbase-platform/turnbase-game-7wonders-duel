package io.turnbase.games.sevenwondersduel.view;

import io.turnbase.games.sevenwondersduel.id.PlayerId;

/** Who is looking: one of the two players, or somebody who only watches. */
public sealed interface Viewer {

  /** A seated player, who also sees whatever they have seen before. */
  record Seat(PlayerId player) implements Viewer {}

  /** A spectator, who sees only what is public. */
  record Spectator() implements Viewer {}
}
