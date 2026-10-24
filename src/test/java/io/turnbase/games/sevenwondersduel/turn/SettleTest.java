package io.turnbase.games.sevenwondersduel.turn;

import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P1;
import static org.assertj.core.api.Assertions.assertThat;

import io.turnbase.games.sevenwondersduel.agenda.Agenda;
import io.turnbase.games.sevenwondersduel.agenda.ComponentOption;
import io.turnbase.games.sevenwondersduel.agenda.DecisionStep;
import io.turnbase.games.sevenwondersduel.agenda.TurnEndStep;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.effect.Fixtures;
import io.turnbase.games.sevenwondersduel.effect.Resolution;
import io.turnbase.games.sevenwondersduel.engine.Answer;
import io.turnbase.games.sevenwondersduel.engine.Engine;
import io.turnbase.games.sevenwondersduel.event.EffectRef;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.id.ZoneId;
import io.turnbase.games.sevenwondersduel.setup.SetupFixtures;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.turn.TurnFixtures.Part;
import io.turnbase.games.sevenwondersduel.zone.Placements;
import java.util.List;
import org.junit.jupiter.api.Test;

class SettleTest {

  private static final Part CHOOSER = Part.card("chooser").withEffects(new Fixtures.Choose());

  private final RuleSet rules = TurnFixtures.rulesWith(CHOOSER);
  private final DuelState started = SetupFixtures.startedAgeOne(rules, 1);

  private DuelState withAgenda(
      DuelState state, io.turnbase.games.sevenwondersduel.agenda.Step... steps) {
    return state.withAgenda(new Agenda(List.of(steps)));
  }

  private DecisionStep decisionFor(DuelState state, PlayerId decider) {
    List<ComponentId> board = Placements.idsIn(state.placements(), ZoneId.of("board"));
    return new DecisionStep(
        decider,
        decider,
        new EffectRef(CHOOSER.id(), 0),
        board.stream()
            .<io.turnbase.games.sevenwondersduel.agenda.Option>map(ComponentOption::new)
            .toList());
  }

  @Test
  void aTurnEndStepAloneEndsTheTurn() {
    DuelState state = withAgenda(started, new TurnEndStep());
    Resolution r = new Resolution(state, rules);

    Turn.settle(r);

    assertThat(r.state().agenda().isEmpty()).isTrue();
    assertThat(r.state().active()).isEqualTo(state.opponentOf(state.active()));
  }

  @Test
  void aDecisionAboveTheTurnEndStopsTheSettlement() {
    DecisionStep decision = decisionFor(started, started.active());
    DuelState state = withAgenda(started, decision, new TurnEndStep());
    Resolution r = new Resolution(state, rules);

    Turn.settle(r);

    assertThat(r.state().agenda().steps()).containsExactly(decision, new TurnEndStep());
    assertThat(r.state().active()).isEqualTo(started.active());
  }

  @Test
  void anAnsweredDecisionEndsTheTurnOnce() {
    DecisionStep decision = decisionFor(started, started.active());
    DuelState state = withAgenda(started, decision, new TurnEndStep());

    DuelState after =
        new Engine(rules)
            .apply(state, started.active(), new Answer(decision.options().get(0)))
            .state();

    assertThat(after.agenda().isEmpty()).isTrue();
    assertThat(after.active()).isEqualTo(started.opponentOf(started.active()));
  }

  @Test
  void aDecisionAnsweredOutsideATurnDoesNotMoveTheGame() {
    DecisionStep decision = decisionFor(started, started.active());
    DuelState state = withAgenda(started, decision);

    DuelState after =
        new Engine(rules)
            .apply(state, started.active(), new Answer(decision.options().get(0)))
            .state();

    assertThat(after.active()).isEqualTo(started.active());
  }

  @Test
  void settleWithAnEmptyAgendaChangesNothing() {
    Resolution r = new Resolution(started, rules);

    Turn.settle(r);

    assertThat(r.state()).isEqualTo(started);
    assertThat(P1).isNotNull();
  }
}
