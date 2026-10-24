package io.turnbase.games.sevenwondersduel.playthrough;

import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.effect.Fixtures;
import io.turnbase.games.sevenwondersduel.engine.Engine;
import io.turnbase.games.sevenwondersduel.setup.Setup;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.state.Phase;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Plays a seeded game to its end with a bot, keeping every state along the way. */
final class Playthrough {

  /** {@code states.get(i)} is the game after {@code i} actions; the last one is final. */
  record Result(List<DuelState> states) {
    DuelState finalState() {
      return states.get(states.size() - 1);
    }

    int steps() {
      return states.size() - 1;
    }
  }

  private Playthrough() {}

  /**
   * Plays seat one against seat two, both under {@code policy}. The game seed and the bot's choices
   * both derive from {@code seed}. Fails if the game is not over within {@code cap} actions.
   */
  static Result play(RuleSet rules, long seed, Policy policy, int cap) {
    Engine engine = new Engine(rules);
    Random random = new Random(seed);
    DuelState state = Setup.newGame(rules, seed, Fixtures.P1, Fixtures.P2);
    List<DuelState> states = new ArrayList<>(List.of(state));
    while (state.phase() != Phase.OVER) {
      if (states.size() > cap) {
        throw new AssertionError(
            "seed " + seed + " under " + policy + " is not over after " + cap + " actions");
      }
      Bot.Move move = Bot.legalActions(rules, state);
      if (move.actions().isEmpty()) {
        throw new AssertionError(
            "seed " + seed + " under " + policy + " has no legal action at step " + states.size());
      }
      state = engine.apply(state, move.actor(), policy.pick(rules, move.actions(), random)).state();
      states.add(state);
    }
    return new Result(states);
  }
}
