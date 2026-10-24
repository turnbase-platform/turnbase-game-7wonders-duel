package io.turnbase.games.sevenwondersduel.effect;

import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P1;
import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.effect.Fixtures.Card;
import io.turnbase.games.sevenwondersduel.event.CoinsChanged;
import io.turnbase.games.sevenwondersduel.event.ComponentMoved;
import io.turnbase.games.sevenwondersduel.event.EffectRef;
import io.turnbase.games.sevenwondersduel.event.EventRecord;
import io.turnbase.games.sevenwondersduel.event.GameEvent;
import io.turnbase.games.sevenwondersduel.event.PlayAgainChanged;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class TriggerBusTest {

  interface Reaction<E extends GameEvent> {
    void run(E event, PlayerId owner, Resolution resolution);
  }

  private static <E extends GameEvent> Trigger<E> on(Class<E> type, Reaction<E> reaction) {
    return new Trigger<>() {
      @Override
      public Class<E> eventType() {
        return type;
      }

      @Override
      public void react(E event, PlayerId owner, Resolution resolution) {
        reaction.run(event, owner, resolution);
      }
    };
  }

  private static Trigger<CoinsChanged> noting(List<String> log, String name) {
    return on(CoinsChanged.class, (e, owner, r) -> log.add(name));
  }

  private static Resolution resolution(PlayerId active, Card[] cards, Object... placed) {
    RuleSet rules = Fixtures.rules(cards);
    return new Resolution(Fixtures.state(rules, active, 7, 7, placed), rules);
  }

  @Test
  void liveTriggerReactsToMatchingEventOnly() {
    List<String> log = new ArrayList<>();
    Card watcher = Card.of("w").withTriggers(noting(log, "w"));
    Card other = Card.of("o");
    Resolution r =
        resolution(
            P1,
            new Card[] {watcher, other},
            watcher.id(),
            Fixtures.city(P1),
            other.id(),
            Fixtures.zone("deck"));

    r.move(other.id(), Fixtures.zone("discard"));
    assertThat(log).isEmpty();

    r.changeCoins(P1, 1);
    assertThat(log).containsExactly("w");
  }

  @Test
  void triggerInNonLiveZoneNeverReacts() {
    List<String> log = new ArrayList<>();
    Card watcher = Card.of("w").withTriggers(noting(log, "w"));
    Resolution r = resolution(P1, new Card[] {watcher}, watcher.id(), Fixtures.zone("discard"));

    r.changeCoins(P1, 1);

    assertThat(log).isEmpty();
  }

  @Test
  void activePlayersTriggersRunBeforeOpponents() {
    for (PlayerId active : List.of(P1, P2)) {
      List<String> log = new ArrayList<>();
      Card mine = Card.of("p1card").withTriggers(noting(log, "p1"));
      Card theirs = Card.of("p2card").withTriggers(noting(log, "p2"));
      Resolution r =
          resolution(
              active,
              new Card[] {mine, theirs},
              mine.id(),
              Fixtures.city(P1),
              theirs.id(),
              Fixtures.city(P2));

      r.changeCoins(P1, 1);

      assertThat(log)
          .containsExactlyElementsOf(active.equals(P1) ? List.of("p1", "p2") : List.of("p2", "p1"));
    }
  }

  @Test
  void reactionEventsAreChildrenOfTheTriggeringEventWithTriggerSource() {
    Card tax =
        Card.of("tax")
            .withTriggers(
                on(
                    CoinsChanged.class,
                    (e, owner, r) -> {
                      if (e.player().equals(owner) && e.delta() > 0) {
                        r.changeCoins(P2, -1);
                      }
                    }));
    Resolution r = resolution(P1, new Card[] {tax}, tax.id(), Fixtures.city(P1));

    r.changeCoins(P1, 1);

    assertThat(r.events())
        .containsExactly(
            new EventRecord(0, -1, Optional.empty(), new CoinsChanged(P1, 1)),
            new EventRecord(
                1, 0, Optional.of(new EffectRef(tax.id(), 0)), new CoinsChanged(P2, -1)));
  }

  @Test
  void chainedTriggersFormATreeDepthFirst() {
    Card b =
        Card.of("b")
            .withTriggers(
                on(
                    CoinsChanged.class,
                    (e, owner, r) -> {
                      if (e.delta() == 1) {
                        r.setPlayAgain(P1, true);
                      }
                    }));
    Card a =
        Card.of("a")
            .withTriggers(
                on(
                    CoinsChanged.class,
                    (e, owner, r) -> {
                      if (e.delta() == 2) {
                        r.changeCoins(P1, 1);
                      }
                    }),
                on(
                    CoinsChanged.class,
                    (e, owner, r) -> {
                      if (e.delta() == 2) {
                        r.changeCoins(P1, 10);
                      }
                    }));
    Resolution r =
        resolution(P1, new Card[] {a, b}, a.id(), Fixtures.city(P1), b.id(), Fixtures.city(P1));

    r.changeCoins(P1, 2);

    assertThat(r.events())
        .containsExactly(
            new EventRecord(0, -1, Optional.empty(), new CoinsChanged(P1, 2)),
            new EventRecord(1, 0, Optional.of(new EffectRef(a.id(), 0)), new CoinsChanged(P1, 1)),
            new EventRecord(
                2, 1, Optional.of(new EffectRef(b.id(), 0)), new PlayAgainChanged(P1, true)),
            new EventRecord(3, 0, Optional.of(new EffectRef(a.id(), 1)), new CoinsChanged(P1, 10)));
  }

  @Test
  void triggerLeavingPlayMidDispatchDoesNotReact() {
    List<String> log = new ArrayList<>();
    ComponentId victim = ComponentId.of("victim");
    Card remover =
        Card.of("remover")
            .withTriggers(
                on(CoinsChanged.class, (e, owner, r) -> r.move(victim, Fixtures.zone("discard"))));
    Card second = new Card(victim, List.of(), List.of(), List.of(noting(log, "victim")));
    Resolution r =
        resolution(
            P1,
            new Card[] {remover, second},
            remover.id(),
            Fixtures.city(P1),
            victim,
            Fixtures.city(P1));

    r.changeCoins(P1, 1);

    assertThat(log).isEmpty();
    assertThat(r.events())
        .extracting(EventRecord::event)
        .anyMatch(ComponentMoved.class::isInstance);
  }

  @Test
  void componentEnteringPlayMidDispatchDoesNotSeeTheEventThatPrecededIt() {
    List<String> log = new ArrayList<>();
    ComponentId newcomer = ComponentId.of("newcomer");
    Card mover =
        Card.of("mover")
            .withTriggers(
                on(CoinsChanged.class, (e, owner, r) -> r.move(newcomer, Fixtures.city(P2))));
    Card late = new Card(newcomer, List.of(), List.of(), List.of(noting(log, "newcomer")));
    Resolution r =
        resolution(
            P1,
            new Card[] {mover, late},
            mover.id(),
            Fixtures.city(P1),
            newcomer,
            Fixtures.zone("discard"));

    r.changeCoins(P1, 1);

    assertThat(log).isEmpty();
  }

  @Test
  void selfRetriggeringTriggerStopsWithClearError() {
    Card loop =
        Card.of("loop").withTriggers(on(CoinsChanged.class, (e, owner, r) -> r.changeCoins(P1, 1)));
    Resolution r = resolution(P1, new Card[] {loop}, loop.id(), Fixtures.city(P1));

    assertThatThrownBy(() -> r.changeCoins(P1, 1))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("too deep");
  }

  @Test
  void endToEndUrbanismStyleCausationTree() {
    Card gift = Card.of("gift").withEffects(new GainCoins(2));
    Card tax =
        Card.of("tax")
            .withTriggers(
                on(
                    CoinsChanged.class,
                    (e, owner, r) -> {
                      if (e.player().equals(owner) && e.delta() > 0) {
                        r.changeCoins(P2, -1);
                      }
                    }));
    Resolution r =
        resolution(
            P1,
            new Card[] {gift, tax},
            gift.id(),
            Fixtures.zone("deck"),
            tax.id(),
            Fixtures.city(P1));

    r.move(gift.id(), Fixtures.city(P1));
    r.resolveEffects(gift.id(), P1);

    assertThat(r.events())
        .containsExactly(
            new EventRecord(
                0,
                -1,
                Optional.empty(),
                new ComponentMoved(gift.id(), Fixtures.zone("deck"), Fixtures.city(P1))),
            new EventRecord(
                1, -1, Optional.of(new EffectRef(gift.id(), 0)), new CoinsChanged(P1, 2)),
            new EventRecord(
                2, 1, Optional.of(new EffectRef(tax.id(), 0)), new CoinsChanged(P2, -1)));
    assertThat(r.state().player(P1).coins()).isEqualTo(9);
    assertThat(r.state().player(P2).coins()).isEqualTo(6);
  }
}
