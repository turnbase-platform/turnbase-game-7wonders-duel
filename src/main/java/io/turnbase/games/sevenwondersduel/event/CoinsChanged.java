package io.turnbase.games.sevenwondersduel.event;

import io.turnbase.games.sevenwondersduel.id.PlayerId;

/** A treasury changed by {@code delta} (never zero). */
public record CoinsChanged(PlayerId player, int delta) implements GameEvent {}
