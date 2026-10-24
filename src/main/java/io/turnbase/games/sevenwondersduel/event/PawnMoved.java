package io.turnbase.games.sevenwondersduel.event;

import io.turnbase.games.sevenwondersduel.state.Pawn;

/** The military pawn moved from one position to another (never the same one). */
public record PawnMoved(Pawn from, Pawn to) implements GameEvent {}
