package io.turnbase.games.sevenwondersduel.content;

import io.turnbase.games.sevenwondersduel.cost.PriceModifier;
import io.turnbase.games.sevenwondersduel.cost.Resource;
import io.turnbase.games.sevenwondersduel.effect.Modifier;
import java.util.List;
import java.util.NoSuchElementException;

/** Lookups shared by the Age table tests. */
final class CardAssertions {

  static Card named(List<Card> cards, String name) {
    return cards.stream()
        .filter(card -> card.name().equals(name))
        .findFirst()
        .orElseThrow(() -> new NoSuchElementException(name));
  }

  /** The price the card's {@link PriceModifier}s leave for {@code resource}, from a base of 2. */
  static int priceOf(Card card, Resource resource) {
    int price = 2;
    for (Modifier modifier : card.modifiers()) {
      if (modifier instanceof PriceModifier pm) {
        price = pm.price(resource, price);
      }
    }
    return price;
  }

  private CardAssertions() {}
}
