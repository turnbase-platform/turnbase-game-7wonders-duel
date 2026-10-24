package io.turnbase.games.sevenwondersduel.turn;

import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P1;
import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.turnbase.games.sevenwondersduel.agenda.Agenda;
import io.turnbase.games.sevenwondersduel.agenda.EffectStep;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.engine.Discard;
import io.turnbase.games.sevenwondersduel.engine.Engine;
import io.turnbase.games.sevenwondersduel.engine.IllegalActionException;
import io.turnbase.games.sevenwondersduel.engine.Outcome;
import io.turnbase.games.sevenwondersduel.event.EffectRef;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.id.ZoneId;
import io.turnbase.games.sevenwondersduel.setup.SetupFixtures;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.state.Phase;
import io.turnbase.games.sevenwondersduel.structure.Structure;
import io.turnbase.games.sevenwondersduel.zone.Placements;
import java.util.List;
import org.junit.jupiter.api.Test;

class TurnTest {

  private static final RuleSet RULES = SetupFixtures.baseSized();
  private static final Engine ENGINE = new Engine(RULES);

  private static DuelState started() {
    return SetupFixtures.startedAgeOne(RULES, 42);
  }

  private static ComponentId takeable(DuelState state) {
    return Structure.takeable(state.placements()).get(0);
  }

  private static void assertRejected(
      DuelState state, PlayerId actor, ComponentId card, String why) {
    assertThatThrownBy(() -> ENGINE.apply(state, actor, new Discard(card)))
        .isInstanceOf(IllegalActionException.class)
        .hasMessageContaining(why);
  }

  @Test
  void discardMovesTheCardAndPaysTwoCoins() {
    DuelState state = started();
    PlayerId actor = state.active();
    ComponentId card = takeable(state);

    DuelState after = ENGINE.apply(state, actor, new Discard(card)).state();

    assertThat(Placements.idsIn(after.placements(), ZoneId.of("discard"))).containsExactly(card);
    assertThat(after.player(actor).coins()).isEqualTo(state.player(actor).coins() + 2);
  }

  @Test
  void theTurnPassesToTheOpponent() {
    DuelState state = started();

    DuelState after = ENGINE.apply(state, state.active(), new Discard(takeable(state))).state();

    assertThat(after.active()).isEqualTo(state.opponentOf(state.active()));
  }

  @Test
  void cardsUncoveredByTheTakesAreTurnedFaceUpForTheNextPlayer() {
    DuelState state = started();
    int downBefore = Placements.idsIn(state.placements(), Structure.DOWN).size();

    for (int take = 0; take < 6; take++) {
      state = ENGINE.apply(state, state.active(), new Discard(takeable(state))).state();

      assertThat(Structure.takeable(state.placements()))
          .hasSameSizeAs(Structure.uncovered(state.placements()));
    }
    assertThat(Placements.idsIn(state.placements(), Structure.DOWN).size()).isLessThan(downBefore);
  }

  @Test
  void theEventRecordShowsTheMoveAndThePayment() {
    DuelState state = started();

    Outcome outcome = ENGINE.apply(state, state.active(), new Discard(takeable(state)));

    assertThat(outcome.events()).hasSizeGreaterThanOrEqualTo(2);
  }

  @Test
  void onlyTheActivePlayerMayDiscard() {
    DuelState state = started();

    assertRejected(state, state.opponentOf(state.active()), takeable(state), "not your turn");
  }

  @Test
  void aCoveredCardCannotBeDiscarded() {
    DuelState state = started();
    ComponentId covered =
        Placements.idsIn(state.placements(), Structure.UP).stream()
            .filter(id -> !Structure.takeable(state.placements()).contains(id))
            .findFirst()
            .orElseGet(() -> Placements.idsIn(state.placements(), Structure.DOWN).get(0));

    assertRejected(state, state.active(), covered, "not available");
  }

  @Test
  void aCardOutsideTheStructureCannotBeDiscarded() {
    DuelState state = started();
    ComponentId wonder = Placements.idsIn(state.placements(), ZoneId.of("wonders.unbuilt")).get(0);

    assertRejected(state, state.active(), wonder, "not available");
  }

  @Test
  void nothingCanBeDiscardedBeforeTheStarterIsChosen() {
    DuelState state = started().withDeciding(P1);

    assertRejected(state, state.active(), takeable(state), "starting player");
  }

  @Test
  void nothingCanBeDiscardedOutsideAnAge() {
    DuelState state = started().withPhase(Phase.WONDER_DRAFT);

    assertRejected(state, state.active(), takeable(state), "no Age");
  }

  @Test
  void nothingCanBeDiscardedWhileADecisionOrEffectIsPending() {
    DuelState state = started();
    DuelState busy =
        state.withAgenda(
            new Agenda(
                List.of(new EffectStep(new EffectRef(ComponentId.of("a1-0"), 0), state.active()))));

    assertRejected(busy, busy.active(), takeable(busy), "pending");
  }

  @Test
  void aRejectedDiscardLeavesTheStateUntouched() {
    DuelState state = started();
    DuelState copy = state.withPhase(state.phase());

    assertThatThrownBy(
            () ->
                ENGINE.apply(
                    state, P2.equals(state.active()) ? P1 : P2, new Discard(takeable(state))))
        .isInstanceOf(IllegalActionException.class);
    assertThat(state).isEqualTo(copy);
  }
}
