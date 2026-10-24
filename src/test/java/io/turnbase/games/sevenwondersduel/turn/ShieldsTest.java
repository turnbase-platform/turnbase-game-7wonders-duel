package io.turnbase.games.sevenwondersduel.turn;

import static io.turnbase.games.sevenwondersduel.turn.TurnFixtures.ageOne;
import static io.turnbase.games.sevenwondersduel.turn.TurnFixtures.takeable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.turnbase.games.sevenwondersduel.catalog.ComponentType;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.effect.GrantPlayAgain;
import io.turnbase.games.sevenwondersduel.effect.LoseCoins;
import io.turnbase.games.sevenwondersduel.effect.Shields;
import io.turnbase.games.sevenwondersduel.engine.Build;
import io.turnbase.games.sevenwondersduel.engine.BuildWonder;
import io.turnbase.games.sevenwondersduel.engine.Discard;
import io.turnbase.games.sevenwondersduel.engine.Engine;
import io.turnbase.games.sevenwondersduel.engine.IllegalActionException;
import io.turnbase.games.sevenwondersduel.engine.Outcome;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.id.ZoneId;
import io.turnbase.games.sevenwondersduel.military.MilitaryTrack;
import io.turnbase.games.sevenwondersduel.setup.SetupFixtures;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.state.Pawn;
import io.turnbase.games.sevenwondersduel.state.Phase;
import io.turnbase.games.sevenwondersduel.state.Victory;
import io.turnbase.games.sevenwondersduel.turn.TurnFixtures.Part;
import io.turnbase.games.sevenwondersduel.zone.Location;
import io.turnbase.games.sevenwondersduel.zone.Placements;
import org.junit.jupiter.api.Test;

class ShieldsTest {

  private static final long SEED = 7;
  private static final ComponentId CARD =
      takeable(SetupFixtures.startedAgeOne(TurnFixtures.rulesWith(), SEED));
  private static final Part LOOT =
      Part.of("loot", ComponentType.TOKEN).withEffects(new LoseCoins(2));

  private record Game(RuleSet rules, DuelState state, PlayerId actor) {
    PlayerId rival() {
      return state.opponentOf(actor);
    }

    Game withLootIn(int zone) {
      return new Game(
          rules,
          state.withPlacements(
              Placements.place(
                  state.placements(),
                  LOOT.id(),
                  MilitaryTrack.tokenSpot(rival(), zone),
                  rules.zones())),
          actor);
    }

    Outcome build() {
      return new Engine(rules).apply(state, actor, new Build(CARD));
    }
  }

  private static Game game(Part card) {
    RuleSet rules = TurnFixtures.rulesWith(card, LOOT);
    DuelState state = SetupFixtures.startedAgeOne(rules, SEED);
    return new Game(rules, state, state.active());
  }

  private static Part shielding(int count) {
    return ageOne(CARD).withEffects(new Shields(count));
  }

  @Test
  void aBuiltCardPushesThePawnTowardTheOpponentAndTheTurnPasses() {
    Game g = game(shielding(2));

    DuelState after = g.build().state();

    assertThat(after.pawn()).isEqualTo(new Pawn(g.rival(), 2));
    assertThat(after.active()).isEqualTo(g.rival());
  }

  @Test
  void aBuiltWonderPushesThePawnToo() {
    DuelState seen = SetupFixtures.startedAgeOne(TurnFixtures.rulesWith(), SEED);
    PlayerId actor = seen.active();
    ComponentId wonder =
        Placements.idsIn(
                seen.placements(),
                new Location(ZoneId.of("wonders.unbuilt"), actor.value().toString()))
            .get(0);
    RuleSet rules =
        TurnFixtures.rulesWith(
            ageOne(CARD), Part.wonder(wonder.value()).withEffects(new Shields(1)));
    DuelState state = SetupFixtures.startedAgeOne(rules, SEED);

    DuelState after = new Engine(rules).apply(state, actor, new BuildWonder(CARD, wonder)).state();

    assertThat(after.pawn()).isEqualTo(new Pawn(state.opponentOf(actor), 1));
  }

  @Test
  void shieldsWithPlayAgainMoveThePawnAndKeepTheMove() {
    Game g = game(ageOne(CARD).withEffects(new Shields(1), new GrantPlayAgain()));

    DuelState after = g.build().state();

    assertThat(after.pawn()).isEqualTo(new Pawn(g.rival(), 1));
    assertThat(after.active()).isEqualTo(g.actor());
  }

  @Test
  void enteringATokenZoneFiresTheLootBeforeTheTurnEnds() {
    Game g = game(shielding(1)).withLootIn(1);

    DuelState after = g.build().state();

    assertThat(after.player(g.rival()).coins()).isEqualTo(g.state().player(g.rival()).coins() - 2);
    assertThat(after.active()).isEqualTo(g.rival());
    assertThat(Placements.locationOf(after.placements(), LOOT.id()).zone())
        .isEqualTo(ZoneId.of("destroyed"));
  }

  @Test
  void reachingTheCapitalEndsTheGameAtOnce() {
    Game g = game(shielding(9));

    DuelState after = g.build().state();

    assertThat(after.pawn()).isEqualTo(new Pawn(g.rival(), Pawn.CAPITAL));
    assertThat(after.phase()).isEqualTo(Phase.OVER);
    assertThat(after.victory()).isEqualTo(new Victory(g.actor(), Victory.Reason.MILITARY));
    assertThat(after.active()).isEqualTo(g.actor());
    assertThat(after.agenda().isEmpty()).isTrue();
  }

  @Test
  void aFinishedGameRejectsEveryFurtherAction() {
    Game g = game(shielding(9));
    DuelState over = g.build().state();
    Engine engine = new Engine(g.rules());

    assertThatThrownBy(() -> engine.apply(over, over.active(), new Discard(takeable(over))))
        .isInstanceOf(IllegalActionException.class)
        .hasMessageContaining("over");
  }
}
