package io.turnbase.games.sevenwondersduel.view;

import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P1;
import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P2;
import static org.assertj.core.api.Assertions.assertThat;

import io.turnbase.games.sevenwondersduel.agenda.ComponentOption;
import io.turnbase.games.sevenwondersduel.agenda.DecisionStep;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.effect.Resolution;
import io.turnbase.games.sevenwondersduel.event.EffectRef;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.ZoneId;
import io.turnbase.games.sevenwondersduel.setup.SetupFixtures;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.state.Pawn;
import io.turnbase.games.sevenwondersduel.state.Phase;
import io.turnbase.games.sevenwondersduel.state.Victory;
import io.turnbase.games.sevenwondersduel.structure.Structure;
import io.turnbase.games.sevenwondersduel.zone.Location;
import io.turnbase.games.sevenwondersduel.zone.Placement;
import io.turnbase.games.sevenwondersduel.zone.Placements;
import io.turnbase.games.sevenwondersduel.zone.Zones;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ViewsTest {

  private static final RuleSet RULES = SetupFixtures.baseSized();
  private static final DuelState STARTED = SetupFixtures.startedAgeOne(RULES, 7);
  private static final Viewer ONE = new Viewer.Seat(P1);
  private static final Viewer SPECTATOR = new Viewer.Spectator();

  private static List<ComponentId> in(DuelState state, ZoneId zone) {
    return Placements.idsIn(state.placements(), zone);
  }

  private static long hiddenIn(DuelView view, String zone) {
    return view.hidden().stream()
        .filter(h -> h.zone().equals(zone))
        .mapToLong(HiddenView::count)
        .sum();
  }

  @Test
  void theDecksAreCountsPerAgeWithNoIdsAndNoOrder() {
    DuelView view = Views.of(STARTED, RULES, ONE);

    assertThat(in(STARTED, Zones.DECK)).isNotEmpty();
    assertThat(hiddenIn(view, "deck")).isEqualTo(in(STARTED, Zones.DECK).size());
    assertThat(view.hidden().stream().filter(h -> h.zone().equals("deck")))
        .allMatch(h -> !h.key().isEmpty());
    assertThat(view.visible()).noneMatch(p -> p.zone().equals("deck"));
  }

  @Test
  void eachFaceDownSlotIsOneHiddenCard() {
    DuelView view = Views.of(STARTED, RULES, ONE);

    long down = in(STARTED, Structure.DOWN).size();
    assertThat(down).isPositive();
    assertThat(view.hidden().stream().filter(h -> h.zone().equals("structure.down")))
        .hasSize((int) down)
        .allMatch(h -> h.count() == 1);
    assertThat(view.visible()).noneMatch(p -> p.zone().equals("structure.down"));
  }

  @Test
  void theFaceUpStructureIsVisibleWithItsSlots() {
    DuelView view = Views.of(STARTED, RULES, SPECTATOR);

    assertThat(view.visible().stream().filter(p -> p.zone().equals("structure.up")))
        .hasSize(in(STARTED, Structure.UP).size());
  }

  @Test
  void swappingTwoHiddenCardsChangesNothingTheViewerCanSee() {
    List<ComponentId> deck = in(STARTED, Zones.DECK);
    List<Placement> swapped = new ArrayList<>();
    for (Placement p : STARTED.placements()) {
      if (p.component().equals(deck.get(0))) {
        swapped.add(new Placement(deck.get(1), p.at()));
      } else if (p.component().equals(deck.get(1))) {
        swapped.add(new Placement(deck.get(0), p.at()));
      } else {
        swapped.add(p);
      }
    }

    assertThat(Views.of(STARTED.withPlacements(swapped), RULES, ONE))
        .isEqualTo(Views.of(STARTED, RULES, ONE));
  }

  @Test
  void noHiddenIdAppearsAnywhereInAView() {
    for (Viewer viewer : List.of(ONE, new Viewer.Seat(P2), SPECTATOR)) {
      String text = Views.of(STARTED, RULES, viewer).toString();
      for (ZoneId zone : List.of(Zones.DECK, Structure.DOWN, Zones.BOX)) {
        for (ComponentId id : in(STARTED, zone)) {
          assertThat(text).as(zone.value()).doesNotContain("\"" + id.value() + "\"");
          assertThat(text).doesNotContain("component=" + id.value() + ",");
        }
      }
    }
  }

  @Test
  void aCardUnderAWonderIsShownToThePlayersWhoSawItButNotToSpectators() {
    ComponentId card = in(STARTED, Structure.UP).get(0);
    Resolution r = new Resolution(STARTED, RULES);
    r.move(card, new Location(Zones.UNDER_WONDER, "w"));
    DuelState tucked = r.state();

    assertThat(Views.of(tucked, RULES, ONE).visible())
        .contains(new PlacedView(card.value(), "underWonder", "w"));
    assertThat(Views.of(tucked, RULES, new Viewer.Seat(P2)).visible())
        .contains(new PlacedView(card.value(), "underWonder", "w"));
    DuelView watched = Views.of(tucked, RULES, SPECTATOR);
    assertThat(watched.visible()).noneMatch(p -> p.component().equals(card.value()));
    assertThat(hiddenIn(watched, "underWonder")).isEqualTo(1);
  }

  @Test
  void theHeaderCarriesPlayersPawnAndVictory() {
    DuelState over =
        STARTED
            .withPawn(new Pawn(P2, 3))
            .withPhase(Phase.OVER)
            .withVictory(new Victory(null, Victory.Reason.POINTS));

    DuelView view = Views.of(over, RULES, ONE);

    assertThat(view.phase()).isEqualTo("OVER");
    assertThat(view.pawnSide()).isEqualTo(P2.value());
    assertThat(view.pawnSpace()).isEqualTo(3);
    assertThat(view.victoryWinner()).isNull();
    assertThat(view.victoryReason()).isEqualTo("POINTS");
    assertThat(view.players()).extracting(PlayerView::coins).containsExactly(7, 7);
  }

  @Test
  void aPendingDecisionShowsItsDeciderAndOptions() {
    ComponentId token = in(STARTED, Zones.BOARD).get(0);
    DecisionStep step =
        new DecisionStep(P2, P1, new EffectRef(token, 0), List.of(new ComponentOption(token)));
    DuelState asking = STARTED.withAgenda(STARTED.agenda().pushAll(List.of(step)));

    PendingView pending = Views.of(asking, RULES, SPECTATOR).pending();

    assertThat(pending).isEqualTo(new PendingView(P2.value(), List.of(token.value()), 0));
    assertThat(Views.of(STARTED, RULES, ONE).pending()).isNull();
  }

  @Test
  void aSecretOptionIsOfferedOnlyToTheDecider() {
    ComponentId secret = in(STARTED, Zones.BOX).get(0);
    ComponentId token = in(STARTED, Zones.BOARD).get(0);
    DecisionStep step =
        new DecisionStep(
            P2,
            P1,
            new EffectRef(token, 0),
            List.of(new ComponentOption(secret), new ComponentOption(token)));
    DuelState asking = STARTED.withAgenda(STARTED.agenda().pushAll(List.of(step)));

    assertThat(Views.of(asking, RULES, new Viewer.Seat(P2)).pending())
        .isEqualTo(new PendingView(P2.value(), List.of(secret.value(), token.value()), 0));
    for (Viewer other : List.of(ONE, SPECTATOR)) {
      assertThat(Views.of(asking, RULES, other).pending())
          .isEqualTo(new PendingView(P2.value(), List.of(token.value()), 1));
    }
  }
}
