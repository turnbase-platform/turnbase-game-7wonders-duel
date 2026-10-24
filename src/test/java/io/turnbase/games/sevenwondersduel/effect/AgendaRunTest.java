package io.turnbase.games.sevenwondersduel.effect;

import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P1;
import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.turnbase.games.sevenwondersduel.agenda.Agenda;
import io.turnbase.games.sevenwondersduel.agenda.ComponentOption;
import io.turnbase.games.sevenwondersduel.agenda.DecisionStep;
import io.turnbase.games.sevenwondersduel.agenda.EffectStep;
import io.turnbase.games.sevenwondersduel.agenda.Option;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.effect.Fixtures.Card;
import io.turnbase.games.sevenwondersduel.event.CoinsChanged;
import io.turnbase.games.sevenwondersduel.event.EffectRef;
import io.turnbase.games.sevenwondersduel.event.EventRecord;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.zone.Placements;
import java.util.List;
import org.junit.jupiter.api.Test;

class AgendaRunTest {

  private static final Card T1 = Card.of("t1");
  private static final Card T2 = Card.of("t2");

  private static Option option(Card card) {
    return new ComponentOption(card.id());
  }

  /** A resolution with {@code source} in P1's city and the given tokens on the board. */
  private static Resolution resolution(Card source, Card... onBoard) {
    Card[] cards = new Card[onBoard.length + 1];
    Object[] placed = new Object[(onBoard.length + 1) * 2];
    cards[0] = source;
    placed[0] = source.id();
    placed[1] = Fixtures.city(P1);
    for (int i = 0; i < onBoard.length; i++) {
      cards[i + 1] = onBoard[i];
      placed[2 + 2 * i] = onBoard[i].id();
      placed[3 + 2 * i] = Fixtures.zone("board");
    }
    RuleSet rules = Fixtures.rules(cards);
    return new Resolution(Fixtures.state(rules, P1, 7, 7, placed), rules);
  }

  @Test
  void effectsRunInPrintedOrderThroughTheAgenda() {
    Card source = Card.of("s").withEffects(new GainCoins(1), new GainCoins(2));
    Resolution r = resolution(source);

    r.resolveEffects(source.id(), P1);

    assertThat(r.events())
        .extracting(EventRecord::event)
        .containsExactly(new CoinsChanged(P1, 1), new CoinsChanged(P1, 2));
    assertThat(r.state().agenda().isEmpty()).isTrue();
  }

  @Test
  void decisionPausesRemainingEffects() {
    Card source = Card.of("s").withEffects(new Fixtures.Choose(), new GainCoins(5));
    Resolution r = resolution(source, T1, T2);

    r.resolveEffects(source.id(), P1);

    assertThat(r.state().player(P1).coins()).isEqualTo(7);
    assertThat(r.state().agenda().steps())
        .containsExactly(
            new DecisionStep(P1, new EffectRef(source.id(), 0), List.of(option(T1), option(T2))),
            new EffectStep(new EffectRef(source.id(), 1), P1));
  }

  @Test
  void resumeAppliesAnswerThenRemainingEffectsFollow() {
    Card source = Card.of("s").withEffects(new Fixtures.Choose(), new GainCoins(5));
    Resolution r = resolution(source, T1, T2);
    r.resolveEffects(source.id(), P1);

    r.resume(option(T2));
    r.drain();

    assertThat(Placements.locationOf(r.state().placements(), T2.id())).isEqualTo(Fixtures.city(P1));
    assertThat(r.state().player(P1).coins()).isEqualTo(12);
    assertThat(r.events())
        .extracting(EventRecord::source)
        .extracting(s -> s.orElseThrow())
        .containsExactly(new EffectRef(source.id(), 0), new EffectRef(source.id(), 1));
    assertThat(r.state().agenda().isEmpty()).isTrue();
  }

  @Test
  void noFeasibleOptionOpensNoDecisionAndNextEffectRuns() {
    Card source = Card.of("s").withEffects(new Fixtures.Choose(), new GainCoins(5));
    Resolution r = resolution(source);

    r.resolveEffects(source.id(), P1);

    assertThat(r.state().agenda().isEmpty()).isTrue();
    assertThat(r.state().player(P1).coins()).isEqualTo(12);
  }

  @Test
  void stagedDecisionsAreAnsweredInCallOrder() {
    Effect both =
        new Effect() {
          @Override
          public void apply(Resolution resolution, PlayerId owner) {
            resolution.decide(P2, List.of(option(T1)));
            resolution.decide(P1, List.of(option(T2)));
          }
        };
    Card source = Card.of("s").withEffects(both);
    Resolution r = resolution(source, T1, T2);

    r.resolveEffects(source.id(), P1);

    assertThat(r.state().agenda().steps())
        .extracting(s -> ((DecisionStep) s).owner())
        .containsExactly(P2, P1);
  }

  @Test
  void effectWithoutResumeRejectsAnAnswer() {
    Card source = Card.of("s").withEffects(new GainCoins(1));
    Resolution r = resolution(source, T1);
    r.queue(List.of(new DecisionStep(P1, new EffectRef(source.id(), 0), List.of(option(T1)))));

    assertThatThrownBy(() -> r.resume(option(T1)))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("takes no answer");
  }

  @Test
  void staleReferenceFailsClearly() {
    Card source = Card.of("s").withEffects(new GainCoins(1));
    Resolution unknown = resolution(source);
    unknown.queue(List.of(new EffectStep(new EffectRef(ComponentId.of("gone"), 0), P1)));
    Resolution pastEnd = resolution(source);
    pastEnd.queue(List.of(new EffectStep(new EffectRef(source.id(), 3), P1)));

    assertThatThrownBy(unknown::drain)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("gone");
    assertThatThrownBy(pastEnd::drain)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("s#3");
  }

  @Test
  void drainWhileDrainingFails() {
    Effect reenter =
        new Effect() {
          @Override
          public void apply(Resolution resolution, PlayerId owner) {
            resolution.drain();
          }
        };
    Card source = Card.of("s").withEffects(reenter);
    Resolution r = resolution(source);

    assertThatThrownBy(() -> r.resolveEffects(source.id(), P1))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("already draining");
  }

  @Test
  void triggerCannotDecide() {
    Trigger<CoinsChanged> greedy =
        new Trigger<>() {
          @Override
          public Class<CoinsChanged> eventType() {
            return CoinsChanged.class;
          }

          @Override
          public void react(CoinsChanged event, PlayerId owner, Resolution resolution) {
            resolution.decide(owner, List.of(option(T1)));
          }
        };
    Card source = Card.of("s").withTriggers(greedy);
    Resolution r = resolution(source, T1);

    assertThatThrownBy(() -> r.changeCoins(P1, 1))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("triggers cannot");
    assertThat(r.state().agenda()).isEqualTo(Agenda.EMPTY);
  }
}
