package io.turnbase.games.sevenwondersduel.agenda;

/**
 * Sits at the bottom of the agenda while an action's effects and decisions are pending: when it
 * reaches the top, the action is done and the turn ends (B-TURN-3).
 */
public record TurnEndStep() implements Step {}
