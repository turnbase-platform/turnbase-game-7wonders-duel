package io.turnbase.games.sevenwondersduel.snapshot;

import java.util.List;
import java.util.UUID;

/**
 * Wire form of an agenda {@code Step}.
 *
 * @param kind {@code EFFECT} or {@code DECISION}
 * @param owner the player the step belongs to
 * @param source id of the component whose effect the step names
 * @param index position of that effect in the component's declaration order
 * @param options the offered options; empty for {@code EFFECT}
 */
public record StepData(
    String kind, UUID owner, String source, int index, List<OptionData> options) {}
