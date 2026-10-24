package io.turnbase.games.sevenwondersduel.view;

import java.util.UUID;

/**
 * Where the military pawn stands.
 *
 * @param side the player whose side it is on, null at the centre
 * @param space its distance from the centre
 */
public record PawnView(UUID side, int space) {}
