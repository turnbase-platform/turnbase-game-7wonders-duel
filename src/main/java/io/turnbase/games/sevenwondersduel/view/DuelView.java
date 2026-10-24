package io.turnbase.games.sevenwondersduel.view;

import java.util.List;
import java.util.UUID;

/**
 * What one viewer may see of a game, in GDK-legal types. Components the viewer cannot identify
 * appear only as counts in {@code hidden}.
 *
 * @param phase name of the current {@code Phase}
 * @param active the player whose turn it is
 * @param age the Age being played, 0 before Age I
 * @param first the first player of the Wonder draft
 * @param deciding who must choose the Age's starting player, or null
 * @param players both seats, ordered by id
 * @param pawnSide the player whose side the military pawn is on, or null at the centre
 * @param pawnSpace the pawn's distance from the centre
 * @param victoryWinner the winner, or null while running or for a shared victory
 * @param victoryReason name of the {@code Victory.Reason}, null while the game is running
 * @param visible every component the viewer may identify, in placement order
 * @param hidden the rest, by place
 * @param pending the decision waiting for an answer, or null
 */
public record DuelView(
    String phase,
    UUID active,
    int age,
    UUID first,
    UUID deciding,
    List<PlayerView> players,
    UUID pawnSide,
    int pawnSpace,
    UUID victoryWinner,
    String victoryReason,
    List<PlacedView> visible,
    List<HiddenView> hidden,
    PendingView pending) {}
