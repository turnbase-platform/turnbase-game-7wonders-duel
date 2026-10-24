package io.turnbase.games.sevenwondersduel.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.turnbase.games.sevenwondersduel.effect.Fixtures.Card;
import org.junit.jupiter.api.Test;

class TagTest {

  @Test
  void ageTagIsNamedByItsNumber() {
    assertThat(Tag.age(2)).isEqualTo(Tag.of("age:2"));
  }

  @Test
  void blankTagIsRejected() {
    assertThatThrownBy(() -> Tag.of(" ")).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void componentsHaveNoTagsByDefault() {
    assertThat(Card.of("c").tags()).isEmpty();
  }
}
