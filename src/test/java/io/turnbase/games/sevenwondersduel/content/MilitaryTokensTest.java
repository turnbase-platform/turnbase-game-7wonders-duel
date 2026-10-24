package io.turnbase.games.sevenwondersduel.content;

import static org.assertj.core.api.Assertions.assertThat;

import io.turnbase.games.sevenwondersduel.catalog.ComponentType;
import io.turnbase.games.sevenwondersduel.effect.LoseCoins;
import io.turnbase.games.sevenwondersduel.military.MilitaryTrack;
import java.util.List;
import org.junit.jupiter.api.Test;

class MilitaryTokensTest {

  @Test
  void twoLoot2InZoneTwoAndTwoLoot5InZoneThree() {
    List<Token> tokens = MilitaryTokens.all();

    assertThat(tokens).hasSize(4);
    assertThat(tokens.stream().map(Token::id).distinct()).hasSize(4);
    assertThat(tokens).allSatisfy(t -> assertThat(t.type()).isEqualTo(ComponentType.TOKEN));
    assertThat(tokens.stream().filter(t -> t.tags().contains(MilitaryTrack.zoneTag(2))))
        .hasSize(2)
        .allSatisfy(t -> assertThat(t.effects()).isEqualTo(List.of(new LoseCoins(2))));
    assertThat(tokens.stream().filter(t -> t.tags().contains(MilitaryTrack.zoneTag(3))))
        .hasSize(2)
        .allSatisfy(t -> assertThat(t.effects()).isEqualTo(List.of(new LoseCoins(5))));
  }

  @Test
  void eachZoneHoldsOneTokenPerSeat() {
    for (int zone : List.of(2, 3)) {
      for (int seat : List.of(0, 1)) {
        assertThat(
                MilitaryTokens.all().stream()
                    .filter(t -> t.tags().contains(MilitaryTrack.zoneTag(zone)))
                    .filter(t -> t.tags().contains(MilitaryTrack.seatTag(seat))))
            .as("zone %d seat %d", zone, seat)
            .hasSize(1);
      }
    }
  }
}
