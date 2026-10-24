package io.turnbase.games.sevenwondersduel.state;

import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class PawnTest {

  @Test
  void centreHasNoSide() {
    assertThat(Pawn.CENTRE.side()).isNull();
    assertThat(Pawn.CENTRE.space()).isZero();
  }

  @Test
  void offCentrePawnNamesItsSide() {
    Pawn pawn = new Pawn(P1, 4);

    assertThat(pawn.side()).isEqualTo(P1);
    assertThat(pawn.space()).isEqualTo(4);
  }

  @Test
  void capitalIsTheLastSpace() {
    assertThat(new Pawn(P1, Pawn.CAPITAL).space()).isEqualTo(9);
  }

  @Test
  void offCentreWithoutSideIsRejected() {
    assertThatThrownBy(() -> new Pawn(null, 3))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("side");
  }

  @Test
  void centreWithSideIsRejected() {
    assertThatThrownBy(() -> new Pawn(P1, 0))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("centre");
  }

  @Test
  void spaceOutsideTheTrackIsRejected() {
    assertThatThrownBy(() -> new Pawn(P1, 10)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new Pawn(P1, -1)).isInstanceOf(IllegalArgumentException.class);
  }
}
