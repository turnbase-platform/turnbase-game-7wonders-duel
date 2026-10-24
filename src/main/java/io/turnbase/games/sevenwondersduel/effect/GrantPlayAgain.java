package io.turnbase.games.sevenwondersduel.effect;

import io.turnbase.games.sevenwondersduel.id.PlayerId;
import java.util.List;

/** The owner takes another turn after their next one (B-TURN-6). */
public record GrantPlayAgain() implements Effect {

  @Override
  public void apply(Resolution resolution, PlayerId owner) {
    resolution.setPlayAgain(owner, true);
  }

  @Override
  public List<Badge> badges() {
    return List.of(Badge.of("playAgain"));
  }
}
