package io.turnbase.games.sevenwondersduel.wire;

import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.turnbase.games.sevenwondersduel.agenda.ComponentOption;
import io.turnbase.games.sevenwondersduel.agenda.PlayerOption;
import io.turnbase.games.sevenwondersduel.engine.Answer;
import io.turnbase.games.sevenwondersduel.engine.Build;
import io.turnbase.games.sevenwondersduel.engine.BuildWonder;
import io.turnbase.games.sevenwondersduel.engine.ChooseStarter;
import io.turnbase.games.sevenwondersduel.engine.Discard;
import io.turnbase.games.sevenwondersduel.engine.DraftWonder;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.gdk.context.ActionDecodeException;
import org.junit.jupiter.api.Test;

class WireActionsTest {

  private static final ComponentId C = ComponentId.of("c");
  private static final ComponentId W = ComponentId.of("w");

  @Test
  void eachTypeDecodesToItsAction() {
    assertThat(WireActions.decode(new WireAction("draftWonder", "w", null, null)))
        .isEqualTo(new DraftWonder(W));
    assertThat(WireActions.decode(new WireAction("chooseStarter", null, null, P1.value())))
        .isEqualTo(new ChooseStarter(P1));
    assertThat(WireActions.decode(new WireAction("discard", "c", null, null)))
        .isEqualTo(new Discard(C));
    assertThat(WireActions.decode(new WireAction("build", "c", null, null)))
        .isEqualTo(new Build(C));
    assertThat(WireActions.decode(new WireAction("buildWonder", "c", "w", null)))
        .isEqualTo(new BuildWonder(C, W));
    assertThat(WireActions.decode(new WireAction("answerComponent", "c", null, null)))
        .isEqualTo(new Answer(new ComponentOption(C)));
    assertThat(WireActions.decode(new WireAction("answerPlayer", null, null, P1.value())))
        .isEqualTo(new Answer(new PlayerOption(P1)));
  }

  @Test
  void anythingElseIsADecodeErrorNamingTheType() {
    assertThatThrownBy(() -> WireActions.decode(new WireAction("fly", null, null, null)))
        .isInstanceOf(ActionDecodeException.class)
        .hasMessageContaining("fly");
    assertThatThrownBy(() -> WireActions.decode(new WireAction(null, null, null, null)))
        .isInstanceOf(ActionDecodeException.class);
    assertThatThrownBy(() -> WireActions.decode(null)).isInstanceOf(ActionDecodeException.class);
  }

  @Test
  void aMissingFieldIsRejected() {
    assertThatThrownBy(() -> WireActions.decode(new WireAction("build", null, null, null)))
        .isInstanceOf(ActionDecodeException.class)
        .hasMessageContaining("build");
    assertThatThrownBy(() -> WireActions.decode(new WireAction("buildWonder", "c", null, null)))
        .isInstanceOf(ActionDecodeException.class);
  }

  @Test
  void anExtraFieldIsRejected() {
    assertThatThrownBy(() -> WireActions.decode(new WireAction("build", "c", "w", null)))
        .isInstanceOf(ActionDecodeException.class);
    assertThatThrownBy(
            () -> WireActions.decode(new WireAction("answerPlayer", "c", null, P1.value())))
        .isInstanceOf(ActionDecodeException.class);
  }

  @Test
  void aBlankIdIsRejectedAsADecodeError() {
    assertThatThrownBy(() -> WireActions.decode(new WireAction("build", "", null, null)))
        .isInstanceOf(ActionDecodeException.class);
  }
}
