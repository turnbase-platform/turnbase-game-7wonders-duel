package io.turnbase.games.sevenwondersduel.cost;

import static io.turnbase.games.sevenwondersduel.cost.Resource.CLAY;
import static io.turnbase.games.sevenwondersduel.cost.Resource.GLASS;
import static io.turnbase.games.sevenwondersduel.cost.Resource.STONE;
import static io.turnbase.games.sevenwondersduel.cost.Resource.WOOD;
import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P1;
import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P2;
import static org.assertj.core.api.Assertions.assertThat;

import io.turnbase.games.sevenwondersduel.catalog.Component;
import io.turnbase.games.sevenwondersduel.catalog.ComponentType;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.effect.Fixtures;
import io.turnbase.games.sevenwondersduel.effect.Modifier;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CostSolverReductionTest {

  private static final ChainSymbol HORSESHOE = new ChainSymbol("horseshoe");

  record Piece(ComponentId id, ComponentType type, Cost cost, List<Modifier> modifiers)
      implements Component {
    static Piece target(String id, ComponentType type, Cost cost) {
      return new Piece(ComponentId.of(id), type, cost, List.of());
    }

    static Piece holding(String id, Modifier... modifiers) {
      return new Piece(ComponentId.of(id), ComponentType.CARD, Cost.FREE, List.of(modifiers));
    }

    @Override
    public String name() {
      return id.value();
    }
  }

  private static Quote quote(Piece target, List<Piece> own, List<Piece> theirs) {
    List<Component> all = new ArrayList<>(List.of(target));
    List<Object> placed = new ArrayList<>(List.of(target.id(), Fixtures.zone("deck")));
    for (Piece p : own) {
      all.add(p);
      placed.addAll(List.of(p.id(), Fixtures.city(P1)));
    }
    for (Piece p : theirs) {
      all.add(p);
      placed.addAll(List.of(p.id(), Fixtures.city(P2)));
    }
    RuleSet rules = Fixtures.rules(all.toArray(Component[]::new));
    DuelState state = Fixtures.state(rules, P1, 20, 20, placed.toArray());
    return new CostSolver(rules).quote(state, P1, target.id());
  }

  private static Piece masonry(int symbols, ComponentType type) {
    return masonry("red", symbols, type);
  }

  private static Piece masonry(String id, int symbols, ComponentType type) {
    return Piece.holding(id, new Reduction(symbols, c -> c.type() == type));
  }

  @Test
  void aHeldChainSymbolFreesTheWholeCostCoinsIncluded() {
    Piece target =
        Piece.target("t", ComponentType.CARD, Cost.of(3, WOOD, STONE).withFreedBy(HORSESHOE));

    Quote q =
        quote(
            target,
            List.of(Piece.holding("c", new ChainSource(java.util.Set.of(HORSESHOE)))),
            List.of());

    assertThat(q.chained()).isTrue();
    assertThat(q.total()).isZero();
    assertThat(q.bought()).isEmpty();
  }

  @Test
  void aChainSymbolThatFreesSomethingElseDoesNothing() {
    Piece target = Piece.target("t", ComponentType.CARD, Cost.of(3, WOOD).withFreedBy(HORSESHOE));

    Quote q =
        quote(
            target,
            List.of(Piece.holding("c", new ChainSource(java.util.Set.of(new ChainSymbol("moon"))))),
            List.of());

    assertThat(q.chained()).isFalse();
    assertThat(q.total()).isEqualTo(3 + 2);
  }

  @Test
  void aReductionRemovesTheDearestUncoveredSymbols() {
    Piece target = Piece.target("t", ComponentType.WONDER, Cost.of(0, WOOD, CLAY, CLAY));
    Piece surcharge = Piece.holding("o", Production.fixed(CLAY, 1, true));

    Quote q = quote(target, List.of(masonry(1, ComponentType.WONDER)), List.of(surcharge));

    // clay 3, clay 3, wood 2 -> one clay removed
    assertThat(q.bought()).containsEntry(CLAY, 1).containsEntry(WOOD, 1);
    assertThat(q.total()).isEqualTo(5);
  }

  @Test
  void aSymbolAlreadyCoveredByProductionIsLeftAlone() {
    Piece target = Piece.target("t", ComponentType.WONDER, Cost.of(0, WOOD, GLASS));
    Piece wood = Piece.holding("w", Production.fixed(WOOD, 1, false));

    Quote q = quote(target, List.of(masonry(1, ComponentType.WONDER), wood), List.of());

    assertThat(q.total()).isZero();
  }

  @Test
  void reductionAndChoiceProducersAreSearchedTogether() {
    Piece target = Piece.target("t", ComponentType.WONDER, Cost.of(0, WOOD, STONE, GLASS));
    Piece choice = Piece.holding("c", Production.choice(false, WOOD, STONE));

    Quote q = quote(target, List.of(masonry(1, ComponentType.WONDER), choice), List.of());

    assertThat(q.total()).isEqualTo(2);
  }

  @Test
  void reductionsAddUp() {
    Piece target = Piece.target("t", ComponentType.WONDER, Cost.of(0, WOOD, STONE, GLASS));

    Quote q =
        quote(
            target,
            List.of(
                masonry("red1", 1, ComponentType.WONDER), masonry("red2", 1, ComponentType.WONDER)),
            List.of());

    assertThat(q.total()).isEqualTo(2);
  }

  @Test
  void aReductionBiggerThanTheCostIsHarmless() {
    Piece target = Piece.target("t", ComponentType.WONDER, Cost.of(1, WOOD));

    Quote q = quote(target, List.of(masonry(5, ComponentType.WONDER)), List.of());

    assertThat(q.bought()).isEmpty();
    assertThat(q.total()).isEqualTo(1);
  }

  @Test
  void aReductionForAnotherTypeDoesNotApply() {
    Piece target = Piece.target("t", ComponentType.CARD, Cost.of(0, WOOD));

    assertThat(quote(target, List.of(masonry(1, ComponentType.WONDER)), List.of()).total())
        .isEqualTo(2);
  }

  @Test
  void aTargetWithNoResourceCostIsUnaffected() {
    Piece target = Piece.target("t", ComponentType.WONDER, Cost.coins(4));

    Quote q = quote(target, List.of(masonry(2, ComponentType.WONDER)), List.of());

    assertThat(q.bought()).isEqualTo(Map.of());
    assertThat(q.total()).isEqualTo(4);
  }
}
