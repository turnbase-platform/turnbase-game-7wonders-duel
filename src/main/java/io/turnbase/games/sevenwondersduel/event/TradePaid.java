package io.turnbase.games.sevenwondersduel.event;

import io.turnbase.games.sevenwondersduel.id.PlayerId;

/**
 * A player paid coins for resources bought from the bank (B-TRADE-4); the printed coin cost of a
 * component is not part of it.
 */
public record TradePaid(PlayerId payer, int amount) implements GameEvent {}
