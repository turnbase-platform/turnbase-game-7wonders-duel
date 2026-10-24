package io.turnbase.games.sevenwondersduel.rng;

import static org.assertj.core.api.Assertions.assertThat;

import io.turnbase.games.sevenwondersduel.rng.Rng.Drawn;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class RngTest {

  @Test
  void sameCursorGivesSameDraw() {
    Drawn<Integer> a = Rng.nextInt(RngCursor.start(42), 100);
    Drawn<Integer> b = Rng.nextInt(RngCursor.start(42), 100);

    assertThat(a).isEqualTo(b);
  }

  @Test
  void drawAdvancesCursorAndFollowsOneJavaRandomSequence() {
    Random reference = new Random(42);
    RngCursor cursor = RngCursor.start(42);
    for (int i = 1; i <= 3; i++) {
      Drawn<Integer> drawn = Rng.nextInt(cursor, 100);
      assertThat(drawn.value()).isEqualTo(reference.nextInt(100));
      assertThat(drawn.next().draws()).isEqualTo(i);
      cursor = drawn.next();
    }
  }

  @Test
  void shuffleIsAPermutationAndDeterministic() {
    List<Integer> input = IntStream.rangeClosed(1, 20).boxed().toList();
    List<Integer> snapshot = new ArrayList<>(input);

    Drawn<List<Integer>> first = Rng.shuffle(input, RngCursor.start(7));
    Drawn<List<Integer>> second = Rng.shuffle(input, RngCursor.start(7));

    assertThat(first.value()).containsExactlyInAnyOrderElementsOf(input);
    assertThat(first.value()).isEqualTo(second.value());
    assertThat(first.value()).isNotEqualTo(input);
    assertThat(input).isEqualTo(snapshot);
  }

  @Test
  void shuffleEdgeCases() {
    RngCursor start = RngCursor.start(1);

    Drawn<List<Integer>> empty = Rng.shuffle(List.of(), start);
    Drawn<List<Integer>> single = Rng.shuffle(List.of(5), start);

    assertThat(empty.value()).isEmpty();
    assertThat(single.value()).containsExactly(5);
    assertThat(empty.next()).isEqualTo(start);
    assertThat(single.next()).isEqualTo(start);
  }

  @Test
  void cursorContinuesAfterRebuildFromSeedAndDraws() {
    RngCursor advanced = Rng.nextInt(Rng.nextInt(RngCursor.start(7), 10).next(), 10).next();
    RngCursor rebuilt = new RngCursor(advanced.seed(), advanced.draws());

    assertThat(Rng.nextInt(rebuilt, 1000)).isEqualTo(Rng.nextInt(advanced, 1000));
  }
}
