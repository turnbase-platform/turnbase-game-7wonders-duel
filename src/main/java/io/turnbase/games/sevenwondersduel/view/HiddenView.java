package io.turnbase.games.sevenwondersduel.view;

/**
 * Components the viewer cannot identify, collapsed per place so neither their identity nor their
 * order can be read off the view.
 *
 * @param zone the zone id
 * @param key the place within the zone, empty when the zone has none
 * @param count how many components lie there
 */
public record HiddenView(String zone, String key, int count) {}
