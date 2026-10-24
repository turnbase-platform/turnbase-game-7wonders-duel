package io.turnbase.games.sevenwondersduel.event;

import io.turnbase.games.sevenwondersduel.id.PlayerId;

/** A player's play-again flag was set or cleared (B-TURN-6). */
public record PlayAgainChanged(PlayerId player, boolean granted) implements GameEvent {}
