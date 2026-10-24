package io.turnbase.games.sevenwondersduel.turn;

import static io.turnbase.games.sevenwondersduel.turn.TurnFixtures.ageOne;
import static io.turnbase.games.sevenwondersduel.turn.TurnFixtures.takeable;
import static org.assertj.core.api.Assertions.assertThat;

import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.catalog.Tag;
import io.turnbase.games.sevenwondersduel.effect.Fixtures;
import io.turnbase.games.sevenwondersduel.effect.Shields;
import io.turnbase.games.sevenwondersduel.engine.Build;
import io.turnbase.games.sevenwondersduel.engine.BuildWonder;
import io.turnbase.games.sevenwondersduel.engine.Engine;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.military.ShieldBonus;
import io.turnbase.games.sevenwondersduel.setup.SetupFixtures;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.state.Pawn;
import io.turnbase.games.sevenwondersduel.state.Phase;
import io.turnbase.games.sevenwondersduel.turn.TurnFixtures.Part;
import io.turnbase.games.sevenwondersduel.zone.Location;
import io.turnbase.games.sevenwondersduel.zone.Placements;
import io.turnbase.games.sevenwondersduel.zone.Zones;
import org.junit.jupiter.api.Test;

class StrategyTest {

  private static final long SEED = 7;
  private static final ComponentId CARD =
      takeable(SetupFixtures.startedAgeOne(TurnFixtures.rulesWith(), SEED));
  private static final Part STRATEGY =
      Part.card("strategy").withModifiers(new ShieldBonus(Tag.color("red"), 1));

  private record Game(RuleSet rules, DuelState state, PlayerId actor) {
    PlayerId rival() {
      return state.opponentOf(actor);
    }

    Game holding(Part held, PlayerId owner) {
      return new Game(
          rules,
          state.withPlacements(
              Placements.place(state.placements(), held.id(), Fixtures.city(owner), rules.zones())),
          actor);
    }

    DuelState build() {
      return new Engine(rules).apply(state, actor, new Build(CARD)).state();
    }
  }

  private static Game game(Part... parts) {
    Part[] all = new Part[parts.length + 1];
    all[0] = STRATEGY;
    System.arraycopy(parts, 0, all, 1, parts.length);
    RuleSet rules = TurnFixtures.rulesWith(all);
    DuelState state = SetupFixtures.startedAgeOne(rules, SEED);
    return new Game(rules, state, state.active());
  }

  private static Part redShield(int count) {
    return ageOne(CARD).tagged(Tag.age(1), Tag.color("red")).withEffects(new Shields(count));
  }

  @Test
  void aRedCardBuiltWithStrategyGivesOneMoreShield() {
    Game g = game(redShield(1));
    g = g.holding(STRATEGY, g.actor());

    assertThat(g.build().pawn()).isEqualTo(new Pawn(g.rival(), 2));
  }

  @Test
  void withoutStrategyTheShieldIsAlone() {
    Game g = game(redShield(1));

    assertThat(g.build().pawn()).isEqualTo(new Pawn(g.rival(), 1));
  }

  @Test
  void aCardThatIsNotRedGetsNoBonus() {
    Game g = game(ageOne(CARD).withEffects(new Shields(1)));
    g = g.holding(STRATEGY, g.actor());

    assertThat(g.build().pawn()).isEqualTo(new Pawn(g.rival(), 1));
  }

  @Test
  void theOpponentsTokenGivesTheActorNothing() {
    Game g = game(redShield(1));
    g = g.holding(STRATEGY, g.rival());

    assertThat(g.build().pawn()).isEqualTo(new Pawn(g.rival(), 1));
  }

  @Test
  void aWonderGetsNoBonus() {
    DuelState seen = SetupFixtures.startedAgeOne(TurnFixtures.rulesWith(), SEED);
    PlayerId actor = seen.active();
    ComponentId wonder =
        Placements.idsIn(
                seen.placements(), new Location(Zones.WONDERS_UNBUILT, actor.value().toString()))
            .get(0);
    Game g = game(ageOne(CARD), Part.wonder(wonder.value()).withEffects(new Shields(1)));
    g = g.holding(STRATEGY, actor);

    DuelState after =
        new Engine(g.rules()).apply(g.state(), actor, new BuildWonder(CARD, wonder)).state();

    assertThat(after.pawn()).isEqualTo(new Pawn(g.rival(), 1));
  }

  @Test
  void bonusShieldsCanReachTheCapital() {
    Game g = game(redShield(8));
    g = g.holding(STRATEGY, g.actor());

    DuelState after = g.build();

    assertThat(after.phase()).isEqualTo(Phase.OVER);
    assertThat(after.pawn()).isEqualTo(new Pawn(g.rival(), Pawn.CAPITAL));
  }
}
