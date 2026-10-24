package io.turnbase.games.sevenwondersduel.structure;

import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.effect.Fixtures;
import io.turnbase.games.sevenwondersduel.effect.Fixtures.Card;
import io.turnbase.games.sevenwondersduel.effect.Resolution;
import io.turnbase.games.sevenwondersduel.event.ComponentMoved;
import io.turnbase.games.sevenwondersduel.event.EventRecord;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.LayoutId;
import io.turnbase.games.sevenwondersduel.id.ZoneId;
import io.turnbase.games.sevenwondersduel.zone.Location;
import io.turnbase.games.sevenwondersduel.zone.Placement;
import io.turnbase.games.sevenwondersduel.zone.Placements;
import io.turnbase.games.sevenwondersduel.zone.Zones;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class StructureTest {

  private static final Card[] CARDS =
      IntStream.range(0, 20).mapToObj(i -> Card.of("c" + i)).toArray(Card[]::new);
  private static final RuleSet RULES = Fixtures.rules(CARDS);
  private static final ZoneId UP = Zones.STRUCTURE_UP;
  private static final ZoneId DOWN = Zones.STRUCTURE_DOWN;

  private static Layout age(int n) {
    return RULES.layout(LayoutId.of("age" + n));
  }

  private static ComponentId card(int i) {
    return CARDS[i].id();
  }

  private static List<ComponentId> deckOrder() {
    return IntStream.range(0, 20).mapToObj(StructureTest::card).toList();
  }

  private static List<Placement> deck() {
    List<Placement> all = new ArrayList<>();
    for (ComponentId id : deckOrder()) {
      all = Placements.place(all, id, Fixtures.zone("deck"), RULES.zones());
    }
    return all;
  }

  private static List<Placement> dealt(int n) {
    return Structure.deal(deck(), age(n), deckOrder(), RULES.zones());
  }

  private static List<Placement> without(List<Placement> all, int... cards) {
    List<Placement> result = all;
    for (int i : cards) {
      result = Placements.move(result, card(i), Fixtures.zone("discard"), RULES.zones());
    }
    return result;
  }

  private static Resolution resolution(List<Placement> placements) {
    return new Resolution(Fixtures.state(RULES, P1, 7, 7).withPlacements(placements), RULES);
  }

  @Test
  void dealPutsFaceUpAndFaceDownCardsInTheirZones() {
    List<Placement> all = dealt(1);

    assertThat(Placements.locationOf(all, card(0))).isEqualTo(new Location(UP, "0:4"));
    assertThat(Placements.locationOf(all, card(2))).isEqualTo(new Location(DOWN, "1:3"));
    assertThat(Placements.idsIn(all, UP)).hasSize(12);
    assertThat(Placements.idsIn(all, DOWN)).hasSize(8);
  }

  @Test
  void dealRejectsWrongDeckSizeAndLeavesInputUntouched() {
    List<Placement> before = deck();

    assertThatThrownBy(
            () -> Structure.deal(before, age(1), deckOrder().subList(0, 19), RULES.zones()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("19");
    assertThat(before).isEqualTo(deck());
  }

  @Test
  void onlyTheBottomRowIsUncoveredAfterDeal() {
    int[] bottom = {6, 2, 2};
    for (int n = 1; n <= 3; n++) {
      List<Slot> uncovered = Structure.uncovered(dealt(n));

      assertThat(uncovered).hasSize(bottom[n - 1]);
      int last = age(n).rows().size() - 1;
      assertThat(uncovered).allMatch(s -> s.row() == last);
    }
  }

  @Test
  void cardIsUncoveredOnlyWhenBothBelowAreGone() {
    // Age I (3,1) is card 9, covered by (4,0)=14 and (4,2)=15.
    assertThat(Structure.uncovered(without(dealt(1), 14))).doesNotContain(new Slot(3, 1));
    assertThat(Structure.uncovered(without(dealt(1), 14, 15))).contains(new Slot(3, 1));
  }

  @Test
  void edgeSlotNeedsOnlyItsSingleNeighbour() {
    // Age II (1,1) is card 6; below it only (2,2)=11 exists, (2,0) does not.
    assertThat(Structure.uncovered(dealt(2))).doesNotContain(new Slot(1, 1));
    assertThat(Structure.uncovered(without(dealt(2), 11))).contains(new Slot(1, 1));
  }

  @Test
  void ageThreeGapCardsFollowTheirPairs() {
    List<Placement> afterRow5 = without(dealt(3), 18, 19, 15, 16);
    assertThat(Structure.uncovered(afterRow5)).contains(new Slot(4, 0), new Slot(4, 2));

    List<Placement> oneOfPair = without(afterRow5, 11);
    assertThat(Structure.uncovered(oneOfPair)).doesNotContain(new Slot(3, 1));

    assertThat(Structure.uncovered(without(oneOfPair, 12))).contains(new Slot(3, 1));
  }

  @Test
  void takeableExcludesFaceDownCards() {
    List<Placement> all = without(dealt(3), 18, 19);

    assertThat(Structure.uncovered(all)).hasSize(3);
    assertThat(Structure.takeable(all)).isEmpty();
    assertThat(Structure.takeable(dealt(3))).containsExactly(card(18), card(19));
  }

  @Test
  void revealTurnsUncoveredFaceDownCardsUpInRowOrder() {
    Resolution r = resolution(without(dealt(3), 18, 19));

    Structure.revealUncovered(r);

    assertThat(r.events())
        .extracting(EventRecord::event)
        .containsExactly(
            new ComponentMoved(card(15), new Location(DOWN, "5:1"), new Location(UP, "5:1")),
            new ComponentMoved(card(16), new Location(DOWN, "5:3"), new Location(UP, "5:3")),
            new ComponentMoved(card(17), new Location(DOWN, "5:5"), new Location(UP, "5:5")));
    assertThat(Structure.takeable(r.state().placements()))
        .containsExactly(card(15), card(16), card(17));
  }

  @Test
  void revealWithNothingToDoEmitsNothing() {
    Resolution r = resolution(dealt(1));

    Structure.revealUncovered(r);

    assertThat(r.events()).isEmpty();
  }

  @Test
  void revealLeavesCoveredFaceDownCardsAlone() {
    Resolution r = resolution(without(dealt(1), 14));

    Structure.revealUncovered(r);

    assertThat(r.events()).isEmpty();
    assertThat(Placements.locationOf(r.state().placements(), card(9)).zone()).isEqualTo(DOWN);
  }

  @Test
  void dealRejectsACardThatIsNotInTheDeck() {
    List<Placement> all = Placements.move(deck(), card(5), Fixtures.zone("discard"), RULES.zones());

    assertThatThrownBy(() -> Structure.deal(all, age(1), deckOrder(), RULES.zones()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("c5");
  }

  @Test
  void dealRejectsADuplicatedCard() {
    List<ComponentId> cards = new ArrayList<>(deckOrder());
    cards.set(1, card(0));
    List<Placement> before = deck();

    assertThatThrownBy(() -> Structure.deal(before, age(1), cards, RULES.zones()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("c0");
    assertThat(before).isEqualTo(deck());
  }

  @Test
  void dealRejectsANonEmptyStructure() {
    List<Placement> all = dealt(1);

    assertThatThrownBy(() -> Structure.deal(all, age(2), deckOrder(), RULES.zones()))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void aCardCannotBeLaidFaceDownWithoutABack() {
    Resolution r = resolution(deck());

    assertThatThrownBy(() -> Structure.deal(r, age(1), deckOrder()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("c0")
        .hasMessageContaining("no back");
  }
}
