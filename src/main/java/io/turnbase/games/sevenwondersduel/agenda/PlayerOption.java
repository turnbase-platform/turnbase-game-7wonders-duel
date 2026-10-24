package io.turnbase.games.sevenwondersduel.agenda;

import io.turnbase.games.sevenwondersduel.id.PlayerId;

/**
 * Picking a player, e.g. who starts an Age (B-AGE-0).
 *
 * @param player the player offered
 */
public record PlayerOption(PlayerId player) implements Option {}
