package io.turnbase.games.sevenwondersduel.draft;

import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P1;
import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.engine.DraftWonder;
import io.turnbase.games.sevenwondersduel.engine.Engine;
import io.turnbase.games.sevenwondersduel.engine.IllegalActionException;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.setup.Setup;
import io.turnbase.games.sevenwondersduel.setup.SetupFixtures;
import io.turnbase.games.sevenwondersduel.snapshot.Snapshots;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.state.Phase;
import io.turnbase.games.sevenwondersduel.zone.Location;
import io.turnbase.games.sevenwondersduel.zone.Placements;
import io.turnbase.games.sevenwondersduel.zone.Zones;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class DraftTest {

  private static final RuleSet RULES = SetupFixtures.baseSized();
  private static final Engine ENGINE = new Engine(RULES);

  private static DuelState fresh() {
    return Setup.newGame(RULES, 42, P1, P2);
  }

  private static List<ComponentId> offered(DuelState state) {
    return Placements.idsIn(state.placements(), Zones.DRAFT);
  }

  private static DuelState pickFirstOffered(DuelState state) {
    PlayerId mover = Draft.toMove(state).orElseThrow();
    return ENGINE.apply(state, mover, new DraftWonder(offered(state).get(0))).state();
  }

  private static List<ComponentId> unbuilt(DuelState state, PlayerId player) {
    return Placements.idsIn(
        state.placements(), new Location(Zones.WONDERS_UNBUILT, player.value().toString()));
  }

  @Test
  void pickOrderIsFirstSecondSecondFirstThenTheReverse() {
    DuelState state = fresh();
    PlayerId first = state.first();
    PlayerId second = state.opponentOf(first);
    List<PlayerId> movers = new ArrayList<>();
    for (int i = 0; i < 8; i++) {
      movers.add(Draft.toMove(state).orElseThrow());
      assertThat(state.active()).isEqualTo(movers.get(i));
      state = pickFirstOffered(state);
    }

    assertThat(movers).containsExactly(first, second, second, first, second, first, first, second);
  }

  @Test
  void secondRoundRevealsFourMoreWondersAfterTheFourthPick() {
    DuelState state = fresh();
    for (int i = 0; i < 3; i++) {
      state = pickFirstOffered(state);
    }
    assertThat(offered(state)).hasSize(1);

    state = pickFirstOffered(state);

    assertThat(offered(state)).hasSize(4);
    assertThat(Placements.idsIn(state.placements(), Setup.WONDER_POOL)).hasSize(4);
  }

  @Test
  void aFinishedDraftGivesFourWondersEachSendsFourToTheBoxAndStartsAgeOne() {
    DuelState state = fresh();
    for (int i = 0; i < 8; i++) {
      state = pickFirstOffered(state);
    }

    assertThat(unbuilt(state, P1)).hasSize(4);
    assertThat(unbuilt(state, P2)).hasSize(4);
    assertThat(
            Placements.idsIn(state.placements(), Zones.BOX).stream()
                .filter(id -> id.value().startsWith("wonder-")))
        .hasSize(4);
    assertThat(offered(state)).isEmpty();
    assertThat(Placements.idsIn(state.placements(), Setup.WONDER_POOL)).isEmpty();
    assertThat(state.phase()).isEqualTo(Phase.AGE);
    assertThat(state.age()).isEqualTo(1);
    assertThat(state.deciding()).isEqualTo(state.first());
    assertThat(Draft.toMove(state)).isEmpty();
  }

  @Test
  void onlyThePlayerToMoveMayPick() {
    DuelState state = fresh();
    PlayerId other = state.opponentOf(Draft.toMove(state).orElseThrow());

    assertThatThrownBy(() -> ENGINE.apply(state, other, new DraftWonder(offered(state).get(0))))
        .isInstanceOf(IllegalActionException.class)
        .hasMessageContaining("not your pick");
  }

  @Test
  void aWonderThatIsNotOfferedIsRejectedAndNothingChanges() {
    DuelState state = fresh();
    ComponentId inThePool = Placements.idsIn(state.placements(), Setup.WONDER_POOL).get(0);
    PlayerId mover = Draft.toMove(state).orElseThrow();

    assertThatThrownBy(() -> ENGINE.apply(state, mover, new DraftWonder(inThePool)))
        .isInstanceOf(IllegalActionException.class)
        .hasMessageContaining("not an offered Wonder");
    assertThatThrownBy(() -> ENGINE.apply(state, mover, new DraftWonder(null)))
        .isInstanceOf(IllegalActionException.class);
    assertThat(state).isEqualTo(fresh());
  }

  @Test
  void pickingOutsideTheDraftIsRejected() {
    DuelState state = fresh();
    for (int i = 0; i < 8; i++) {
      state = pickFirstOffered(state);
    }
    DuelState aged = state;

    assertThatThrownBy(
            () -> ENGINE.apply(aged, aged.first(), new DraftWonder(ComponentId.of("wonder-0"))))
        .isInstanceOf(IllegalActionException.class)
        .hasMessageContaining("no Wonder draft");
    assertThat(Draft.toMove(fresh().withPhase(Phase.SETUP))).isEmpty();
  }

  @Test
  void aSnapshotMidDraftRoundTrips() {
    DuelState state = pickFirstOffered(pickFirstOffered(fresh()));

    assertThat(Snapshots.fromSnapshot(Snapshots.toSnapshot(state))).isEqualTo(state);
  }
}
