package io.turnbase.games.sevenwondersduel.catalog;

/**
 * The numbers a pack's setup is built from (B-SET-*). The Wonder draft is a procedure of its own
 * (B-WND-3) and is not a parameter.
 *
 * @param startingCoins each player's treasury at the start
 * @param progressOnBoard progress tokens laid face up on the board; the rest go to the box
 * @param removedPerAge cards removed unseen from each Age's deck
 * @param guildsInPlay Guilds shuffled into the last Age's deck
 */
public record SetupParams(
    int startingCoins, int progressOnBoard, int removedPerAge, int guildsInPlay) {

  public SetupParams {
    positive("startingCoins", startingCoins);
    positive("progressOnBoard", progressOnBoard);
    positive("removedPerAge", removedPerAge);
    positive("guildsInPlay", guildsInPlay);
  }

  private static void positive(String name, int value) {
    if (value <= 0) {
      throw new IllegalArgumentException(name + " must be positive: " + value);
    }
  }
}
