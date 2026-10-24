package io.turnbase.games.sevenwondersduel.event;

import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.zone.Location;

/** A component changed place (build, discard, steal, tuck… are all moves, design §4.2). */
public record ComponentMoved(ComponentId component, Location from, Location to)
    implements GameEvent {}
