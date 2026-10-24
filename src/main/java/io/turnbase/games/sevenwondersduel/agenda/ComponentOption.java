package io.turnbase.games.sevenwondersduel.agenda;

import io.turnbase.games.sevenwondersduel.id.ComponentId;

/**
 * Picking a card, Wonder or token.
 *
 * @param component the component offered
 */
public record ComponentOption(ComponentId component) implements Option {}
