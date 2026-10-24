package io.turnbase.games.sevenwondersduel;

import io.turnbase.games.sevenwondersduel.snapshot.PlacementData;
import io.turnbase.games.sevenwondersduel.snapshot.PlayerData;
import io.turnbase.games.sevenwondersduel.snapshot.StepData;
import io.turnbase.gdk.GameState;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The wire form of a game, shaped by the GDK serialization rules (records, {@code List}, {@code
 * String}, {@code UUID}). The engine works on {@code DuelState}; {@code snapshot.Snapshots} maps
 * between the two.
 *
 * @param phase name of the current {@code Phase}
 * @param active the player whose turn it is
 * @param age the Age being played, 0 before Age I
 * @param first the first player of the Wonder draft
 * @param deciding who must choose the Age's starting player, or null when nobody must
 * @param placements where every component is, in placement order
 * @param players per-player scalar state
 * @param knowledge per player, the sorted ids of components they have seen
 * @param rngSeed seed of the random stream
 * @param rngDraws values already drawn from the stream
 * @param packs ids of the content packs in play
 * @param agenda the pending steps, top first
 * @param pawnSide the player whose side the military pawn is on, or null at the centre
 * @param pawnSpace the pawn's distance from the centre
 * @param victoryWinner who won by supremacy, or null while the game is running
 * @param victoryReason name of the {@code Victory.Reason}, null exactly when there is no winner
 */
public record SevenWondersDuelState(
    String phase,
    UUID active,
    int age,
    UUID first,
    UUID deciding,
    List<PlacementData> placements,
    Map<UUID, PlayerData> players,
    Map<UUID, List<String>> knowledge,
    long rngSeed,
    long rngDraws,
    List<String> packs,
    List<StepData> agenda,
    UUID pawnSide,
    int pawnSpace,
    UUID victoryWinner,
    String victoryReason)
    implements GameState {}
