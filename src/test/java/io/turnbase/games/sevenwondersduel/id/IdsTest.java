package io.turnbase.games.sevenwondersduel.id;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class IdsTest {

  @Test
  void equalValuesAreEqual() {
    UUID uuid = UUID.randomUUID();

    assertThat(ComponentId.of("lumber-yard")).isEqualTo(new ComponentId("lumber-yard"));
    assertThat(ZoneId.of("city")).isEqualTo(new ZoneId("city"));
    assertThat(PackId.of("base")).isEqualTo(new PackId("base"));
    assertThat(PlayerId.of(uuid)).isEqualTo(new PlayerId(uuid));
  }

  @Test
  void blankAndNullStringIdsAreRejected() {
    assertThatThrownBy(() -> ComponentId.of(" ")).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> ZoneId.of("")).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> PackId.of(null)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void nullPlayerIdIsRejected() {
    assertThatThrownBy(() -> PlayerId.of(null)).isInstanceOf(IllegalArgumentException.class);
  }
}
