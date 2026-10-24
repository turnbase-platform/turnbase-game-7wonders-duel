package io.turnbase.games.sevenwondersduel.snapshot;

import java.util.List;
import java.util.UUID;

/**
 * Wire form of an agenda {@code Step}.
 *
 * @param kind {@code EFFECT}, {@code DECISION} or {@code TURN_END}
 * @param owner the player who owns the step's source; {@code null} for {@code TURN_END}
 * @param decider the player who must answer; {@code null} for {@code EFFECT}
 * @param source id of the component whose effect the step names; {@code null} for {@code TURN_END}
 * @param index position of that effect in the component's declaration order
 * @param options the offered options; empty for {@code EFFECT}
 */
public record StepData(
    String kind, UUID owner, String source, int index, UUID decider, List<OptionData> options) {}
