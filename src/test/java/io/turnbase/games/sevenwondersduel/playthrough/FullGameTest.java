package io.turnbase.games.sevenwondersduel.playthrough;

import static org.assertj.core.api.Assertions.assertThat;

import io.turnbase.games.sevenwondersduel.catalog.BasePack;
import io.turnbase.games.sevenwondersduel.catalog.ComponentType;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.catalog.Tag;
import io.turnbase.games.sevenwondersduel.content.BaseContent;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.science.Science;
import io.turnbase.games.sevenwondersduel.scoring.Scorecard;
import io.turnbase.games.sevenwondersduel.scoring.Scoring;
import io.turnbase.games.sevenwondersduel.snapshot.Snapshots;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.state.Pawn;
import io.turnbase.games.sevenwondersduel.state.Phase;
import io.turnbase.games.sevenwondersduel.state.Victory;
import io.turnbase.games.sevenwondersduel.zone.Placement;
import io.turnbase.games.sevenwondersduel.zone.Zones;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class FullGameTest {

  private static final RuleSet RULES = RuleSet.assemble(List.of(new BasePack(), new BaseContent()));
  private static final int SEEDS = 120;
  private static final int CAP = 400;

  @Test
  void everyGameEndsInAVictoryAndKeepsItsInvariants() {
    Set<Victory.Reason> reasons = EnumSet.noneOf(Victory.Reason.class);
    Set<ComponentId> reached = new HashSet<>();
    for (Policy policy : Policy.values()) {
      for (long seed = 1; seed <= SEEDS; seed++) {
        Playthrough.Result game = Playthrough.play(RULES, seed, policy, CAP);
        String label = "seed " + seed + " under " + policy;
        DuelState previous = null;
        for (DuelState state : game.states()) {
          try {
            Invariants.check(RULES, previous, state);
          } catch (AssertionError e) {
            throw new AssertionError(label + ": " + e.getMessage(), e);
          }
          assertThat(Snapshots.fromSnapshot(Snapshots.toSnapshot(state)))
              .as(label + " snapshot round trip")
              .isEqualTo(state);
          reached.addAll(acquired(state));
          previous = state;
        }
        DuelState end = game.finalState();
        assertThat(end.phase()).as(label).isEqualTo(Phase.OVER);
        assertThat(end.victory()).as(label).isNotNull();
        verdictAgreesWithTheBoard(end, label);
        reasons.add(end.victory().reason());
      }
    }
    assertThat(reasons)
        .contains(Victory.Reason.MILITARY, Victory.Reason.SCIENCE, Victory.Reason.POINTS);
    assertThat(reached).containsAll(catalogue(ComponentType.WONDER));
    assertThat(reached)
        .containsAll(RULES.catalog().tagged(Tag.PROGRESS).stream().map(c -> c.id()).toList());
  }

  @Test
  void aSeedAndPolicyFixTheGame() {
    for (Policy policy : Policy.values()) {
      assertThat(Playthrough.play(RULES, 5, policy, CAP).states())
          .isEqualTo(Playthrough.play(RULES, 5, policy, CAP).states());
    }
  }

  /** Wonders in {@code wonders.built} and progress tokens in a city. */
  private static Set<ComponentId> acquired(DuelState state) {
    Set<ComponentId> ids = new HashSet<>();
    for (Placement p : state.placements()) {
      ComponentId id = p.component();
      boolean token = RULES.catalog().get(id).tags().contains(Tag.PROGRESS);
      if (p.at().zone().equals(Zones.WONDERS_BUILT)
          || (token && p.at().zone().equals(Zones.CITY))) {
        ids.add(id);
      }
    }
    return ids;
  }

  private static List<ComponentId> catalogue(ComponentType type) {
    return RULES.catalog().ofType(type).stream().map(c -> c.id()).toList();
  }

  private static void verdictAgreesWithTheBoard(DuelState end, String label) {
    Victory v = end.victory();
    switch (v.reason()) {
      case MILITARY -> {
        assertThat(end.pawn().space()).as(label).isEqualTo(Pawn.CAPITAL);
        assertThat(end.pawn().side()).as(label).isEqualTo(end.opponentOf(v.winner()));
      }
      case SCIENCE ->
          assertThat(Science.distinct(end, RULES, v.winner()))
              .as(label)
              .isGreaterThanOrEqualTo(Science.SUPREMACY);
      case POINTS -> {
        PlayerId[] seats = end.players().keySet().toArray(new PlayerId[0]);
        Scorecard a = Scoring.score(end, RULES, seats[0]);
        Scorecard b = Scoring.score(end, RULES, seats[1]);
        PlayerId expected = null;
        if (a.total() != b.total()) {
          expected = a.total() > b.total() ? seats[0] : seats[1];
        } else if (a.line(Scoring.BLUE) != b.line(Scoring.BLUE)) {
          expected = a.line(Scoring.BLUE) > b.line(Scoring.BLUE) ? seats[0] : seats[1];
        }
        assertThat(v.winner()).as(label).isEqualTo(expected);
      }
      default -> throw new AssertionError(label + ": unexpected reason " + v.reason());
    }
  }

  /** Pinned so that a game differing between JVM runs (hash order) cannot pass unnoticed. */
  @Test
  void aKnownGameEndsAsItAlwaysHas() {
    Playthrough.Result game = Playthrough.play(RULES, 5, Policy.RANDOM, CAP);

    assertThat(game.steps()).isEqualTo(75);
  }
}
