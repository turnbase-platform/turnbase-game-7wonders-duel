package io.turnbase.games.sevenwondersduel.zone;

import static org.assertj.core.api.Assertions.assertThat;

import io.turnbase.games.sevenwondersduel.catalog.BasePack;
import io.turnbase.games.sevenwondersduel.id.ZoneId;
import java.util.List;
import org.junit.jupiter.api.Test;

class ZonesTest {

  @Test
  void theIdsAreThePublishedZoneNames() {
    assertThat(Zones.ALL)
        .extracting(ZoneId::value)
        .containsExactly(
            "deck",
            "structure.up",
            "structure.down",
            "city",
            "underWonder",
            "discard",
            "draft",
            "wonders.unbuilt",
            "wonders.built",
            "board",
            "box",
            "military",
            "destroyed");
  }

  @Test
  void theBasePackDefinesExactlyThoseZones() {
    List<ZoneId> defined = new BasePack().zoneKinds().stream().map(ZoneKind::id).toList();

    assertThat(defined).containsExactlyElementsOf(Zones.ALL);
  }
}
