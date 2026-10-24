package io.turnbase.games.sevenwondersduel.event;

/** Something that changed in the game. The same stream drives triggers, the UI and the log. */
public sealed interface GameEvent permits ComponentMoved, CoinsChanged, PlayAgainChanged {}
