package io.turnbase.games.sevenwondersduel.snapshot;

/**
 * Wire form of a {@code Placement}.
 *
 * @param component the component id
 * @param zone the zone id
 * @param key the location key within the zone
 */
public record PlacementData(String component, String zone, String key) {}
