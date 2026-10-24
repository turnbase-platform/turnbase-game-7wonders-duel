package io.turnbase.games.sevenwondersduel.effect;

import io.turnbase.games.sevenwondersduel.id.PlayerId;

/** The owner takes another turn after their next one (B-TURN-6). */
public record GrantPlayAgain() implements Effect {

  @Override
  public void apply(Resolution resolution, PlayerId owner) {
    resolution.setPlayAgain(owner, true);
  }
}
