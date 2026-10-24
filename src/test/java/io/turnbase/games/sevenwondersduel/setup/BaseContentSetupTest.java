package io.turnbase.games.sevenwondersduel.setup;

import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P1;
import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P2;
import static org.assertj.core.api.Assertions.assertThat;

import io.turnbase.games.sevenwondersduel.catalog.BasePack;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.catalog.Tag;
import io.turnbase.games.sevenwondersduel.content.BaseContent;
import io.turnbase.games.sevenwondersduel.effect.Resolution;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.military.Military;
import io.turnbase.games.sevenwondersduel.military.MilitaryTrack;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.zone.Placements;
import io.turnbase.games.sevenwondersduel.zone.Zones;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Setup over the real base catalogue rather than a fixture. */
class BaseContentSetupTest {

  private static final RuleSet RULES = RuleSet.assemble(List.of(new BasePack(), new BaseContent()));

  private static DuelState game(long seed) {
    return Setup.newGame(RULES, seed, P1, P2);
  }

  private static List<ComponentId> at(
      DuelState state, io.turnbase.games.sevenwondersduel.zone.Location where) {
    return Placements.idsIn(state.placements(), where);
  }

  @Test
  void fiveProgressTokensLieOnTheBoardAndFiveInTheBox() {
    DuelState state = game(1);
    List<ComponentId> board = Placements.idsIn(state.placements(), Zones.BOARD);
    List<ComponentId> box = Placements.idsIn(state.placements(), Zones.BOX);

    assertThat(board).hasSize(5);
    assertThat(box.stream().filter(id -> RULES.catalog().get(id).tags().contains(Tag.PROGRESS)))
        .hasSize(5);
    assertThat(board)
        .allSatisfy(id -> assertThat(RULES.catalog().get(id).tags()).contains(Tag.PROGRESS));
  }

  @Test
  void fourWondersAreOfferedAndEightWaitInThePool() {
    DuelState state = game(1);

    assertThat(Placements.idsIn(state.placements(), Zones.DRAFT)).hasSize(4);
    assertThat(at(state, Setup.WONDER_POOL)).hasSize(8);
  }

  @Test
  void eachSideHoldsALootTwoInZoneTwoAndALootFiveInZoneThree() {
    DuelState state = game(1);

    for (var side : List.of(P1, P2)) {
      assertThat(at(state, MilitaryTrack.tokenSpot(side, 1))).isEmpty();
      assertThat(at(state, MilitaryTrack.tokenSpot(side, 2)))
          .singleElement()
          .satisfies(id -> assertThat(RULES.catalog().get(id).name()).startsWith("Loot 2"));
      assertThat(at(state, MilitaryTrack.tokenSpot(side, 3)))
          .singleElement()
          .satisfies(id -> assertThat(RULES.catalog().get(id).name()).startsWith("Loot 5"));
    }
  }

  @Test
  void aPawnEnteringZoneTwoCostsTheDefenderTwoCoins() {
    DuelState state = game(1);
    Resolution r = new Resolution(state, RULES);

    Military.advance(r, P1, 3);
    r.drain();

    assertThat(r.state().player(P2).coins()).isEqualTo(state.player(P2).coins() - 2);
    assertThat(r.state().player(P1).coins()).isEqualTo(state.player(P1).coins());
    assertThat(at(r.state(), MilitaryTrack.tokenSpot(P2, 2))).isEmpty();
  }

  @Test
  void setupIsStablePerSeed() {
    assertThat(game(5)).isEqualTo(game(5));
  }
}
