package io.turnbase.games.sevenwondersduel.zone;

import io.turnbase.games.sevenwondersduel.id.ZoneId;

/**
 * A kind of place registered by a content pack (base.md §10.4).
 *
 * @param id the zone id
 * @param visibility who may see identities inside the zone
 * @param ordered whether the order of components in the zone is meaningful
 * @param terminal whether the engine forbids moving anything out once it is in
 */
public record ZoneKind(ZoneId id, Visibility visibility, boolean ordered, boolean terminal) {}
