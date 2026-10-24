package io.turnbase.games.sevenwondersduel.engine;

import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P1;
import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.turnbase.games.sevenwondersduel.SevenWondersDuelState;
import io.turnbase.games.sevenwondersduel.agenda.ComponentOption;
import io.turnbase.games.sevenwondersduel.agenda.DecisionStep;
import io.turnbase.games.sevenwondersduel.agenda.Option;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.effect.Effect;
import io.turnbase.games.sevenwondersduel.effect.Fixtures;
import io.turnbase.games.sevenwondersduel.effect.Fixtures.Card;
import io.turnbase.games.sevenwondersduel.effect.GainCoins;
import io.turnbase.games.sevenwondersduel.effect.Resolution;
import io.turnbase.games.sevenwondersduel.event.EffectRef;
import io.turnbase.games.sevenwondersduel.event.EventRecord;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.snapshot.Snapshots;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.zone.Placements;
import io.turnbase.serialization.GdkSerialization;
import java.util.List;
import org.junit.jupiter.api.Test;

class EngineTest {

  private static final Card T1 = Card.of("t1");
  private static final Card T2 = Card.of("t2");
  private static final Card SOURCE =
      Card.of("s").withEffects(new Fixtures.Choose(), new GainCoins(5));

  private static final RuleSet RULES = Fixtures.rules(SOURCE, T1, T2);
  private static final Engine ENGINE = new Engine(RULES);

  private static Option pick(Card card) {
    return new ComponentOption(card.id());
  }

  /** P1 played {@code SOURCE}; the Choose effect is waiting for P1's pick of t1 or t2. */
  private static DuelState paused() {
    DuelState start =
        Fixtures.state(
            RULES,
            P1,
            7,
            7,
            SOURCE.id(),
            Fixtures.city(P1),
            T1.id(),
            Fixtures.zone("board"),
            T2.id(),
            Fixtures.zone("board"));
    Resolution r = new Resolution(start, RULES);
    r.resolveEffects(SOURCE.id(), P1);
    return r.state();
  }

  @Test
  void ownerAnswersAndTheSourceContinues() {
    Outcome outcome = ENGINE.apply(paused(), P1, new Answer(pick(T2)));

    assertThat(Placements.locationOf(outcome.state().placements(), T2.id()))
        .isEqualTo(Fixtures.city(P1));
    assertThat(outcome.state().player(P1).coins()).isEqualTo(12);
    assertThat(outcome.state().agenda().isEmpty()).isTrue();
    assertThat(outcome.events())
        .extracting(EventRecord::source)
        .extracting(s -> s.orElseThrow())
        .containsExactly(new EffectRef(SOURCE.id(), 0), new EffectRef(SOURCE.id(), 1));
  }

  @Test
  void opponentOwnedDecisionIsAnsweredByTheOpponent() {
    Effect askOpponent =
        new Effect() {
          @Override
          public void apply(Resolution resolution, PlayerId owner) {
            resolution.decide(P2, List.of(pick(T1)));
          }

          @Override
          public void resume(Resolution resolution, PlayerId owner, Option answer) {
            resolution.changeCoins(P2, 1);
          }
        };
    Card source = Card.of("steal").withEffects(askOpponent);
    RuleSet rules = Fixtures.rules(source, T1);
    Resolution r =
        new Resolution(
            Fixtures.state(
                rules, P1, 7, 7, source.id(), Fixtures.city(P1), T1.id(), Fixtures.zone("board")),
            rules);
    r.resolveEffects(source.id(), P1);
    Engine engine = new Engine(rules);

    assertThatThrownBy(() -> engine.apply(r.state(), P1, new Answer(pick(T1))))
        .isInstanceOf(IllegalActionException.class)
        .hasMessageContaining("not your decision");
    Outcome outcome = engine.apply(r.state(), P2, new Answer(pick(T1)));

    assertThat(outcome.state().player(P2).coins()).isEqualTo(8);
  }

  @Test
  void answerWithoutPendingDecisionIsRejected() {
    DuelState idle = Fixtures.state(RULES, P1, 7, 7);

    assertThatThrownBy(() -> ENGINE.apply(idle, P1, new Answer(pick(T1))))
        .isInstanceOf(IllegalActionException.class)
        .hasMessageContaining("no decision is pending");
  }

  @Test
  void answeringTwiceIsRejected() {
    Outcome first = ENGINE.apply(paused(), P1, new Answer(pick(T1)));

    assertThatThrownBy(() -> ENGINE.apply(first.state(), P1, new Answer(pick(T2))))
        .isInstanceOf(IllegalActionException.class)
        .hasMessageContaining("no decision is pending");
  }

  @Test
  void answerOutsideTheOfferedOptionsIsRejectedAndStateUntouched() {
    DuelState before = paused();
    Option notOffered = new ComponentOption(SOURCE.id());

    assertThatThrownBy(() -> ENGINE.apply(before, P1, new Answer(notOffered)))
        .isInstanceOf(IllegalActionException.class)
        .hasMessageContaining("not an offered option");
    assertThat(before).isEqualTo(paused());
  }

  @Test
  void answerThatOpensAnotherDecisionStopsAgain() {
    Card twice = Card.of("twice").withEffects(new Fixtures.Choose(), new Fixtures.Choose());
    RuleSet rules = Fixtures.rules(twice, T1, T2);
    Resolution r =
        new Resolution(
            Fixtures.state(
                rules,
                P1,
                7,
                7,
                twice.id(),
                Fixtures.city(P1),
                T1.id(),
                Fixtures.zone("board"),
                T2.id(),
                Fixtures.zone("board")),
            rules);
    r.resolveEffects(twice.id(), P1);

    Outcome outcome = new Engine(rules).apply(r.state(), P1, new Answer(pick(T1)));

    assertThat(outcome.state().agenda().top())
        .isEqualTo(new DecisionStep(P1, new EffectRef(twice.id(), 1), List.of(pick(T2))));
  }

  @Test
  void pausedGameSurvivesTheSnapshot() {
    GdkSerialization serialization = new GdkSerialization();
    DuelState original = paused();
    String json = serialization.writeState(Snapshots.toSnapshot(original), Long.MAX_VALUE);
    DuelState restored =
        Snapshots.fromSnapshot(serialization.readState(json, SevenWondersDuelState.class));

    assertThat(ENGINE.apply(restored, P1, new Answer(pick(T2))))
        .isEqualTo(ENGINE.apply(original, P1, new Answer(pick(T2))));
  }
}
