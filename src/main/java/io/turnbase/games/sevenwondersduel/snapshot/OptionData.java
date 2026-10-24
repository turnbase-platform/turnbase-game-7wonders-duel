package io.turnbase.games.sevenwondersduel.snapshot;

/**
 * Wire form of an {@code Option}.
 *
 * @param kind {@code COMPONENT} or {@code PLAYER}
 * @param value the component id, or the player's UUID as text
 */
public record OptionData(String kind, String value) {}
