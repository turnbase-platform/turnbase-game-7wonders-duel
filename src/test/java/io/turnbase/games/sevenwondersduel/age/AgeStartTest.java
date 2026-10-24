package io.turnbase.games.sevenwondersduel.age;

import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P1;
import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.draft.Draft;
import io.turnbase.games.sevenwondersduel.effect.Resolution;
import io.turnbase.games.sevenwondersduel.engine.ChooseStarter;
import io.turnbase.games.sevenwondersduel.engine.DraftWonder;
import io.turnbase.games.sevenwondersduel.engine.Engine;
import io.turnbase.games.sevenwondersduel.engine.IllegalActionException;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.id.ZoneId;
import io.turnbase.games.sevenwondersduel.setup.Setup;
import io.turnbase.games.sevenwondersduel.setup.SetupFixtures;
import io.turnbase.games.sevenwondersduel.snapshot.Snapshots;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.structure.Structure;
import io.turnbase.games.sevenwondersduel.zone.Placements;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AgeStartTest {

  private static final RuleSet RULES = SetupFixtures.baseSized();
  private static final Engine ENGINE = new Engine(RULES);

  private static DuelState drafted() {
    DuelState state = Setup.newGame(RULES, 42, P1, P2);
    while (Draft.toMove(state).isPresent()) {
      PlayerId mover = Draft.toMove(state).get();
      ComponentId wonder = Placements.idsIn(state.placements(), ZoneId.of("draft")).get(0);
      state = ENGINE.apply(state, mover, new DraftWonder(wonder)).state();
    }
    return state;
  }

  @Test
  void ageOneIsDealtIntoItsLayoutAndTheUncoveredCardsAreFaceUp() {
    DuelState state = drafted();

    assertThat(Placements.idsIn(state.placements(), Setup.ageDeck(1))).isEmpty();
    int up = Placements.idsIn(state.placements(), Structure.UP).size();
    int down = Placements.idsIn(state.placements(), Structure.DOWN).size();
    assertThat(up + down).isEqualTo(20);
    assertThat(Structure.takeable(state.placements())).hasSize(6);
    assertThat(down).isEqualTo(5 + 3);
  }

  @Test
  void theDecidingPlayerChoosesWhoStarts() {
    DuelState state = drafted();
    PlayerId other = state.opponentOf(state.first());

    DuelState started = ENGINE.apply(state, state.first(), new ChooseStarter(other)).state();

    assertThat(started.active()).isEqualTo(other);
    assertThat(started.deciding()).isNull();
  }

  @Test
  void anyoneElseOrNobodyOrAStrangerIsRejectedAndNothingChanges() {
    DuelState state = drafted();
    PlayerId other = state.opponentOf(state.first());

    assertThatThrownBy(() -> ENGINE.apply(state, other, new ChooseStarter(other)))
        .isInstanceOf(IllegalActionException.class)
        .hasMessageContaining("not your choice");
    assertThatThrownBy(
            () ->
                ENGINE.apply(
                    state, state.first(), new ChooseStarter(PlayerId.of(UUID.randomUUID()))))
        .isInstanceOf(IllegalActionException.class)
        .hasMessageContaining("not seated");
    assertThatThrownBy(() -> ENGINE.apply(state, state.first(), new ChooseStarter(null)))
        .isInstanceOf(IllegalActionException.class);

    DuelState started = ENGINE.apply(state, state.first(), new ChooseStarter(other)).state();
    assertThatThrownBy(() -> ENGINE.apply(started, other, new ChooseStarter(other)))
        .isInstanceOf(IllegalActionException.class)
        .hasMessageContaining("nobody has to choose");
  }

  @Test
  void aSnapshotWhileTheStarterIsPendingRoundTrips() {
    DuelState state = drafted();

    DuelState restored = Snapshots.fromSnapshot(Snapshots.toSnapshot(state));

    assertThat(restored).isEqualTo(state);
    assertThat(restored.deciding()).isEqualTo(state.first());
  }

  @Test
  void beginRejectsAnAgeWhoseDeckWasAlreadyDealt() {
    DuelState state = drafted();

    assertThatThrownBy(() -> AgeStart.begin(new Resolution(state, RULES), 1, P1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("needs 20 cards");
  }
}
