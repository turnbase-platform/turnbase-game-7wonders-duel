package io.turnbase.games.sevenwondersduel.turn;

import static io.turnbase.games.sevenwondersduel.turn.TurnFixtures.ageOne;
import static io.turnbase.games.sevenwondersduel.turn.TurnFixtures.takeable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.cost.Cost;
import io.turnbase.games.sevenwondersduel.effect.GainCoins;
import io.turnbase.games.sevenwondersduel.engine.BuildWonder;
import io.turnbase.games.sevenwondersduel.engine.Engine;
import io.turnbase.games.sevenwondersduel.engine.IllegalActionException;
import io.turnbase.games.sevenwondersduel.engine.Outcome;
import io.turnbase.games.sevenwondersduel.event.CoinsChanged;
import io.turnbase.games.sevenwondersduel.event.ComponentMoved;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.id.ZoneId;
import io.turnbase.games.sevenwondersduel.setup.SetupFixtures;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.turn.TurnFixtures.Part;
import io.turnbase.games.sevenwondersduel.zone.Location;
import io.turnbase.games.sevenwondersduel.zone.Placement;
import io.turnbase.games.sevenwondersduel.zone.Placements;
import java.util.List;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.Test;

class BuildWonderTest {

  private static final long SEED = 7;
  private static final ComponentId CARD =
      takeable(SetupFixtures.startedAgeOne(TurnFixtures.rulesWith(), SEED));

  private static Location at(String zone, PlayerId owner) {
    return new Location(ZoneId.of(zone), owner.value().toString());
  }

  private static List<ComponentId> unbuilt(DuelState state, PlayerId owner) {
    return Placements.idsIn(state.placements(), at("wonders.unbuilt", owner));
  }

  private record Game(RuleSet rules, DuelState state, PlayerId actor, ComponentId wonder) {
    Outcome build() {
      return new Engine(rules).apply(state, actor, new BuildWonder(CARD, wonder));
    }

    PlayerId other() {
      return state.opponentOf(actor);
    }

    /** Builds, without a turn, all but {@code keep} of the owner's Wonders. */
    Game withBuilt(PlayerId owner, int count) {
      DuelState changed = state;
      for (ComponentId w :
          unbuilt(state, owner).stream().filter(w -> !w.equals(wonder)).limit(count).toList()) {
        changed =
            changed.withPlacements(
                Placements.move(
                    changed.placements(), w, at("wonders.built", owner), rules.zones()));
      }
      return new Game(rules, changed, actor, wonder);
    }
  }

  /** The active player's first Wonder is replaced by {@code wonderPart}; the card is free. */
  private static Game game(UnaryOperator<Part> wonderPart) {
    return game(ageOne(CARD), wonderPart);
  }

  private static Game game(Part card, UnaryOperator<Part> wonderPart) {
    DuelState seen = SetupFixtures.startedAgeOne(TurnFixtures.rulesWith(), SEED);
    PlayerId actor = seen.active();
    ComponentId wonder = unbuilt(seen, actor).get(0);
    RuleSet rules = TurnFixtures.rulesWith(card, wonderPart.apply(Part.wonder(wonder.value())));
    return new Game(rules, SetupFixtures.startedAgeOne(rules, SEED), actor, wonder);
  }

  @Test
  void theWondersCostIsChargedAndTheCardsIsIgnored() {
    Game g = game(ageOne(CARD).costing(Cost.coins(100)), w -> w.costing(Cost.coins(3)));

    assertThat(g.build().state().player(g.actor()).coins())
        .isEqualTo(g.state().player(g.actor()).coins() - 3);
  }

  @Test
  void theCardIsTuckedUnderTheWonderAndTheWonderIsBuilt() {
    Game g = game(w -> w);

    DuelState after = g.build().state();

    assertThat(after.placements())
        .contains(
            new Placement(CARD, new Location(ZoneId.of("underWonder"), g.wonder().value())),
            new Placement(g.wonder(), at("wonders.built", g.actor())));
    assertThat(after.active()).isEqualTo(g.other());
  }

  @Test
  void theWondersEffectsResolveForItsOwner() {
    Game g = game(w -> w.withEffects(new GainCoins(2)));

    assertThat(g.build().state().player(g.actor()).coins())
        .isEqualTo(g.state().player(g.actor()).coins() + 2);
  }

  @Test
  void theTuckedCardHasNoEffect() {
    Game g = game(ageOne(CARD).withEffects(new GainCoins(5)), w -> w);

    assertThat(g.build().state().player(g.actor()).coins())
        .isEqualTo(g.state().player(g.actor()).coins());
  }

  @Test
  void theOpponentsWonderCannotBeBuilt() {
    Game g = game(w -> w);
    ComponentId theirs = unbuilt(g.state(), g.other()).get(0);

    assertThatThrownBy(
            () -> new Engine(g.rules()).apply(g.state(), g.actor(), new BuildWonder(CARD, theirs)))
        .isInstanceOf(IllegalActionException.class)
        .hasMessageContaining("unbuilt Wonders");
  }

  @Test
  void aBuiltWonderCannotBeBuiltAgain() {
    Game g = game(w -> w);
    DuelState built = g.build().state();

    assertThatThrownBy(
            () -> new Engine(g.rules()).apply(built, g.other(), new BuildWonder(CARD, g.wonder())))
        .isInstanceOf(IllegalActionException.class);
  }

  @Test
  void anUnaffordableWonderIsRejectedAndNothingChanges() {
    Game g = game(w -> w.costing(Cost.coins(99)));

    assertThatThrownBy(g::build)
        .isInstanceOf(IllegalActionException.class)
        .hasMessageContaining("cannot afford");
  }

  @Test
  void theSeventhWonderDestroysTheRemainingOnesBeforeItsEffects() {
    Game g = game(w -> w.withEffects(new GainCoins(2)));
    Game six = sixBuilt(g);
    ComponentId leftover = unbuilt(six.state(), six.other()).get(0);

    Outcome out = six.build();

    assertThat(out.state().placements())
        .contains(new Placement(leftover, at("destroyed", six.other())));
    List<Class<?>> kinds = out.events().stream().<Class<?>>map(e -> e.event().getClass()).toList();
    assertThat(kinds.lastIndexOf(ComponentMoved.class))
        .isLessThan(kinds.indexOf(CoinsChanged.class));
  }

  @Test
  void theSixthWonderDestroysNothing() {
    Game g = game(w -> w);
    Game five = g.withBuilt(g.actor(), 2).withBuilt(g.other(), 3);

    assertThat(unbuilt(five.build().state(), five.other())).hasSize(1);
  }

  @Test
  void nothingCanBeBuiltOnceSevenExist() {
    Game g = game(w -> w);
    Game seven = g.withBuilt(g.actor(), 3).withBuilt(g.other(), 4);

    assertThatThrownBy(seven::build)
        .isInstanceOf(IllegalActionException.class)
        .hasMessageContaining("seven");
  }

  private static Game sixBuilt(Game g) {
    return g.withBuilt(g.actor(), 3).withBuilt(g.other(), 3);
  }
}
