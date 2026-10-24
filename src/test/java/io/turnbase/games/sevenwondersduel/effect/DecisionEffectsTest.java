package io.turnbase.games.sevenwondersduel.effect;

import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P1;
import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P2;
import static org.assertj.core.api.Assertions.assertThat;

import io.turnbase.games.sevenwondersduel.agenda.ComponentOption;
import io.turnbase.games.sevenwondersduel.agenda.DecisionStep;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.catalog.Tag;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.zone.Location;
import io.turnbase.games.sevenwondersduel.zone.Placements;
import io.turnbase.games.sevenwondersduel.zone.Spots;
import java.util.List;
import org.junit.jupiter.api.Test;

class DecisionEffectsTest {

  private static final Tag GREY = Tag.color("grey");
  private static final Tag BROWN = Tag.color("brown");

  private static final Piece SOURCE = Piece.of("source");
  private static final Piece MINE_GREY = Piece.of("mine-grey", GREY);
  private static final Piece THEIRS_GREY = Piece.of("theirs-grey", GREY);
  private static final Piece THEIRS_GREY_2 = Piece.of("theirs-grey-2", GREY);
  private static final Piece THEIRS_BROWN = Piece.of("theirs-brown", BROWN);
  private static final Piece GAIN = Piece.of("gain").with(new GainCoins(4));

  private static final RuleSet RULES =
      Fixtures.rules(
          SOURCE.with(new DiscardOpponentCard(GREY), new BuildFromDiscard()),
          MINE_GREY,
          THEIRS_GREY,
          THEIRS_GREY_2,
          THEIRS_BROWN,
          GAIN);

  private static List<ComponentId> optionsOf(Resolution r) {
    DecisionStep step = (DecisionStep) r.state().agenda().top();
    return step.options().stream().map(o -> ((ComponentOption) o).component()).toList();
  }

  private static Resolution start(Object... placed) {
    Resolution r = new Resolution(Fixtures.state(RULES, P1, 7, 7, placed), RULES);
    r.queueEffects(SOURCE.id(), P1);
    return r;
  }

  private static Location where(Resolution r, Piece piece) {
    return Placements.locationOf(r.state().placements(), piece.id());
  }

  @Test
  void discardOffersOnlyTheOpponentsCardsOfThatColour() {
    Resolution r =
        start(
            MINE_GREY.id(), Fixtures.city(P1),
            THEIRS_GREY.id(), Fixtures.city(P2),
            THEIRS_GREY_2.id(), Fixtures.city(P2),
            THEIRS_BROWN.id(), Fixtures.city(P2));
    r.drain();

    assertThat(optionsOf(r)).containsExactlyInAnyOrder(THEIRS_GREY.id(), THEIRS_GREY_2.id());
    assertThat(((DecisionStep) r.state().agenda().top()).decider()).isEqualTo(P1);
  }

  @Test
  void discardMovesTheChosenCardToTheDiscardPile() {
    Resolution r =
        start(THEIRS_GREY.id(), Fixtures.city(P2), THEIRS_GREY_2.id(), Fixtures.city(P2));
    r.drain();

    r.resume(new ComponentOption(THEIRS_GREY_2.id()));

    assertThat(where(r, THEIRS_GREY_2)).isEqualTo(Spots.DISCARD);
    assertThat(where(r, THEIRS_GREY)).isEqualTo(Fixtures.city(P2));
  }

  @Test
  void discardWithNoTargetAsksNothingAndMovesNothing() {
    Resolution r = start(MINE_GREY.id(), Fixtures.city(P1), THEIRS_BROWN.id(), Fixtures.city(P2));
    r.drain();

    assertThat(r.state().agenda().isEmpty() || !(r.state().agenda().top() instanceof DecisionStep))
        .isTrue();
    assertThat(where(r, MINE_GREY)).isEqualTo(Fixtures.city(P1));
    assertThat(where(r, THEIRS_BROWN)).isEqualTo(Fixtures.city(P2));
  }

  @Test
  void buildFromDiscardOffersTheWholePileAndBuildsFreeWithItsEffects() {
    Resolution r = start(GAIN.id(), Spots.DISCARD, THEIRS_BROWN.id(), Spots.DISCARD);
    r.drain();

    assertThat(optionsOf(r)).containsExactlyInAnyOrder(GAIN.id(), THEIRS_BROWN.id());
    r.resume(new ComponentOption(GAIN.id()));
    r.drain();

    assertThat(where(r, GAIN)).isEqualTo(Fixtures.city(P1));
    assertThat(r.state().player(P1).coins()).isEqualTo(11);
    assertThat(where(r, THEIRS_BROWN)).isEqualTo(Spots.DISCARD);
  }

  @Test
  void buildFromAnEmptyDiscardAsksNothing() {
    Resolution r = start();
    r.drain();

    assertThat(r.state().agenda().isEmpty()).isTrue();
  }
}
