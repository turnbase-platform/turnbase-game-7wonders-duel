package io.turnbase.games.sevenwondersduel.agenda;

import io.turnbase.games.sevenwondersduel.event.EffectRef;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import java.util.HashSet;
import java.util.List;

/**
 * A choice some player must make before the game goes on (B-EFF-3). The decider may be the opponent
 * of whoever is playing. The answer resumes {@code effect}.
 *
 * @param decider the player who must answer
 * @param owner the player who owns the effect's source; the effect is resumed on their behalf
 * @param effect the effect that asked and will receive the answer
 * @param options what the owner may pick; never empty, never repeated
 */
public record DecisionStep(PlayerId decider, PlayerId owner, EffectRef effect, List<Option> options)
    implements Step {

  public DecisionStep {
    options = List.copyOf(options);
    if (options.isEmpty()) {
      throw new IllegalArgumentException("a decision needs at least one option");
    }
    if (new HashSet<>(options).size() != options.size()) {
      throw new IllegalArgumentException("a decision must not repeat an option");
    }
  }
}
