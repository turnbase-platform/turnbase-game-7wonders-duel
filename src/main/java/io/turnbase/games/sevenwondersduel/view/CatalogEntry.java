package io.turnbase.games.sevenwondersduel.view;

import io.turnbase.games.sevenwondersduel.effect.Badge;
import java.util.List;

/**
 * One component as a client draws it: static facts only, so every client can have it whether or not
 * it may see the component in play.
 *
 * @param id the component id the views and events name
 * @param description what the component does in words, or empty
 * @param type {@code CARD}, {@code WONDER} or {@code TOKEN}
 * @param tags the component's tags, sorted
 * @param back the back group id, or null when it has none
 * @param badges what its effects, modifiers and triggers show, in that order
 */
public record CatalogEntry(
    String id,
    String name,
    String description,
    String type,
    List<String> tags,
    String back,
    CostView cost,
    List<Badge> badges) {

  public CatalogEntry {
    tags = List.copyOf(tags);
    badges = List.copyOf(badges);
  }
}
