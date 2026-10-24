package io.turnbase.games.sevenwondersduel.agenda;

/** One thing a player may pick when an effect asks for a choice (B-EFF-3). */
public sealed interface Option permits ComponentOption, PlayerOption {}
