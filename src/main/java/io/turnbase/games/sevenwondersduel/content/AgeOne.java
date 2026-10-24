package io.turnbase.games.sevenwondersduel.content;

import static io.turnbase.games.sevenwondersduel.content.Chains.*;
import static io.turnbase.games.sevenwondersduel.cost.Resource.*;

import io.turnbase.games.sevenwondersduel.catalog.Tag;
import io.turnbase.games.sevenwondersduel.cost.Cost;
import io.turnbase.games.sevenwondersduel.cost.PriceModifier;
import io.turnbase.games.sevenwondersduel.science.ScienceSymbol;
import java.util.List;

/** The 23 cards of Age I (base.md §3.3). */
public final class AgeOne {

  public static List<Card> cards() {
    return List.of(
        card("Lumber Yard", Colors.BROWN).producing(WOOD, 1),
        card("Logging Camp", Colors.BROWN).costing(Cost.coins(1)).producing(WOOD, 1),
        card("Clay Pool", Colors.BROWN).producing(CLAY, 1),
        card("Clay Pit", Colors.BROWN).costing(Cost.coins(1)).producing(CLAY, 1),
        card("Quarry", Colors.BROWN).producing(STONE, 1),
        card("Stone Pit", Colors.BROWN).costing(Cost.coins(1)).producing(STONE, 1),
        card("Glassworks", Colors.GREY).costing(Cost.coins(1)).producing(GLASS, 1),
        card("Press", Colors.GREY).costing(Cost.coins(1)).producing(PAPYRUS, 1),
        card("Guard Tower", Colors.RED).shields(1),
        card("Stable", Colors.RED).costing(Cost.of(0, WOOD)).shields(1).giving(HORSESHOE),
        card("Garrison", Colors.RED).costing(Cost.of(0, CLAY)).shields(1).giving(SWORD),
        card("Palisade", Colors.RED).costing(Cost.coins(2)).shields(1).giving(FORTRESS),
        card("Workshop", Colors.GREEN)
            .costing(Cost.of(0, PAPYRUS))
            .showing(ScienceSymbol.PLUMB_BOB)
            .worth(1),
        card("Apothecary", Colors.GREEN)
            .costing(Cost.of(0, GLASS))
            .showing(ScienceSymbol.WHEEL)
            .worth(1),
        card("Scriptorium", Colors.GREEN)
            .costing(Cost.coins(2))
            .showing(ScienceSymbol.QUILL)
            .giving(BOOK),
        card("Pharmacist", Colors.GREEN)
            .costing(Cost.coins(2))
            .showing(ScienceSymbol.MORTAR_AND_PESTLE)
            .giving(GEAR),
        card("Theater", Colors.BLUE).worth(3).giving(THEATER),
        card("Altar", Colors.BLUE).worth(3).giving(MOON),
        card("Baths", Colors.BLUE).costing(Cost.of(0, STONE)).worth(3).giving(WATER),
        card("Stone Reserve", Colors.YELLOW)
            .costing(Cost.coins(3))
            .modifiers(PriceModifier.fixedAt(1, STONE)),
        card("Clay Reserve", Colors.YELLOW)
            .costing(Cost.coins(3))
            .modifiers(PriceModifier.fixedAt(1, CLAY)),
        card("Wood Reserve", Colors.YELLOW)
            .costing(Cost.coins(3))
            .modifiers(PriceModifier.fixedAt(1, WOOD)),
        card("Tavern", Colors.YELLOW).coins(4).giving(VASE));
  }

  private static Card card(String name, Tag colour) {
    return Card.of(name, 1, colour, Backs.BROWN);
  }

  private AgeOne() {}
}
