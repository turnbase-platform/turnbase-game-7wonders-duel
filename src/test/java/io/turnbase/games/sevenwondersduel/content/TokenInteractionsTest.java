package io.turnbase.games.sevenwondersduel.content;

import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P1;
import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P2;
import static org.assertj.core.api.Assertions.assertThat;

import io.turnbase.games.sevenwondersduel.agenda.ComponentOption;
import io.turnbase.games.sevenwondersduel.agenda.DecisionStep;
import io.turnbase.games.sevenwondersduel.agenda.Option;
import io.turnbase.games.sevenwondersduel.catalog.BasePack;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.catalog.Tag;
import io.turnbase.games.sevenwondersduel.cost.CostSolver;
import io.turnbase.games.sevenwondersduel.effect.Fixtures;
import io.turnbase.games.sevenwondersduel.effect.Resolution;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.setup.Setup;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.zone.Placements;
import io.turnbase.games.sevenwondersduel.zone.Zones;
import java.util.List;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;

/** Progress tokens and Wonders of the real catalogue acting on each other (B-SCI-1, B-WND). */
class TokenInteractionsTest {

  private static final RuleSet RULES = RuleSet.assemble(List.of(new BasePack(), new BaseContent()));

  private static ComponentId id(String name) {
    return ComponentId.of(name);
  }

  private static List<Card> allCards() {
    List<Card> all = new java.util.ArrayList<>(AgeOne.cards());
    all.addAll(AgeTwo.cards());
    all.addAll(AgeThree.cards());
    return all;
  }

  /** What P1, holding {@code held} and facing nothing, pays in all for {@code target}. */
  private static int price(ComponentId target, ComponentId... held) {
    Object[] placed = new Object[(held.length + 1) * 2];
    for (int i = 0; i < held.length; i++) {
      placed[2 * i] = held[i];
      placed[2 * i + 1] = Fixtures.city(P1);
    }
    placed[placed.length - 2] = target;
    placed[placed.length - 1] = Fixtures.zone("deck");
    DuelState state = Fixtures.state(RULES, P1, 40, 40, placed);
    return new CostSolver(RULES).quote(state, P1, target).total();
  }

  @Test
  void architectureCheapensAWonderButNotACard() {
    ComponentId wonder = id("the-great-library");
    ComponentId card =
        allCards().stream()
            .filter(c -> c.tags().contains(Colors.BLUE) && price(c.id()) > 0)
            .findFirst()
            .orElseThrow()
            .id();

    assertThat(price(wonder, id("architecture"))).isLessThan(price(wonder));
    assertThat(price(card, id("architecture"))).isEqualTo(price(card));
  }

  @Test
  void masonryCheapensABlueCardButNotAWonder() {
    ComponentId wonder = id("the-great-library");
    ComponentId blue =
        allCards().stream()
            .filter(c -> c.tags().contains(Colors.BLUE) && price(c.id()) >= 4)
            .findFirst()
            .orElseThrow()
            .id();

    assertThat(price(blue, id("masonry"))).isLessThan(price(blue));
    assertThat(price(wonder, id("masonry"))).isEqualTo(price(wonder));
  }

  @Test
  void urbanismPaysNothingWhenTheMausoleumRebuildsADiscardedCard() {
    ComponentId card = AgeOne.cards().get(0).id();
    DuelState state =
        Fixtures.state(
            RULES,
            P1,
            7,
            7,
            id("urbanism"),
            Fixtures.city(P1),
            card,
            Fixtures.zone(Zones.DISCARD.value()));
    Resolution r = new Resolution(state, RULES);

    r.queueEffects(id("the-mausoleum"), P1);
    r.drain();
    r.resume(new ComponentOption(card));
    r.drain();

    assertThat(Placements.idsIn(r.state().placements(), Fixtures.city(P1))).contains(card);
    assertThat(r.state().player(P1).coins()).isEqualTo(7);
  }

  @Test
  void theGreatLibraryOffersOnlyTokensFromTheBox() {
    DuelState state = Setup.newGame(RULES, 3, P1, P2);
    List<ComponentId> board = Placements.idsIn(state.placements(), Zones.BOARD);
    Resolution r = new Resolution(state, RULES);

    r.queueEffects(id("the-great-library"), P1);
    r.drain();

    List<Option> offered = ((DecisionStep) r.state().agenda().top()).options();
    assertThat(offered).hasSize(3);
    Predicate<Option> inBox =
        o ->
            Placements.idsIn(state.placements(), Zones.BOX)
                    .contains(((ComponentOption) o).component())
                && RULES
                    .catalog()
                    .get(((ComponentOption) o).component())
                    .tags()
                    .contains(Tag.PROGRESS);
    assertThat(offered).allMatch(inBox);
    assertThat(offered).noneMatch(o -> board.contains(((ComponentOption) o).component()));
    assertThat(r.state().knowledge().get(P2))
        .doesNotContainAnyElementsOf(
            offered.stream().map(o -> ((ComponentOption) o).component()).toList());
  }
}
