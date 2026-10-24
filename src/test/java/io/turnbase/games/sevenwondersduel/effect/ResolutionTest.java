package io.turnbase.games.sevenwondersduel.effect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.event.CoinsChanged;
import io.turnbase.games.sevenwondersduel.event.ComponentMoved;
import io.turnbase.games.sevenwondersduel.event.EffectRef;
import io.turnbase.games.sevenwondersduel.event.EventRecord;
import io.turnbase.games.sevenwondersduel.event.PlayAgainChanged;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.id.ZoneId;
import io.turnbase.games.sevenwondersduel.rng.RngCursor;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.state.Phase;
import io.turnbase.games.sevenwondersduel.state.PlayerState;
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
        Placements.place(List.of(), CARD, Location.of(cardZone), RULES.zones()),
        Map.of(P1, new PlayerState(7, false), P2, new PlayerState(7, false)),
        Map.of(P1, Set.of(), P2, Set.of()),
        RngCursor.start(1),
        RULES.packIds());
  }

  private static Resolution resolution() {
    return new Resolution(state(ZoneId.of("deck")), RULES);
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
  void eventsListIsUnmodifiable() {
    Resolution r = resolution();
    r.changeCoins(P1, 1);

    assertThatThrownBy(() -> r.events().clear()).isInstanceOf(UnsupportedOperationException.class);
  }
}
