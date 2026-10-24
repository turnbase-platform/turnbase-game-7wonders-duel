package io.turnbase.games.sevenwondersduel.engine;

import io.turnbase.games.sevenwondersduel.agenda.DecisionStep;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.effect.Resolution;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.state.DuelState;

/**
 * The single entry point for player actions: decides whether the actor may act, then resolves the
 * action to the next decision or to rest. The input state is never modified; a rejected action
 * throws before any resolution starts.
 */
public final class Engine {

  private final RuleSet rules;

  public Engine(RuleSet rules) {
    this.rules = rules;
  }

  public Outcome apply(DuelState state, PlayerId actor, PlayerAction action) {
    if (action == null) {
      throw new IllegalActionException("no action given");
    }
    return switch (action) {
      case Answer answer -> answer(state, actor, answer);
    };
  }

  /** Only the owner of the decision on top may answer, with one of the offered options. */
  private Outcome answer(DuelState state, PlayerId actor, Answer answer) {
    if (state.agenda().isEmpty() || !(state.agenda().top() instanceof DecisionStep decision)) {
      throw new IllegalActionException("no decision is pending");
    }
    if (!decision.decider().equals(actor)) {
      throw new IllegalActionException("not your decision");
    }
    if (answer.choice() == null || !decision.options().contains(answer.choice())) {
      throw new IllegalActionException("not an offered option");
    }
    Resolution resolution = new Resolution(state, rules);
    resolution.resume(answer.choice());
    resolution.drain();
    return new Outcome(resolution.state(), resolution.events());
  }
}
