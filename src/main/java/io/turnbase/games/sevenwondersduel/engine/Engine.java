package io.turnbase.games.sevenwondersduel.engine;

import io.turnbase.games.sevenwondersduel.agenda.DecisionStep;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.draft.Draft;
import io.turnbase.games.sevenwondersduel.effect.Resolution;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.state.Phase;

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
      case DraftWonder pick -> draft(state, actor, pick);
      case ChooseStarter choice -> chooseStarter(state, actor, choice);
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

  /** Only the player to move may pick, and only a Wonder on offer (B-WND-2, B-WND-3). */
  private Outcome draft(DuelState state, PlayerId actor, DraftWonder pick) {
    PlayerId mover =
        Draft.toMove(state)
            .orElseThrow(() -> new IllegalActionException("no Wonder draft is under way"));
    if (!mover.equals(actor)) {
      throw new IllegalActionException("not your pick");
    }
    if (pick.wonder() == null || !Draft.isOffered(state, pick.wonder())) {
      throw new IllegalActionException("not an offered Wonder");
    }
    Resolution resolution = new Resolution(state, rules);
    Draft.take(resolution, actor, pick.wonder());
    return new Outcome(resolution.state(), resolution.events());
  }

  /** Only the deciding player may choose, and only a seated player (B-AGE-0). */
  private Outcome chooseStarter(DuelState state, PlayerId actor, ChooseStarter choice) {
    if (state.phase() != Phase.AGE || state.deciding() == null) {
      throw new IllegalActionException("nobody has to choose a starting player");
    }
    if (!state.deciding().equals(actor)) {
      throw new IllegalActionException("not your choice");
    }
    if (choice.starter() == null || !state.players().containsKey(choice.starter())) {
      throw new IllegalActionException("starter is not seated");
    }
    Resolution resolution = new Resolution(state, rules);
    resolution.advance(s -> s.withActive(choice.starter()).withDeciding(null));
    return new Outcome(resolution.state(), resolution.events());
  }
}
