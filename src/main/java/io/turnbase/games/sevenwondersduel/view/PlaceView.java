package io.turnbase.games.sevenwondersduel.view;

/**
 * A place on the table.
 *
 * @param zone the zone id
 * @param key the place within the zone, empty when the zone has none
 */
public record PlaceView(String zone, String key) {}
