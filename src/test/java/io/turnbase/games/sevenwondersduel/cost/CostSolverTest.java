package io.turnbase.games.sevenwondersduel.cost;

import static io.turnbase.games.sevenwondersduel.cost.Resource.CLAY;
import static io.turnbase.games.sevenwondersduel.cost.Resource.GLASS;
import static io.turnbase.games.sevenwondersduel.cost.Resource.PAPYRUS;
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

class CostSolverTest {

  record Piece(ComponentId id, Cost cost, List<Modifier> modifiers) implements Component {
    static Piece target(String id, Cost cost) {
      return new Piece(ComponentId.of(id), cost, List.of());
    }

    static Piece producing(String id, Modifier... modifiers) {
      return new Piece(ComponentId.of(id), Cost.FREE, List.of(modifiers));
    }

    @Override
    public String name() {
      return id.value();
    }

    @Override
    public ComponentType type() {
      return ComponentType.CARD;
    }
  }

  /** P1 holds {@code own}, P2 holds {@code theirs}; {@code target} waits in the deck. */
  private static Setup setup(Piece target, List<Piece> own, List<Piece> theirs) {
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
    return new Setup(
        new CostSolver(rules), Fixtures.state(rules, P1, 20, 20, placed.toArray()), target);
  }

  private record Setup(CostSolver solver, DuelState state, Piece target) {
    Quote quote() {
      return solver.quote(state, P1, target.id());
    }
  }

  private static Piece clay(String id, int n) {
    return Piece.producing(id, Production.fixed(CLAY, n, true));
  }

  @Test
  void rulebookExampleOpponentClayRaisesThePrice() {
    Setup s = setup(Piece.target("t", Cost.of(0, CLAY, PAPYRUS)), List.of(), List.of(clay("o", 1)));

    Quote q = s.quote();

    assertThat(q.bought()).containsEntry(CLAY, 1).containsEntry(PAPYRUS, 1);
    assertThat(q.tradeCost()).isEqualTo(3 + 2);
    assertThat(q.total()).isEqualTo(5);
  }

  @Test
  void rulebookExampleTwoOpponentStoneMakesThreeStoneCostTwelve() {
    Piece two = Piece.producing("o", Production.fixed(STONE, 2, true));
    Setup s = setup(Piece.target("t", Cost.of(0, STONE, STONE, STONE)), List.of(), List.of(two));

    assertThat(s.quote().total()).isEqualTo(12);
  }

  @Test
  void rulebookExampleCaravanseryStyleCostsSeven() {
    Piece glass = Piece.producing("o", Production.fixed(GLASS, 1, true));
    Setup s = setup(Piece.target("t", Cost.of(2, GLASS, PAPYRUS)), List.of(), List.of(glass));

    assertThat(s.quote().coins()).isEqualTo(2);
    assertThat(s.quote().total()).isEqualTo(2 + 3 + 2);
  }

  @Test
  void ownFixedProductionCoversSymbolsAndIsNotConsumed() {
    Setup s =
        setup(
            Piece.target("t", Cost.of(0, WOOD, WOOD)),
            List.of(Piece.producing("w", Production.fixed(WOOD, 2, false))),
            List.of());

    assertThat(s.quote().total()).isZero();
    assertThat(s.quote()).isEqualTo(s.quote());
  }

  @Test
  void ownProductionCoversPartAndTheRestIsBought() {
    Setup s =
        setup(
            Piece.target("t", Cost.of(0, WOOD, WOOD, WOOD)),
            List.of(Piece.producing("w", Production.fixed(WOOD, 1, false))),
            List.of());

    assertThat(s.quote().bought()).containsOnly(Map.entry(WOOD, 2));
    assertThat(s.quote().total()).isEqualTo(4);
  }

  @Test
  void aChoiceProducerCoversOneOfTwoNeedsNeverBoth() {
    Setup s =
        setup(
            Piece.target("t", Cost.of(0, WOOD, STONE)),
            List.of(Piece.producing("c", Production.choice(false, WOOD, STONE))),
            List.of());

    assertThat(s.quote().total()).isEqualTo(2);
  }

  @Test
  void twoChoiceProducersAreAssignedJointlyToMinimiseCost() {
    Piece wood = Piece.producing("c1", Production.choice(false, WOOD, STONE));
    Piece stone = Piece.producing("c2", Production.choice(false, STONE, CLAY));
    Setup s =
        setup(
            Piece.target("t", Cost.of(0, WOOD, STONE, CLAY)),
            List.of(wood, stone),
            List.of(clay("o", 3)));

    // clay costs 5 to buy, wood and stone 2: c1 must take wood, c2 must take clay -> pay stone 2
    assertThat(s.quote().total()).isEqualTo(2);
  }

  @Test
  void equallyPricedAssignmentsResolveByResourceIdNotSetOrder() {
    Setup s =
        setup(
            Piece.target("t", Cost.of(0, WOOD, CLAY, STONE)),
            List.of(Piece.producing("c", Production.choice(false, WOOD, CLAY, STONE))),
            List.of());

    // the unit covers the alphabetically first resource, so the others are bought
    assertThat(s.quote().bought()).containsOnly(Map.entry(STONE, 1), Map.entry(WOOD, 1));
  }

  @Test
  void opponentChoiceProducersAndUnflaggedProductionDoNotRaiseThePrice() {
    Piece choice = Piece.producing("o1", Production.choice(true, CLAY, STONE));
    Piece plain = Piece.producing("o2", Production.fixed(CLAY, 2, false));
    Setup s = setup(Piece.target("t", Cost.of(0, CLAY)), List.of(), List.of(choice, plain));

    assertThat(s.quote().total()).isEqualTo(2);
  }

  @Test
  void aOneCoinTradeCardSetsThePriceOnlyInTheCity() {
    PriceModifier cheap = PriceModifier.fixedAt(1, WOOD);
    Setup held =
        setup(
            Piece.target("t", Cost.of(0, WOOD, WOOD)),
            List.of(Piece.producing("m", cheap)),
            List.of());
    assertThat(held.quote().total()).isEqualTo(2);

    RuleSet rules = Fixtures.rules(held.target(), Piece.producing("m", cheap));
    DuelState inDeck =
        Fixtures.state(
            rules,
            P1,
            20,
            20,
            held.target().id(),
            Fixtures.zone("deck"),
            ComponentId.of("m"),
            Fixtures.zone("deck"));
    assertThat(new CostSolver(rules).quote(inDeck, P1, held.target().id()).total()).isEqualTo(4);
  }

  @Test
  void aOneCoinPriceIgnoresTheOpponentSurcharge() {
    Setup s =
        setup(
            Piece.target("t", Cost.of(0, CLAY)),
            List.of(Piece.producing("m", PriceModifier.fixedAt(1, CLAY))),
            List.of(clay("o", 2)));

    assertThat(s.quote().total()).isEqualTo(1);
  }

  @Test
  void payableWithComparesTotalToTreasury() {
    Quote q = new Quote(2, java.util.Map.of(WOOD, 1), 2, false);

    assertThat(q.total()).isEqualTo(4);
    assertThat(q.payableWith(4)).isTrue();
    assertThat(q.payableWith(3)).isFalse();
  }

  @Test
  void aResourceNobodyProducesIsStillQuotable() {
    Setup s = setup(Piece.target("t", Cost.of(0, PAPYRUS)), List.of(), List.of());

    assertThat(s.quote().total()).isEqualTo(2);
    assertThat(s.solver().unitPrice(s.state(), P1, PAPYRUS)).isEqualTo(2);
  }
}
