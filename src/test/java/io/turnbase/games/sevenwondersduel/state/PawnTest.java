package io.turnbase.games.sevenwondersduel.state;

import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P1;
import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P2;
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

  @Test
  void fromTheCentreAStepEntersTheTargetsSide() {
    assertThat(Pawn.CENTRE.stepToward(P2)).isEqualTo(new Pawn(P2, 1));
  }

  @Test
  void onTheTargetsSideAStepGoesOutAndStopsAtTheCapital() {
    assertThat(new Pawn(P2, 4).stepToward(P2)).isEqualTo(new Pawn(P2, 5));
    assertThat(new Pawn(P2, 8).stepToward(P2)).isEqualTo(new Pawn(P2, Pawn.CAPITAL));
    assertThat(new Pawn(P2, Pawn.CAPITAL).stepToward(P2)).isEqualTo(new Pawn(P2, Pawn.CAPITAL));
  }

  @Test
  void onTheOtherSideAStepGoesInAndLeavesTheSideAtTheCentre() {
    assertThat(new Pawn(P1, 4).stepToward(P2)).isEqualTo(new Pawn(P1, 3));
    assertThat(new Pawn(P1, 1).stepToward(P2)).isEqualTo(Pawn.CENTRE);
  }
}
