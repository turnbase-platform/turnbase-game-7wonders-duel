package io.turnbase.games.sevenwondersduel.zone;

import io.turnbase.games.sevenwondersduel.id.ComponentId;

/**
 * Where one component currently is. Every component has exactly one placement.
 *
 * @param component the component
 * @param at its location
 */
public record Placement(ComponentId component, Location at) {}
