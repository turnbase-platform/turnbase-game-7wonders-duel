package io.turnbase.games.sevenwondersduel.view;

import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P1;
import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P2;
import static io.turnbase.games.sevenwondersduel.effect.Fixtures.city;
import static io.turnbase.games.sevenwondersduel.effect.Fixtures.zone;
import static org.assertj.core.api.Assertions.assertThat;

import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.effect.Fixtures;
import io.turnbase.games.sevenwondersduel.effect.Fixtures.Card;
import io.turnbase.games.sevenwondersduel.effect.GainCoins;
import io.turnbase.games.sevenwondersduel.effect.Resolution;
import io.turnbase.games.sevenwondersduel.state.Pawn;
import java.util.List;
import org.junit.jupiter.api.Test;

class EventViewsTest {

  private static final Viewer ONE = new Viewer.Seat(P1);
  private static final Viewer TWO = new Viewer.Seat(P2);
  private static final Viewer SPECTATOR = new Viewer.Spectator();

  private static final Card MOVED = Card.of("moved");
  private static final Card GIFT = Card.of("gift").withEffects(new GainCoins(2));

  private static final RuleSet RULES = Fixtures.rules(MOVED, GIFT);

  private static Resolution resolution(Object... placed) {
    return new Resolution(Fixtures.state(RULES, P1, 7, 7, placed), RULES);
  }

  private static List<EventView> shown(Resolution r, Viewer viewer) {
    return EventViews.of(r.events(), r.state(), RULES, viewer);
  }

  @Test
  void aMoveIntoAPublicZoneNamesTheComponentToEveryone() {
    Resolution r = resolution(MOVED.id(), zone("deck"));

    r.move(MOVED.id(), zone("discard"));

    for (Viewer viewer : List.of(ONE, TWO, SPECTATOR)) {
      assertThat(shown(r, viewer))
          .containsExactly(
              new EventView(
                  0,
                  -1,
                  "moved",
                  null,
                  MOVED.id().value(),
                  new PlaceView("deck", ""),
                  new PlaceView("discard", ""),
                  null,
                  0,
                  null,
                  null));
    }
  }

  @Test
  void aMoveBetweenHiddenZonesNamesNothingButKeepsThePlaces() {
    Resolution r = resolution(MOVED.id(), zone("deck"));

    r.move(MOVED.id(), zone("structure.down"));

    for (Viewer viewer : List.of(ONE, TWO, SPECTATOR)) {
      EventView event = shown(r, viewer).get(0);
      assertThat(event.component()).isNull();
      assertThat(event.from()).isEqualTo(new PlaceView("deck", ""));
      assertThat(event.to()).isEqualTo(new PlaceView("structure.down", ""));
    }
  }

  @Test
  void aSeatKeepsNamingWhatItHasSeenWhenItIsHiddenAgain() {
    Resolution r = resolution(MOVED.id(), zone("discard"));
    r.revealTo(P1, MOVED.id());

    r.move(MOVED.id(), zone("box"));

    assertThat(shown(r, ONE).get(0).component()).isEqualTo(MOVED.id().value());
    assertThat(shown(r, TWO).get(0).component()).isNull();
    assertThat(shown(r, SPECTATOR).get(0).component()).isNull();
  }

  @Test
  void aTreasuryChangeNamesThePlayerAndTheDeltaAndItsCause() {
    Resolution r = resolution(GIFT.id(), city(P1));

    r.resolveEffects(GIFT.id(), P1);

    assertThat(shown(r, SPECTATOR))
        .containsExactly(
            new EventView(
                0, -1, "coins", GIFT.id().value(), null, null, null, P1.value(), 2, null, null));
  }

  @Test
  void aTradePaymentIsAKindOfItsOwn() {
    Resolution r = resolution();

    r.payTrade(P2, 3);

    assertThat(shown(r, ONE).stream().map(EventView::kind)).containsExactly("coins", "trade");
    assertThat(shown(r, ONE).get(1).amount()).isEqualTo(3);
    assertThat(shown(r, ONE).get(1).player()).isEqualTo(P2.value());
  }

  @Test
  void aChainBuildNamesTheBuilderAndTheCard() {
    Resolution r = resolution(MOVED.id(), city(P1));

    r.chainBuilt(P1, MOVED.id());

    EventView event = shown(r, TWO).get(0);
    assertThat(event.kind()).isEqualTo("chain");
    assertThat(event.player()).isEqualTo(P1.value());
    assertThat(event.component()).isEqualTo(MOVED.id().value());
  }

  @Test
  void aPawnMoveShowsBothPositions() {
    Resolution r = resolution();

    r.setPawn(new Pawn(P1, 2));

    EventView event = shown(r, SPECTATOR).get(0);
    assertThat(event.kind()).isEqualTo("pawn");
    assertThat(event.pawnFrom()).isEqualTo(new PawnView(null, 0));
    assertThat(event.pawnTo()).isEqualTo(new PawnView(P1.value(), 2));
  }

  @Test
  void thePlayAgainFlagHasOneKindPerDirection() {
    Resolution r = resolution();

    r.setPlayAgain(P1, true);
    r.setPlayAgain(P1, false);

    assertThat(shown(r, ONE).stream().map(EventView::kind))
        .containsExactly("playAgainGranted", "playAgainCleared");
  }
}
