package io.turnbase.games.sevenwondersduel.playthrough;

import io.turnbase.games.sevenwondersduel.agenda.DecisionStep;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.cost.CostSolver;
import io.turnbase.games.sevenwondersduel.draft.Draft;
import io.turnbase.games.sevenwondersduel.engine.Answer;
import io.turnbase.games.sevenwondersduel.engine.Build;
import io.turnbase.games.sevenwondersduel.engine.BuildWonder;
import io.turnbase.games.sevenwondersduel.engine.ChooseStarter;
import io.turnbase.games.sevenwondersduel.engine.Discard;
import io.turnbase.games.sevenwondersduel.engine.DraftWonder;
import io.turnbase.games.sevenwondersduel.engine.PlayerAction;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.state.Phase;
import io.turnbase.games.sevenwondersduel.structure.Structure;
import io.turnbase.games.sevenwondersduel.turn.Construct;
import io.turnbase.games.sevenwondersduel.zone.Placements;
import io.turnbase.games.sevenwondersduel.zone.Zones;
import java.util.ArrayList;
import java.util.List;

/** Lists what the player who must act may do, using only the public rules APIs. */
final class Bot {

  /** The player who must act and everything they may legally do. */
  record Move(PlayerId actor, List<PlayerAction> actions) {}

  private Bot() {}

  static Move legalActions(RuleSet rules, DuelState state) {
    if (state.phase() == Phase.WONDER_DRAFT) {
      PlayerId mover = Draft.toMove(state).orElseThrow();
      return new Move(
          mover,
          Placements.idsIn(state.placements(), Zones.DRAFT).stream()
              .<PlayerAction>map(DraftWonder::new)
              .toList());
    }
    if (state.deciding() != null) {
      return new Move(
          state.deciding(),
          state.players().keySet().stream()
              .sorted(java.util.Comparator.comparing(p -> p.value().toString()))
              .<PlayerAction>map(ChooseStarter::new)
              .toList());
    }
    if (!state.agenda().isEmpty() && state.agenda().top() instanceof DecisionStep decision) {
      return new Move(
          decision.decider(), decision.options().stream().<PlayerAction>map(Answer::new).toList());
    }
    return new Move(state.active(), turnActions(rules, state));
  }

  private static List<PlayerAction> turnActions(RuleSet rules, DuelState state) {
    PlayerId actor = state.active();
    CostSolver costs = new CostSolver(rules);
    int coins = state.player(actor).coins();
    List<ComponentId> unbuilt = Placements.idsIn(state.placements(), Construct.unbuilt(actor));
    List<PlayerAction> actions = new ArrayList<>();
    for (ComponentId card : Structure.takeable(state.placements())) {
      actions.add(new Discard(card));
      if (costs.quote(state, actor, card).payableWith(coins)) {
        actions.add(new Build(card));
      }
      if (Construct.wonderLimitAllows(state)) {
        for (ComponentId wonder : unbuilt) {
          if (costs.quote(state, actor, wonder).payableWith(coins)) {
            actions.add(new BuildWonder(card, wonder));
          }
        }
      }
    }
    return actions;
  }
}
