package io.turnbase.games.sevenwondersduel.agenda;

import java.util.ArrayList;
import java.util.List;

/**
 * The stack of pending steps, top first (design §4.5). Follow-ups go on the front, so resolution is
 * deterministic and depth-first, in printed order (B-EFF-4).
 *
 * @param steps the pending steps; index 0 is the top
 */
public record Agenda(List<Step> steps) {

  public static final Agenda EMPTY = new Agenda(List.of());

  public Agenda {
    steps = List.copyOf(steps);
  }

  public boolean isEmpty() {
    return steps.isEmpty();
  }

  public Step top() {
    if (steps.isEmpty()) {
      throw new IllegalStateException("the agenda is empty");
    }
    return steps.get(0);
  }

  /** The agenda without its top step. */
  public Agenda pop() {
    top();
    return new Agenda(steps.subList(1, steps.size()));
  }

  /** The agenda with {@code added} in front; the first of them becomes the top. */
  public Agenda pushAll(List<Step> added) {
    List<Step> result = new ArrayList<>(added);
    result.addAll(steps);
    return new Agenda(result);
  }
}
