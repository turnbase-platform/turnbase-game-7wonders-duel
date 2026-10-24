package io.turnbase.games.sevenwondersduel.content;

import static io.turnbase.games.sevenwondersduel.content.Chains.*;
import static io.turnbase.games.sevenwondersduel.cost.Resource.*;

import io.turnbase.games.sevenwondersduel.catalog.Tag;
import io.turnbase.games.sevenwondersduel.cost.Cost;
import io.turnbase.games.sevenwondersduel.effect.Census;
import io.turnbase.games.sevenwondersduel.effect.GainCoinsPer;
import io.turnbase.games.sevenwondersduel.science.ScienceSymbol;
import java.util.List;

/** The 20 cards of Age III (base.md §3.5); the Guilds are shuffled in separately. */
public final class AgeThree {

  public static List<Card> cards() {
    return List.of(
        card("Arsenal", Colors.RED).costing(Cost.of(0, CLAY, CLAY, CLAY, WOOD, WOOD)).shields(3),
        card("Pretorium", Colors.RED).costing(Cost.coins(8)).shields(3),
        card("Fortifications", Colors.RED)
            .costing(Cost.of(0, STONE, STONE, CLAY, PAPYRUS).withFreedBy(FORTRESS))
            .shields(2),
        card("Siege Workshop", Colors.RED)
            .costing(Cost.of(0, WOOD, WOOD, WOOD, GLASS).withFreedBy(TARGET))
            .shields(2),
        card("Circus", Colors.RED)
            .costing(Cost.of(0, CLAY, CLAY, STONE, STONE).withFreedBy(HELMET))
            .shields(2),
        card("Academy", Colors.GREEN)
            .costing(Cost.of(0, STONE, WOOD, GLASS, GLASS))
            .showing(ScienceSymbol.SUNDIAL)
            .worth(3),
        card("Study", Colors.GREEN)
            .costing(Cost.of(0, WOOD, WOOD, GLASS, PAPYRUS))
            .showing(ScienceSymbol.SUNDIAL)
            .worth(3),
        card("University", Colors.GREEN)
            .costing(Cost.of(0, CLAY, GLASS, PAPYRUS).withFreedBy(LYRE))
            .showing(ScienceSymbol.ASTROLABE)
            .worth(2),
        card("Observatory", Colors.GREEN)
            .costing(Cost.of(0, STONE, PAPYRUS, PAPYRUS).withFreedBy(LAMP))
            .showing(ScienceSymbol.ASTROLABE)
            .worth(2),
        card("Palace", Colors.BLUE).costing(Cost.of(0, CLAY, STONE, WOOD, GLASS, GLASS)).worth(7),
        card("Town Hall", Colors.BLUE)
            .costing(Cost.of(0, STONE, STONE, STONE, WOOD, WOOD))
            .worth(7),
        card("Obelisk", Colors.BLUE).costing(Cost.of(0, STONE, STONE, GLASS)).worth(5),
        card("Gardens", Colors.BLUE)
            .costing(Cost.of(0, CLAY, CLAY, WOOD, WOOD).withFreedBy(COLUMNS))
            .worth(6),
        card("Pantheon", Colors.BLUE)
            .costing(Cost.of(0, CLAY, WOOD, PAPYRUS, PAPYRUS).withFreedBy(SUN))
            .worth(6),
        card("Senate", Colors.BLUE)
            .costing(Cost.of(0, CLAY, CLAY, STONE, PAPYRUS).withFreedBy(TEMPLE))
            .worth(5),
        card("Chamber of Commerce", Colors.YELLOW)
            .costing(Cost.of(0, PAPYRUS, PAPYRUS))
            .effects(new GainCoinsPer(Census.of(Colors.GREY), 3))
            .worth(3),
        card("Port", Colors.YELLOW)
            .costing(Cost.of(0, WOOD, GLASS, PAPYRUS))
            .effects(new GainCoinsPer(Census.of(Colors.BROWN), 2))
            .worth(3),
        card("Armory", Colors.YELLOW)
            .costing(Cost.of(0, STONE, STONE, GLASS))
            .effects(new GainCoinsPer(Census.of(Colors.RED), 1))
            .worth(3),
        card("Lighthouse", Colors.YELLOW)
            .costing(Cost.of(0, CLAY, CLAY, GLASS).withFreedBy(VASE))
            .effects(new GainCoinsPer(Census.of(Colors.YELLOW), 1))
            .worth(3),
        card("Arena", Colors.YELLOW)
            .costing(Cost.of(0, CLAY, STONE, WOOD).withFreedBy(BARREL))
            .effects(new GainCoinsPer(Census.of(Tag.WONDER), 2))
            .worth(3));
  }

  private static Card card(String name, Tag colour) {
    return Card.of(name, 3, colour, Backs.VIOLET);
  }

  private AgeThree() {}
}
