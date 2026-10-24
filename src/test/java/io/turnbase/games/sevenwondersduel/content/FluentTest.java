package io.turnbase.games.sevenwondersduel.content;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class FluentTest {

  @Test
  void appendReturnsANewListAndLeavesTheOriginalUntouched() {
    List<String> original = List.of("a");

    List<String> result = Fluent.append(original, new String[] {"b", "c"});

    assertThat(result).containsExactly("a", "b", "c");
    assertThat(original).containsExactly("a");
  }

  @Test
  void appendingNothingCopiesTheList() {
    assertThat(Fluent.append(List.of("a"), new String[] {})).containsExactly("a");
  }
}
