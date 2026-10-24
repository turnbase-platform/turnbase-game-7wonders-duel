package io.turnbase.games.sevenwondersduel.zone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.turnbase.games.sevenwondersduel.id.ZoneId;
import java.util.List;
import org.junit.jupiter.api.Test;

class ZoneRegistryTest {

  private static final ZoneKind CITY =
      new ZoneKind(ZoneId.of("city"), Visibility.PUBLIC, false, false);
  private static final ZoneKind DESTROYED =
      new ZoneKind(ZoneId.of("destroyed"), Visibility.PUBLIC, false, true);

  @Test
  void lookupReturnsTheRegisteredKind() {
    ZoneRegistry zones = ZoneRegistry.of(List.of(CITY, DESTROYED));

    assertThat(zones.kind(ZoneId.of("destroyed"))).isEqualTo(DESTROYED);
    assertThat(zones.knows(ZoneId.of("city"))).isTrue();
    assertThat(zones.knows(ZoneId.of("moon"))).isFalse();
  }

  @Test
  void unknownZoneNamesTheId() {
    ZoneRegistry zones = ZoneRegistry.of(List.of(CITY));

    assertThatThrownBy(() -> zones.kind(ZoneId.of("moon")))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("moon");
  }

  @Test
  void duplicateZoneIdIsRejected() {
    assertThatThrownBy(() -> ZoneRegistry.of(List.of(CITY, CITY)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("city");
  }

  @Test
  void locationWithoutKeyUsesEmptyKey() {
    assertThat(Location.of(ZoneId.of("box"))).isEqualTo(new Location(ZoneId.of("box"), ""));
  }
}
