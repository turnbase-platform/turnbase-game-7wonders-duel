package io.turnbase.games.sevenwondersduel.event;

import io.turnbase.games.sevenwondersduel.id.ComponentId;

/**
 * Names the cause of an event: the {@code index}-th effect or trigger of {@code source}.
 *
 * @param source the component the effect belongs to
 * @param index position in the component's declaration order
 */
public record EffectRef(ComponentId source, int index) {}
