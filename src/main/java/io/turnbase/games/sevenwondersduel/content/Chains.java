package io.turnbase.games.sevenwondersduel.content;

import io.turnbase.games.sevenwondersduel.cost.ChainSource;
import io.turnbase.games.sevenwondersduel.cost.ChainSymbol;
import java.util.Set;

/** The base chain symbols (B-COST-5), named after the icons the cards print. */
public final class Chains {

  public static final ChainSymbol VASE = new ChainSymbol("vase");
  public static final ChainSymbol BARREL = new ChainSymbol("barrel");
  public static final ChainSymbol THEATER = new ChainSymbol("theater");
  public static final ChainSymbol TEMPLE = new ChainSymbol("temple");
  public static final ChainSymbol SUN = new ChainSymbol("sun");
  public static final ChainSymbol WATER = new ChainSymbol("water");
  public static final ChainSymbol COLUMNS = new ChainSymbol("columns");
  public static final ChainSymbol MOON = new ChainSymbol("moon");
  public static final ChainSymbol TARGET = new ChainSymbol("target");
  public static final ChainSymbol HELMET = new ChainSymbol("helmet");
  public static final ChainSymbol HORSESHOE = new ChainSymbol("horseshoe");
  public static final ChainSymbol SWORD = new ChainSymbol("sword");
  public static final ChainSymbol FORTRESS = new ChainSymbol("fortress");
  public static final ChainSymbol LYRE = new ChainSymbol("lyre");
  public static final ChainSymbol GEAR = new ChainSymbol("gear");
  public static final ChainSymbol BOOK = new ChainSymbol("book");
  public static final ChainSymbol LAMP = new ChainSymbol("lamp");

  /** The modifier a card carries when it shows these symbols. */
  public static ChainSource gives(ChainSymbol... symbols) {
    return new ChainSource(Set.of(symbols));
  }

  private Chains() {}
}
