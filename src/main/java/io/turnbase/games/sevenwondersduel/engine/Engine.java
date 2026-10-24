package io.turnbase.games.sevenwondersduel.engine;

import io.turnbase.games.sevenwondersduel.agenda.DecisionStep;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.cost.CostSolver;
import io.turnbase.games.sevenwondersduel.cost.Quote;
import io.turnbase.games.sevenwondersduel.draft.Draft;
import io.turnbase.games.sevenwondersduel.effect.Resolution;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.state.Phase;
import io.turnbase.games.sevenwondersduel.structure.Structure;
import io.turnbase.games.sevenwondersduel.turn.Construct;
import io.turnbase.games.sevenwondersduel.turn.Turn;
import io.turnbase.games.sevenwondersduel.zone.Placements;

/**
 * The single entry point for player actions: decides whether the actor may act, then resolves the
 * action to the next decision or to rest. The input state is never modified; a rejected action
 * throws before any resolution starts.
 */
public final class Engine {

  private final RuleSet rules;
  private final CostSolver costs;

  public Engine(RuleSet rules) {
    this.rules = rules;
    this.costs = new CostSolver(rules);
  }

  public Outcome apply(DuelState state, PlayerId actor, PlayerAction action) {
    if (action == null) {
      throw new IllegalActionException("no action given");
    }
    return switch (action) {
      case Answer answer -> answer(state, actor, answer);
      case DraftWonder pick -> draft(state, actor, pick);
      case ChooseStarter choice -> chooseStarter(state, actor, choice);
      case Discard discard -> discard(state, actor, discard);
      case Build build -> build(state, actor, build);
      case BuildWonder build -> buildWonder(state, actor, build);
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
    Turn.settle(resolution);
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

  /**
   * Only the active player may take a card, and only one that is available, in an Age with nothing
   * left to settle (B-TURN-2, B-STR-1).
   */
  private Outcome discard(DuelState state, PlayerId actor, Discard discard) {
    requireTakeable(state, actor, discard.card());
    Resolution resolution = new Resolution(state, rules);
    Turn.discard(resolution, actor, discard.card());
    return new Outcome(resolution.state(), resolution.events());
  }

  /** Only the active player may build, with a card that is available and that they can pay for. */
  private Outcome build(DuelState state, PlayerId actor, Build build) {
    requireTakeable(state, actor, build.card());
    Quote quote = costs.quote(state, actor, build.card());
    if (!quote.payableWith(state.player(actor).coins())) {
      throw new IllegalActionException("cannot afford " + build.card().value());
    }
    Resolution resolution = new Resolution(state, rules);
    Construct.building(resolution, actor, build.card(), quote);
    return new Outcome(resolution.state(), resolution.events());
  }

  /**
   * Only the active player may build, with an available card, one of their own unbuilt Wonders, and
   * while fewer than seven Wonders exist (B-ACT-3, B-ACT-5).
   */
  private Outcome buildWonder(DuelState state, PlayerId actor, BuildWonder build) {
    requireTakeable(state, actor, build.card());
    if (build.wonder() == null
        || !Placements.idsIn(state.placements(), Construct.unbuilt(actor))
            .contains(build.wonder())) {
      throw new IllegalActionException("not one of your unbuilt Wonders");
    }
    if (!Construct.wonderLimitAllows(state)) {
      throw new IllegalActionException("seven Wonders are already built");
    }
    Quote quote = costs.quote(state, actor, build.wonder());
    if (!quote.payableWith(state.player(actor).coins())) {
      throw new IllegalActionException("cannot afford " + build.wonder().value());
    }
    Resolution resolution = new Resolution(state, rules);
    Construct.wonder(resolution, actor, build.card(), build.wonder(), quote);
    return new Outcome(resolution.state(), resolution.events());
  }

  private static void requireTakeable(DuelState state, PlayerId actor, ComponentId card) {
    requireTurnToTake(state, actor);
    if (card == null || !Structure.takeable(state.placements()).contains(card)) {
      throw new IllegalActionException("card is not available");
    }
  }

  private static void requireTurnToTake(DuelState state, PlayerId actor) {
    if (state.phase() != Phase.AGE) {
      throw new IllegalActionException("no Age is under way");
    }
    if (state.deciding() != null) {
      throw new IllegalActionException("the starting player must be chosen first");
    }
    if (!state.agenda().isEmpty()) {
      throw new IllegalActionException("a decision or effect is pending");
    }
    if (!state.active().equals(actor)) {
      throw new IllegalActionException("not your turn");
    }
  }
}
