package io.turnbase.games.sevenwondersduel.effect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.turnbase.games.sevenwondersduel.agenda.Agenda;
import io.turnbase.games.sevenwondersduel.agenda.EffectStep;
import io.turnbase.games.sevenwondersduel.agenda.TurnEndStep;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.event.CoinsChanged;
import io.turnbase.games.sevenwondersduel.event.ComponentMoved;
import io.turnbase.games.sevenwondersduel.event.EffectRef;
import io.turnbase.games.sevenwondersduel.event.EventRecord;
import io.turnbase.games.sevenwondersduel.event.PawnMoved;
import io.turnbase.games.sevenwondersduel.event.PlayAgainChanged;
import io.turnbase.games.sevenwondersduel.event.TradePaid;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.id.ZoneId;
import io.turnbase.games.sevenwondersduel.rng.RngCursor;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.state.Pawn;
import io.turnbase.games.sevenwondersduel.state.Phase;
import io.turnbase.games.sevenwondersduel.state.PlayerState;
import io.turnbase.games.sevenwondersduel.state.Victory;
import io.turnbase.games.sevenwondersduel.zone.Location;
import io.turnbase.games.sevenwondersduel.zone.Placements;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ResolutionTest {

  static final PlayerId P1 = PlayerId.of(UUID.fromString("00000000-0000-0000-0000-000000000001"));
  static final PlayerId P2 = PlayerId.of(UUID.fromString("00000000-0000-0000-0000-000000000002"));
  static final RuleSet RULES = Fixtures.rules(Fixtures.Card.of("card"));
  static final ComponentId CARD = ComponentId.of("card");

  static DuelState state(ZoneId cardZone) {
    return new DuelState(
        Phase.AGE,
        P1,
        1,
        P1,
        null,
        Placements.place(List.of(), CARD, Location.of(cardZone), RULES.zones()),
        Map.of(P1, new PlayerState(7, false), P2, new PlayerState(7, false)),
        Map.of(P1, Set.of(), P2, Set.of()),
        RngCursor.start(1),
        RULES.packIds(),
        Agenda.EMPTY,
        Pawn.CENTRE,
        null);
  }

  private static Resolution resolution() {
    return new Resolution(state(ZoneId.of("deck")), RULES);
  }

  @Test
  void declareVictoryEndsTheGameAndDropsPendingSteps() {
    Resolution r = resolution();
    r.queue(List.of(new TurnEndStep(), new EffectStep(new EffectRef(CARD, 0), P1)));

    r.declareVictory(P2, Victory.Reason.MILITARY);

    assertThat(r.state().phase()).isEqualTo(Phase.OVER);
    assertThat(r.state().victory()).isEqualTo(new Victory(P2, Victory.Reason.MILITARY));
    assertThat(r.state().agenda().isEmpty()).isTrue();
  }

  @Test
  void nothingIsDrainedOnceTheGameIsOver() {
    RuleSet rules = Fixtures.rules(Fixtures.Card.of("card").withEffects(new GainCoins(3)));
    Resolution r = new Resolution(state(ZoneId.of("deck")), rules);
    r.queueEffects(CARD, P1);
    r.advance(s -> s.withPhase(Phase.OVER));

    r.drain();

    assertThat(r.state().player(P1).coins()).isEqualTo(7);
  }

  @Test
  void setPawnMovesItAndRecordsWhereFromAndTo() {
    Resolution r = resolution();
    Pawn there = new Pawn(P2, 3);

    r.setPawn(there);

    assertThat(r.state().pawn()).isEqualTo(there);
    assertThat(r.events())
        .extracting(EventRecord::event)
        .containsExactly(new PawnMoved(Pawn.CENTRE, there));
  }

  @Test
  void setPawnToWhereItIsRecordsNothing() {
    Resolution r = resolution();

    r.setPawn(Pawn.CENTRE);

    assertThat(r.events()).isEmpty();
  }

  @Test
  void moveUpdatesStateAndEmitsComponentMovedWithFromAndTo() {
    Resolution r = resolution();
    Location to = new Location(ZoneId.of("city"), P1.value().toString());

    r.move(CARD, to);

    assertThat(Placements.locationOf(r.state().placements(), CARD)).isEqualTo(to);
    assertThat(r.events()).hasSize(1);
    assertThat(r.events().get(0).event())
        .isEqualTo(new ComponentMoved(CARD, Location.of(ZoneId.of("deck")), to));
  }

  @Test
  void moveOutOfTerminalZoneThrowsAndEmitsNothing() {
    Resolution r = new Resolution(state(ZoneId.of("destroyed")), RULES);

    assertThatThrownBy(() -> r.move(CARD, Location.of(ZoneId.of("discard"))))
        .isInstanceOf(IllegalStateException.class);
    assertThat(r.events()).isEmpty();
  }

  @Test
  void changeCoinsUpdatesStateAndEmitsDelta() {
    Resolution r = resolution();

    r.changeCoins(P2, -3);

    assertThat(r.state().player(P2).coins()).isEqualTo(4);
    assertThat(r.events().get(0).event()).isEqualTo(new CoinsChanged(P2, -3));
  }

  @Test
  void payTradeChargesThePayerAndRecordsThePayment() {
    Resolution r = resolution();

    r.payTrade(P1, 3);

    assertThat(r.state().player(P1).coins()).isEqualTo(4);
    assertThat(r.events())
        .extracting(EventRecord::event)
        .containsExactly(new CoinsChanged(P1, -3), new TradePaid(P1, 3));
  }

  @Test
  void payingNoTradeRecordsNothing() {
    Resolution r = resolution();

    r.payTrade(P1, 0);

    assertThat(r.events()).isEmpty();
  }

  @Test
  void zeroDeltaEmitsNothing() {
    Resolution r = resolution();

    r.changeCoins(P1, 0);

    assertThat(r.events()).isEmpty();
  }

  @Test
  void coinsBelowZeroThrowsNamingPlayerAndLeavesStateUnchanged() {
    Resolution r = resolution();

    assertThatThrownBy(() -> r.changeCoins(P1, -8))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining(P1.value().toString());
    assertThat(r.state().player(P1).coins()).isEqualTo(7);
    assertThat(r.events()).isEmpty();
  }

  @Test
  void setPlayAgainEmitsOnlyOnChange() {
    Resolution r = resolution();

    r.setPlayAgain(P1, false);
    r.setPlayAgain(P1, true);
    r.setPlayAgain(P1, true);

    assertThat(r.state().player(P1).playAgain()).isTrue();
    assertThat(r.events())
        .extracting(EventRecord::event)
        .containsExactly(new PlayAgainChanged(P1, true));
  }

  @Test
  void eventsGetSequentialIdsStartingAtZeroAndRootParentIsMinusOne() {
    Resolution r = resolution();

    r.changeCoins(P1, 1);
    r.changeCoins(P1, 1);

    assertThat(r.events()).extracting(EventRecord::id).containsExactly(0, 1);
    assertThat(r.events()).extracting(EventRecord::parentId).containsExactly(-1, -1);
    assertThat(r.events())
        .extracting(EventRecord::source)
        .containsExactly(Optional.empty(), Optional.empty());
  }

  @Test
  void eventsInsideWithinCarryTheSource() {
    Resolution r = resolution();
    EffectRef ref = new EffectRef(CARD, 2);

    r.within(ref, () -> r.changeCoins(P1, 1));
    r.changeCoins(P1, 1);

    assertThat(r.events().get(0).source()).contains(ref);
    assertThat(r.events().get(1).source()).isEmpty();
  }

  @Test
  void anEffectCanAskWhichComponentItBelongsTo() {
    java.util.List<Optional<ComponentId>> seen = new java.util.ArrayList<>();
    Effect probe = (resolution, owner) -> seen.add(resolution.source());
    RuleSet rules = Fixtures.rules(Fixtures.Card.of("card").withEffects(probe));
    Resolution r = new Resolution(state(ZoneId.of("deck")), rules);

    assertThat(r.source()).isEmpty();
    r.resolveEffects(CARD, P1);

    assertThat(seen).containsExactly(Optional.of(CARD));
    assertThat(r.source()).isEmpty();
  }

  @Test
  void eventsListIsUnmodifiable() {
    Resolution r = resolution();
    r.changeCoins(P1, 1);

    assertThatThrownBy(() -> r.events().clear()).isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void aComponentMovedIntoAPublicZoneIsKnownToBothPlayers() {
    Resolution r = resolution();

    r.move(CARD, Location.of(ZoneId.of("structure.up")));

    assertThat(r.state().knowledge().get(P1)).containsExactly(CARD);
    assertThat(r.state().knowledge().get(P2)).containsExactly(CARD);
  }

  @Test
  void whatWasSeenStaysKnownInAHiddenZone() {
    Resolution r = resolution();
    r.move(CARD, Location.of(ZoneId.of("structure.up")));

    r.move(CARD, Location.of(ZoneId.of("underWonder")));

    assertThat(r.state().knowledge().get(P1)).containsExactly(CARD);
  }

  @Test
  void aComponentMovedIntoAHiddenZoneIsKnownToNobody() {
    Resolution r = resolution();

    r.move(CARD, Location.of(ZoneId.of("box")));

    assertThat(r.state().knowledge().get(P1)).isEmpty();
    assertThat(r.state().knowledge().get(P2)).isEmpty();
  }
}
