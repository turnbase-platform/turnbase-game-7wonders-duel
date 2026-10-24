package io.turnbase.games.sevenwondersduel.content;

import static io.turnbase.games.sevenwondersduel.cost.Resource.*;

import io.turnbase.games.sevenwondersduel.catalog.Tag;
import io.turnbase.games.sevenwondersduel.cost.Cost;
import io.turnbase.games.sevenwondersduel.effect.Census;
import io.turnbase.games.sevenwondersduel.effect.GainCoinsPer;
import io.turnbase.games.sevenwondersduel.scoring.Points;
import java.util.List;

/**
 * The 7 guilds (base.md §3.6). Each counts "the city with the most" through {@link
 * Census#largerOfBoth}, so coins on construction and points at game end take their own maximum.
 */
public final class Guilds {

  public static List<Card> cards() {
    return List.of(
        perCard("Merchants Guild", Cost.of(0, CLAY, WOOD, GLASS, PAPYRUS), Colors.YELLOW),
        perCard(
            "Shipowners Guild",
            Cost.of(0, CLAY, STONE, GLASS, PAPYRUS),
            Census.sum(Census.of(Colors.BROWN), Census.of(Colors.GREY))),
        Card.guild("Builders Guild")
            .costing(Cost.of(0, STONE, STONE, CLAY, WOOD, GLASS))
            .modifiers(Points.per(Census.largerOfBoth(Census.of(Tag.WONDER)), 2)),
        perCard("Magistrates Guild", Cost.of(0, WOOD, WOOD, CLAY, PAPYRUS), Colors.BLUE),
        perCard("Scientists Guild", Cost.of(0, CLAY, CLAY, WOOD, WOOD), Colors.GREEN),
        Card.guild("Moneylenders Guild")
            .costing(Cost.of(0, STONE, STONE, WOOD, WOOD))
            .modifiers(Points.per(Census.every(3, Census.largerOfBoth(Census.coins())), 1)),
        perCard("Tacticians Guild", Cost.of(0, STONE, STONE, CLAY, PAPYRUS), Colors.RED));
  }

  private static Card perCard(String name, Cost cost, Tag colour) {
    return perCard(name, cost, Census.of(colour));
  }

  /** 1$ on construction and 1 VP at the end for each unit in the larger city. */
  private static Card perCard(String name, Cost cost, Census census) {
    Census largest = Census.largerOfBoth(census);
    return Card.guild(name)
        .costing(cost)
        .effects(new GainCoinsPer(largest, 1))
        .modifiers(Points.per(largest, 1));
  }

  private Guilds() {}
}
