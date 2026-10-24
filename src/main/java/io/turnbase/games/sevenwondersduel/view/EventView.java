package io.turnbase.games.sevenwondersduel.view;

import java.util.UUID;

/**
 * One thing that happened, as one viewer may know it. A flat record because the GDK has no
 * polymorphic serialization; which fields are set depends on {@code kind}: {@code moved}
 * (component, from, to), {@code coins} (player, amount), {@code trade} (player, amount), {@code
 * chain} (player, component), {@code pawn} (pawnFrom, pawnTo), {@code playAgainGranted} and {@code
 * playAgainCleared} (player).
 *
 * @param id sequential within one action, starting at 0
 * @param parent id of the event that caused this one, -1 for a root event
 * @param kind what happened
 * @param cause the component whose effect or trigger emitted it; null for the player's own action,
 *     or when the viewer cannot identify it
 * @param component the component concerned; null when the viewer cannot identify it
 * @param from where a moved component was
 * @param to where a moved component is
 * @param player the player concerned
 * @param amount coins changed (signed) or paid
 * @param pawnFrom where the pawn was
 * @param pawnTo where the pawn is
 */
public record EventView(
    int id,
    int parent,
    String kind,
    String cause,
    String component,
    PlaceView from,
    PlaceView to,
    UUID player,
    int amount,
    PawnView pawnFrom,
    PawnView pawnTo) {}
