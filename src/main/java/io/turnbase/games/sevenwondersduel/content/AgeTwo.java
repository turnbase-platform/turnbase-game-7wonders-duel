package io.turnbase.games.sevenwondersduel.content;

import static io.turnbase.games.sevenwondersduel.content.Chains.*;
import static io.turnbase.games.sevenwondersduel.cost.Resource.*;

import io.turnbase.games.sevenwondersduel.catalog.Tag;
import io.turnbase.games.sevenwondersduel.cost.Cost;
import io.turnbase.games.sevenwondersduel.cost.PriceModifier;
import io.turnbase.games.sevenwondersduel.cost.Production;
import io.turnbase.games.sevenwondersduel.science.ScienceSymbol;
import java.util.List;

/** The 23 cards of Age II (base.md §3.4). */
public final class AgeTwo {

  public static List<Card> cards() {
    return List.of(
        card("Sawmill", Colors.BROWN).costing(Cost.coins(2)).producing(WOOD, 2),
        card("Brickyard", Colors.BROWN).costing(Cost.coins(2)).producing(CLAY, 2),
        card("Shelf Quarry", Colors.BROWN).costing(Cost.coins(2)).producing(STONE, 2),
        card("Glass-Blower", Colors.GREY).producing(GLASS, 1),
        card("Drying Room", Colors.GREY).producing(PAPYRUS, 1),
        card("Walls", Colors.RED).costing(Cost.of(0, STONE, STONE)).shields(2),
        card("Forum", Colors.YELLOW)
            .costing(Cost.of(3, CLAY))
            .modifiers(Production.choice(false, GLASS, PAPYRUS)),
        card("Caravansery", Colors.YELLOW)
            .costing(Cost.of(2, GLASS, PAPYRUS))
            .modifiers(Production.choice(false, WOOD, CLAY, STONE)),
        card("Customs House", Colors.YELLOW)
            .costing(Cost.coins(4))
            .modifiers(PriceModifier.fixedAt(1, PAPYRUS, GLASS)),
        card("Courthouse", Colors.BLUE).costing(Cost.of(0, WOOD, WOOD, GLASS)).worth(5),
        card("Horse Breeders", Colors.RED)
            .costing(Cost.of(0, CLAY, WOOD).withFreedBy(HORSESHOE))
            .shields(1),
        card("Barracks", Colors.RED).costing(Cost.coins(3).withFreedBy(SWORD)).shields(1),
        card("Archery Range", Colors.RED)
            .costing(Cost.of(0, STONE, WOOD, PAPYRUS))
            .shields(2)
            .giving(TARGET),
        card("Parade Ground", Colors.RED)
            .costing(Cost.of(0, CLAY, CLAY, GLASS))
            .shields(2)
            .giving(HELMET),
        card("Library", Colors.GREEN)
            .costing(Cost.of(0, STONE, WOOD, GLASS).withFreedBy(BOOK))
            .showing(ScienceSymbol.QUILL)
            .worth(2),
        card("Dispensary", Colors.GREEN)
            .costing(Cost.of(0, CLAY, CLAY, STONE).withFreedBy(GEAR))
            .showing(ScienceSymbol.MORTAR_AND_PESTLE)
            .worth(2),
        card("School", Colors.GREEN)
            .costing(Cost.of(0, WOOD, PAPYRUS, PAPYRUS))
            .showing(ScienceSymbol.WHEEL)
            .worth(1)
            .giving(LYRE),
        card("Laboratory", Colors.GREEN)
            .costing(Cost.of(0, WOOD, GLASS, GLASS))
            .showing(ScienceSymbol.PLUMB_BOB)
            .worth(1)
            .giving(LAMP),
        card("Statue", Colors.BLUE)
            .costing(Cost.of(0, CLAY, CLAY).withFreedBy(THEATER))
            .worth(4)
            .giving(COLUMNS),
        card("Temple", Colors.BLUE)
            .costing(Cost.of(0, WOOD, PAPYRUS).withFreedBy(MOON))
            .worth(4)
            .giving(SUN),
        card("Aqueduct", Colors.BLUE)
            .costing(Cost.of(0, STONE, STONE, STONE).withFreedBy(WATER))
            .worth(5),
        card("Rostrum", Colors.BLUE).costing(Cost.of(0, STONE, WOOD)).worth(4).giving(TEMPLE),
        card("Brewery", Colors.YELLOW).coins(6).giving(BARREL));
  }

  private static Card card(String name, Tag colour) {
    return Card.of(name, 2, colour, Backs.BLUE);
  }

  private AgeTwo() {}
}
