package io.turnbase.games.sevenwondersduel.setup;

import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P1;
import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.turnbase.games.sevenwondersduel.catalog.Component;
import io.turnbase.games.sevenwondersduel.catalog.ComponentType;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.military.MilitaryTrack;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.zone.Placements;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** B-SET-2: a military token lies on the side of the seat it is tagged for. */
class MilitarySetupTest {

  private static Component token(String id, int zone, Integer seat) {
    return seat == null
        ? SetupFixtures.piece(id, ComponentType.TOKEN, MilitaryTrack.zoneTag(zone))
        : SetupFixtures.piece(
            id, ComponentType.TOKEN, MilitaryTrack.zoneTag(zone), MilitaryTrack.seatTag(seat));
  }

  private static RuleSet rulesWith(Component... military) {
    List<Component> all = new ArrayList<>(SetupFixtures.pieces(23, 23, 20, 7, 12, 10));
    all.addAll(List.of(military));
    return SetupFixtures.rules(all);
  }

  @Test
  void eachTokenLiesOnTheSideOfItsSeatWhateverTheCatalogueOrder() {
    RuleSet rules = rulesWith(token("second", 2, 1), token("first", 2, 0));

    DuelState state = Setup.newGame(rules, 1, P1, P2);

    assertThat(Placements.idsIn(state.placements(), MilitaryTrack.tokenSpot(P1, 2)))
        .containsExactly(ComponentId.of("first"));
    assertThat(Placements.idsIn(state.placements(), MilitaryTrack.tokenSpot(P2, 2)))
        .containsExactly(ComponentId.of("second"));
  }

  @Test
  void aTokenWithoutASeatIsRejectedByName() {
    RuleSet rules = rulesWith(token("loose", 2, null));

    assertThatThrownBy(() -> Setup.newGame(rules, 1, P1, P2))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("loose")
        .hasMessageContaining("seat");
  }

  @Test
  void twoTokensForOneSeatAndZoneAreRejected() {
    RuleSet rules = rulesWith(token("one", 3, 0), token("two", 3, 0));

    assertThatThrownBy(() -> Setup.newGame(rules, 1, P1, P2))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("zone 3")
        .hasMessageContaining("seat 0");
  }

  @Test
  void aSeatBeyondThePlayersIsRejected() {
    RuleSet rules = rulesWith(token("far", 2, 2));

    assertThatThrownBy(() -> Setup.newGame(rules, 1, P1, P2))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("far");
  }
}
