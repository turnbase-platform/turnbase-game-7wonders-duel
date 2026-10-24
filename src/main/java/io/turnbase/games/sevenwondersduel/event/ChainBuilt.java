package io.turnbase.games.sevenwondersduel.event;

import io.turnbase.games.sevenwondersduel.id.PlayerId;

/** A player built a card for free through a chain symbol (B-COST-5). */
public record ChainBuilt(PlayerId builder) implements GameEvent {}
