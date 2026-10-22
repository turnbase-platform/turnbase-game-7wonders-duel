package io.turnbase.games.sevenwondersduel;

/**
 * The action payload a frontend sends. {@code type} names the action; nullable because a client can
 * send {@code {}}, which the game rejects rather than failing the hook.
 *
 * <p>Skeleton: the only action is {@code "END_TURN"}. Real moves (pick a card, build a wonder,
 * discard for coins, choose a starting player…) become further {@code type}s, with their arguments
 * added as fields.
 */
public record Action(String type) {

  /** Hands the turn to the opponent. */
  public static final String END_TURN = "END_TURN";
}
