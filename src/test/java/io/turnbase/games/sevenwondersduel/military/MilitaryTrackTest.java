package io.turnbase.games.sevenwondersduel.military;

import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P1;
import static org.assertj.core.api.Assertions.assertThat;

import io.turnbase.games.sevenwondersduel.id.ZoneId;
import io.turnbase.games.sevenwondersduel.zone.Location;
import org.junit.jupiter.api.Test;

class MilitaryTrackTest {

  @Test
  void spacesFallIntoTheZonesOfTheTrack() {
    int[] zones = {0, 1, 1, 2, 2, 2, 3, 3, 3, 4};
    for (int space = 0; space < zones.length; space++) {
      assertThat(MilitaryTrack.zoneOf(space)).as("space " + space).isEqualTo(zones[space]);
    }
  }

  @Test
  void aTokenSpotNamesTheSideAndTheZone() {
    assertThat(MilitaryTrack.tokenSpot(P1, 2))
        .isEqualTo(new Location(ZoneId.of("military"), P1.value() + ":2"));
  }
}
