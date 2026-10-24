package io.turnbase.games.sevenwondersduel.agenda;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.turnbase.games.sevenwondersduel.event.EffectRef;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AgendaTest {

  private static final PlayerId P1 =
      PlayerId.of(UUID.fromString("00000000-0000-0000-0000-000000000001"));

  private static Step step(String source) {
    return new EffectStep(new EffectRef(ComponentId.of(source), 0), P1);
  }

  @Test
  void pushAllPutsFirstElementOnTop() {
    Agenda agenda = new Agenda(List.of(step("c"))).pushAll(List.of(step("a"), step("b")));

    assertThat(agenda.steps()).containsExactly(step("a"), step("b"), step("c"));
    assertThat(agenda.top()).isEqualTo(step("a"));
  }

  @Test
  void popRemovesTopAndLeavesOriginalUntouched() {
    Agenda agenda = new Agenda(List.of(step("a"), step("b")));

    assertThat(agenda.pop().steps()).containsExactly(step("b"));
    assertThat(agenda.steps()).hasSize(2);
  }

  @Test
  void topOfEmptyAgendaFails() {
    assertThat(Agenda.EMPTY.isEmpty()).isTrue();
    assertThatThrownBy(Agenda.EMPTY::top).isInstanceOf(IllegalStateException.class);
    assertThatThrownBy(Agenda.EMPTY::pop).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void decisionRejectsEmptyAndDuplicateOptions() {
    EffectRef ref = new EffectRef(ComponentId.of("c"), 0);
    Option token = new ComponentOption(ComponentId.of("t"));

    assertThatThrownBy(() -> new DecisionStep(P1, P1, ref, List.of()))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new DecisionStep(P1, P1, ref, List.of(token, token)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void agendaCopiesItsInput() {
    List<Step> source = new ArrayList<>(List.of(step("a")));
    Agenda agenda = new Agenda(source);

    source.add(step("b"));

    assertThat(agenda.steps()).containsExactly(step("a"));
  }
}
