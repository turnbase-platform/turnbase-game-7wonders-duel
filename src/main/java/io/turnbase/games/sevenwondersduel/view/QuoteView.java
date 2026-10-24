package io.turnbase.games.sevenwondersduel.view;

import java.util.UUID;

/**
 * What constructing one component would cost one player right now.
 *
 * @param player who would pay
 * @param component the Building or Wonder
 * @param coins the printed coin cost
 * @param tradeCost coins paid to the bank for missing resources
 * @param total {@code coins + tradeCost}
 * @param chained whether a chain symbol waives the whole cost
 * @param affordable whether the player's treasury covers {@code total}
 */
public record QuoteView(
    UUID player,
    String component,
    int coins,
    int tradeCost,
    int total,
    boolean chained,
    boolean affordable) {}
