package io.turnbase.games.sevenwondersduel.cost;

/** A kind of resource a city produces and a cost asks for. Open, so an expansion adds its own. */
public record Resource(String id) {

  public static final Resource WOOD = new Resource("wood");
  public static final Resource STONE = new Resource("stone");
  public static final Resource CLAY = new Resource("clay");
  public static final Resource GLASS = new Resource("glass");
  public static final Resource PAPYRUS = new Resource("papyrus");

  public Resource {
    if (id == null || id.isBlank()) {
      throw new IllegalArgumentException("Resource id must not be blank");
    }
  }
}
