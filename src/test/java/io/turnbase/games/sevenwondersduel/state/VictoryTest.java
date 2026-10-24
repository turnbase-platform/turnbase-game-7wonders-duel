package io.turnbase.games.sevenwondersduel.state;

import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class VictoryTest {

  @Test
  void aSharedVictoryHasNoWinner() {
    assertThat(new Victory(null, Victory.Reason.POINTS).winner()).isNull();
  }

  @Test
  void onlyAScoredGameCanBeShared() {
    assertThatThrownBy(() -> new Victory(null, Victory.Reason.MILITARY))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new Victory(null, Victory.Reason.SCIENCE))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void aVictoryNeedsAReason() {
    assertThatThrownBy(() -> new Victory(P1, null)).isInstanceOf(IllegalArgumentException.class);
  }
}
