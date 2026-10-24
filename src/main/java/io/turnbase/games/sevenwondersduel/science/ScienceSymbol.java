package io.turnbase.games.sevenwondersduel.science;

/** A scientific symbol, such as the wheel or the scales (B-SCI-2). */
public record ScienceSymbol(String id) {

  public static final ScienceSymbol PLUMB_BOB = new ScienceSymbol("plumb-bob");
  public static final ScienceSymbol WHEEL = new ScienceSymbol("wheel");
  public static final ScienceSymbol QUILL = new ScienceSymbol("quill");
  public static final ScienceSymbol MORTAR_AND_PESTLE = new ScienceSymbol("mortar-and-pestle");
  public static final ScienceSymbol SUNDIAL = new ScienceSymbol("sundial");
  public static final ScienceSymbol SCALES = new ScienceSymbol("scales");
  public static final ScienceSymbol ASTROLABE = new ScienceSymbol("astrolabe");

  public ScienceSymbol {
    if (id == null || id.isBlank()) {
      throw new IllegalArgumentException("ScienceSymbol id must not be blank");
    }
  }
}
