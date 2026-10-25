package io.turnbase.games.sevenwondersduel.cost;

/**
 * How one resource of a cost is met: {@code needed = produced + discounted + bought}.
 *
 * @param resource what the cost asks for
 * @param needed symbols the cost asks for
 * @param produced symbols the player's own city covers
 * @param discounted symbols a reduction waives
 * @param bought symbols bought from the bank
 * @param unitPrice coins the bank asks per symbol, whether or not any is bought (B-TRADE-1..3)
 */
public record QuoteLine(
    Resource resource, int needed, int produced, int discounted, int bought, int unitPrice) {}
