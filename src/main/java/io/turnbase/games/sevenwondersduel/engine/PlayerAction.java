package io.turnbase.games.sevenwondersduel.engine;

/** Something a player asks the game to do. Later plans add the main actions of a turn. */
public sealed interface PlayerAction permits Answer {}
