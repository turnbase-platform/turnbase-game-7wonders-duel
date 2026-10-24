package io.turnbase.games.sevenwondersduel.scoring;

import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.catalog.Tag;
import io.turnbase.games.sevenwondersduel.effect.Ownership;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.state.DuelState;

/**
 * What a {@link Points} modifier may look at: the state at the end of the game and whose component
 * it is.
 */
public record ScoreContext(DuelState state, RuleSet rules, PlayerId owner) {

  /** How many live components carrying {@code tag} the player has. */
  public int count(PlayerId player, Tag tag) {
    return Ownership.count(state, rules, player, tag);
  }
}
