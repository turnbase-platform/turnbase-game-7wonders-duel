package io.turnbase.games.sevenwondersduel.agenda;

/** One pending item of the agenda: work to do, or a player to wait for (design §4.5). */
public sealed interface Step permits EffectStep, DecisionStep, TurnEndStep {}
