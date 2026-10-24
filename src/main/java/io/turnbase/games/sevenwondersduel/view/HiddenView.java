package io.turnbase.games.sevenwondersduel.view;

/**
 * Components the viewer cannot identify, collapsed per place and back group so neither their
 * identity nor their order can be read off the view. The back is public (B-BACK-1).
 *
 * @param zone the zone id
 * @param key the place within the zone, empty when the zone has none
 * @param back the back group id, {@code null} for a component without a back
 * @param count how many components lie there with that back
 */
public record HiddenView(String zone, String key, String back, int count) {}
