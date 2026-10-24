package io.turnbase.games.sevenwondersduel.effect;

import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P1;
import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.effect.Fixtures.Card;
import io.turnbase.games.sevenwondersduel.event.CoinsChanged;
import io.turnbase.games.sevenwondersduel.event.EffectRef;
import io.turnbase.games.sevenwondersduel.event.EventRecord;
import io.turnbase.games.sevenwondersduel.event.PlayAgainChanged;
import java.util.List;
import org.junit.jupiter.api.Test;

class InstantEffectsTest {

  private static Resolution resolve(Card card, int coins1, int coins2) {
    RuleSet rules = Fixtures.rules(card);
    Resolution r =
        new Resolution(
            Fixtures.state(rules, P1, coins1, coins2, card.id(), Fixtures.city(P1)), rules);
    r.resolveEffects(card.id(), P1);
    return r;
  }

  @Test
  void gainCoinsAddsToOwner() {
    Resolution r = resolve(Card.of("c").withEffects(new GainCoins(3)), 7, 7);

    assertThat(r.state().player(P1).coins()).isEqualTo(10);
    assertThat(r.events()).extracting(EventRecord::event).containsExactly(new CoinsChanged(P1, 3));
  }

  @Test
  void loseCoinsTakesAtMostTheOpponentsTreasuryToTheBank() {
    Resolution r = resolve(Card.of("c").withEffects(new LoseCoins(5)), 7, 2);

    assertThat(r.state().player(P2).coins()).isZero();
    assertThat(r.state().player(P1).coins()).isEqualTo(7);
    assertThat(r.events()).extracting(EventRecord::event).containsExactly(new CoinsChanged(P2, -2));
  }

  @Test
  void takeCoinsMovesExactlyWhatTheVictimHad() {
    Resolution r = resolve(Card.of("c").withEffects(new TakeCoins(5)), 7, 3);

    assertThat(r.state().player(P1).coins()).isEqualTo(10);
    assertThat(r.state().player(P2).coins()).isZero();
    assertThat(r.events())
        .extracting(EventRecord::event)
        .containsExactly(new CoinsChanged(P2, -3), new CoinsChanged(P1, 3));
  }

  @Test
  void takeCoinsFromEmptyTreasuryDoesNothingAndEmitsNothing() {
    Resolution r = resolve(Card.of("c").withEffects(new TakeCoins(5)), 7, 0);

    assertThat(r.state().player(P1).coins()).isEqualTo(7);
    assertThat(r.events()).isEmpty();
  }

  @Test
  void grantPlayAgainSetsTheOwnersFlagOnly() {
    Resolution r = resolve(Card.of("c").withEffects(new GrantPlayAgain()), 7, 7);

    assertThat(r.state().player(P1).playAgain()).isTrue();
    assertThat(r.state().player(P2).playAgain()).isFalse();
    assertThat(r.events())
        .extracting(EventRecord::event)
        .containsExactly(new PlayAgainChanged(P1, true));
  }

  @Test
  void effectsResolveInPrintedOrderWithIndexedSources() {
    Card card = Card.of("c").withEffects(new GainCoins(2), new GainCoins(3));

    Resolution r = resolve(card, 7, 7);

    assertThat(r.events())
        .extracting(EventRecord::source)
        .containsExactly(
            java.util.Optional.of(new EffectRef(card.id(), 0)),
            java.util.Optional.of(new EffectRef(card.id(), 1)));
    assertThat(r.events())
        .extracting(EventRecord::event)
        .containsExactly(new CoinsChanged(P1, 2), new CoinsChanged(P1, 3));
  }

  @Test
  void instantResultsSurviveTheSourceBeingDestroyed() {
    Card card = Card.of("c").withEffects(new GainCoins(3));
    Resolution r = resolve(card, 7, 7);

    r.move(card.id(), Fixtures.zone("destroyed"));

    assertThat(r.state().player(P1).coins()).isEqualTo(10);
  }

  @Test
  void nonPositiveAmountsAreRejected() {
    for (int bad : List.of(0, -1)) {
      assertThatThrownBy(() -> new GainCoins(bad)).isInstanceOf(IllegalArgumentException.class);
      assertThatThrownBy(() -> new LoseCoins(bad)).isInstanceOf(IllegalArgumentException.class);
      assertThatThrownBy(() -> new TakeCoins(bad)).isInstanceOf(IllegalArgumentException.class);
    }
  }
}
