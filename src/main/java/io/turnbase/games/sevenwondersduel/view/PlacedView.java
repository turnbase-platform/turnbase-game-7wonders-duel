package io.turnbase.games.sevenwondersduel.view;

/**
 * A component the viewer may identify, and where it lies.
 *
 * @param component the component id
 * @param zone the zone id
 * @param key the place within the zone, empty when the zone has none
 */
public record PlacedView(String component, String zone, String key) {}
