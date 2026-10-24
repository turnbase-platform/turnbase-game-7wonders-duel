package io.turnbase.games.sevenwondersduel.state;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.turnbase.games.sevenwondersduel.agenda.Agenda;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PackId;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.id.ZoneId;
import io.turnbase.games.sevenwondersduel.rng.RngCursor;
import io.turnbase.games.sevenwondersduel.zone.Location;
import io.turnbase.games.sevenwondersduel.zone.Placement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DuelStateTest {

  private static final PlayerId P1 =
      PlayerId.of(UUID.fromString("00000000-0000-0000-0000-000000000001"));
  private static final PlayerId P2 =
      PlayerId.of(UUID.fromString("00000000-0000-0000-0000-000000000002"));

  private static DuelState state(List<Placement> placements) {
    return new DuelState(
        Phase.SETUP,
        P1,
        1,
        P1,
        null,
        placements,
        Map.of(P1, new PlayerState(7, false), P2, new PlayerState(7, false)),
        Map.of(P1, Set.of(), P2, Set.of()),
        RngCursor.start(1),
        List.of(PackId.of("base")),
        Agenda.EMPTY,
        Pawn.CENTRE);
  }

  private static DuelState state() {
    return state(List.of());
  }

  @Test
  void opponentOfReturnsTheOtherSeat() {
    assertThat(state().opponentOf(P1)).isEqualTo(P2);
    assertThat(state().opponentOf(P2)).isEqualTo(P1);
  }

  @Test
  void opponentOfUnknownPlayerThrows() {
    PlayerId stranger = PlayerId.of(UUID.randomUUID());

    assertThatThrownBy(() -> state().opponentOf(stranger))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining(stranger.value().toString());
    assertThatThrownBy(() -> state().player(stranger)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void playAgainIsPerPlayer() {
    DuelState next = state().withPlayer(P2, state().player(P2).withPlayAgain(true));

    assertThat(next.player(P2).playAgain()).isTrue();
    assertThat(next.player(P1).playAgain()).isFalse();
  }

  @Test
  void withersDoNotMutateTheOriginal() {
    DuelState original = state();

    DuelState changed =
        original
            .withPhase(Phase.AGE)
            .withActive(P2)
            .withPlayer(P1, new PlayerState(3, false))
            .withKnowledge(P1, Set.of(ComponentId.of("c1")))
            .withRng(new RngCursor(1, 4))
            .withPlacements(
                List.of(new Placement(ComponentId.of("c1"), Location.of(ZoneId.of("deck")))));

    assertThat(changed.phase()).isEqualTo(Phase.AGE);
    assertThat(changed.active()).isEqualTo(P2);
    assertThat(changed.player(P1).coins()).isEqualTo(3);
    assertThat(changed.knowledge().get(P1)).containsExactly(ComponentId.of("c1"));
    assertThat(changed.rng().draws()).isEqualTo(4);
    assertThat(original).isEqualTo(state());
  }

  @Test
  void constructorRejectsActiveOutsideTheTwoSeats() {
    PlayerId stranger = PlayerId.of(UUID.randomUUID());

    assertThatThrownBy(() -> state().withActive(stranger))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () ->
                new DuelState(
                    Phase.SETUP,
                    P1,
                    1,
                    P1,
                    null,
                    List.of(),
                    Map.of(P1, new PlayerState(7, false)),
                    Map.of(),
                    RngCursor.start(1),
                    List.of(),
                    Agenda.EMPTY,
                    Pawn.CENTRE))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void negativeCoinsRejected() {
    assertThatThrownBy(() -> new PlayerState(-1, false))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new PlayerState(1, false).withCoins(-2))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void collectionsAreDefensivelyCopied() {
    List<Placement> input = new ArrayList<>();
    DuelState state = state(input);

    input.add(new Placement(ComponentId.of("c1"), Location.of(ZoneId.of("deck"))));

    assertThat(state.placements()).isEmpty();
    assertThatThrownBy(() -> state.placements().add(input.get(0)))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void duplicatePlacementOfOneComponentIsRejected() {
    Placement first = new Placement(ComponentId.of("c1"), Location.of(ZoneId.of("deck")));
    Placement second = new Placement(ComponentId.of("c1"), Location.of(ZoneId.of("discard")));

    assertThatThrownBy(() -> state(List.of(first, second)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("c1");
    assertThatThrownBy(() -> state().withPlacements(List.of(first, second)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void knowledgeForAStrangerIsRejected() {
    PlayerId stranger = PlayerId.of(UUID.randomUUID());

    assertThatThrownBy(
            () ->
                new DuelState(
                    Phase.SETUP,
                    P1,
                    1,
                    P1,
                    null,
                    List.of(),
                    Map.of(P1, new PlayerState(7, false), P2, new PlayerState(7, false)),
                    Map.of(stranger, Set.of()),
                    RngCursor.start(1),
                    List.of(),
                    Agenda.EMPTY,
                    Pawn.CENTRE))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining(stranger.value().toString());
  }

  @Test
  void firstPlayerMustBeSeated() {
    assertThatThrownBy(() -> state(List.of()).withFirst(PlayerId.of(UUID.randomUUID())))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("first player");
  }

  @Test
  void decidingPlayerMustBeSeatedOrAbsent() {
    assertThat(state(List.of()).withDeciding(P2).deciding()).isEqualTo(P2);
    assertThat(state(List.of()).withDeciding(P2).withDeciding(null).deciding()).isNull();
    assertThatThrownBy(() -> state(List.of()).withDeciding(PlayerId.of(UUID.randomUUID())))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("deciding player");
  }

  @Test
  void ageCannotBeNegative() {
    assertThat(state(List.of()).withAge(3).age()).isEqualTo(3);
    assertThatThrownBy(() -> state(List.of()).withAge(-1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("age");
  }

  @Test
  void pawnSideMustBeSeated() {
    PlayerId stranger =
        PlayerId.of(java.util.UUID.fromString("00000000-0000-0000-0000-000000000009"));

    assertThatThrownBy(() -> state().withPawn(new Pawn(stranger, 2)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("pawn side");
  }

  @Test
  void withPawnReplacesOnlyThePawn() {
    DuelState moved = state().withPawn(new Pawn(P1, 2));

    assertThat(moved.pawn()).isEqualTo(new Pawn(P1, 2));
    assertThat(moved.withPawn(Pawn.CENTRE)).isEqualTo(state());
  }
}
