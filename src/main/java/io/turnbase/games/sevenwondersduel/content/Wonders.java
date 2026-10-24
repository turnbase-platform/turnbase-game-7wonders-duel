package io.turnbase.games.sevenwondersduel.content;

import static io.turnbase.games.sevenwondersduel.cost.Resource.*;

import io.turnbase.games.sevenwondersduel.cost.Cost;
import io.turnbase.games.sevenwondersduel.effect.BuildFromDiscard;
import io.turnbase.games.sevenwondersduel.effect.DiscardOpponentCard;
import io.turnbase.games.sevenwondersduel.effect.DrawProgressToken;
import io.turnbase.games.sevenwondersduel.effect.LoseCoins;
import java.util.List;

/**
 * The 12 Wonders (base.md §3.7), effects in printed order. Their shields are not boosted by
 * Strategy because a Wonder is not a red card (B-PRG).
 */
public final class Wonders {

  public static List<Wonder> all() {
    return List.of(
        Wonder.of("The Appian Way")
            .costing(Cost.of(0, STONE, STONE, CLAY, CLAY, PAPYRUS))
            .coins(3)
            .effects(new LoseCoins(3))
            .playAgain()
            .worth(3),
        Wonder.of("Circus Maximus")
            .costing(Cost.of(0, STONE, STONE, WOOD, GLASS))
            .effects(new DiscardOpponentCard(Colors.GREY))
            .shields(1)
            .worth(3),
        Wonder.of("The Colossus").costing(Cost.of(0, CLAY, CLAY, CLAY, GLASS)).shields(2).worth(3),
        Wonder.of("The Great Library")
            .costing(Cost.of(0, WOOD, WOOD, WOOD, GLASS, PAPYRUS))
            .effects(new DrawProgressToken(3))
            .worth(4),
        Wonder.of("The Great Lighthouse")
            .costing(Cost.of(0, WOOD, STONE, PAPYRUS, PAPYRUS))
            .producingOneOf(WOOD, CLAY, STONE)
            .worth(4),
        Wonder.of("The Hanging Gardens")
            .costing(Cost.of(0, WOOD, WOOD, GLASS, PAPYRUS))
            .coins(6)
            .playAgain()
            .worth(3),
        Wonder.of("The Mausoleum")
            .costing(Cost.of(0, CLAY, CLAY, GLASS, GLASS, PAPYRUS))
            .effects(new BuildFromDiscard())
            .worth(2),
        Wonder.of("Piraeus")
            .costing(Cost.of(0, WOOD, WOOD, STONE, CLAY))
            .producingOneOf(GLASS, PAPYRUS)
            .playAgain()
            .worth(2),
        Wonder.of("The Pyramids").costing(Cost.of(0, STONE, STONE, STONE, PAPYRUS)).worth(9),
        Wonder.of("The Sphinx").costing(Cost.of(0, STONE, CLAY, GLASS, GLASS)).playAgain().worth(6),
        Wonder.of("The Statue of Zeus")
            .costing(Cost.of(0, STONE, WOOD, CLAY, PAPYRUS, PAPYRUS))
            .effects(new DiscardOpponentCard(Colors.BROWN))
            .shields(1)
            .worth(3),
        Wonder.of("The Temple of Artemis")
            .costing(Cost.of(0, WOOD, STONE, GLASS, PAPYRUS))
            .coins(12)
            .playAgain());
  }

  private Wonders() {}
}
