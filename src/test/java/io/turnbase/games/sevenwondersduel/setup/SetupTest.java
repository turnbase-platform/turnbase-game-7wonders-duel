package io.turnbase.games.sevenwondersduel.setup;

import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P1;
import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.turnbase.games.sevenwondersduel.catalog.Component;
import io.turnbase.games.sevenwondersduel.catalog.ComponentType;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.catalog.SetupParams;
import io.turnbase.games.sevenwondersduel.catalog.Tag;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.ZoneId;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.state.Pawn;
import io.turnbase.games.sevenwondersduel.state.Phase;
import io.turnbase.games.sevenwondersduel.zone.Location;
import io.turnbase.games.sevenwondersduel.zone.Placements;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class SetupTest {

  private static final RuleSet RULES = SetupFixtures.baseSized();

  private static DuelState start(long seed) {
    return Setup.newGame(RULES, seed, P1, P2);
  }

  private static List<ComponentId> in(DuelState state, Location at) {
    return Placements.idsIn(state.placements(), at);
  }

  private static List<ComponentId> in(DuelState state, String zone) {
    return Placements.idsIn(state.placements(), ZoneId.of(zone));
  }

  @Test
  void startsInTheWonderDraftWithSevenCoinsEach() {
    DuelState state = start(1);

    assertThat(state.phase()).isEqualTo(Phase.WONDER_DRAFT);
    assertThat(state.age()).isZero();
    assertThat(state.deciding()).isNull();
    assertThat(state.player(P1).coins()).isEqualTo(7);
    assertThat(state.player(P2).coins()).isEqualTo(7);
    assertThat(state.agenda().isEmpty()).isTrue();
  }

  @Test
  void pawnStartsAtTheCentre() {
    assertThat(start(1).pawn()).isEqualTo(Pawn.CENTRE);
  }

  @Test
  void firstPlayerIsSeededAndActive() {
    Set<Object> seen = new HashSet<>();
    for (long seed = 0; seed < 40; seed++) {
      // java.util.Random gives the same first bit for small seeds, so spread them out
      DuelState state = start(seed * 0x9E3779B97F4A7C15L);
      assertThat(state.active()).isEqualTo(state.first());
      seen.add(state.first());
    }
    assertThat(seen).containsExactlyInAnyOrder(P1, P2);
  }

  @Test
  void fiveProgressTokensAreFaceUpOnTheBoardAndFiveInTheBox() {
    DuelState state = start(3);

    assertThat(in(state, "board")).hasSize(5).allMatch(id -> isProgress(id));
    assertThat(in(state, "box").stream().filter(SetupTest::isProgress)).hasSize(5);
  }

  private static boolean isProgress(ComponentId id) {
    return RULES.catalog().get(id).tags().contains(Tag.PROGRESS);
  }

  @Test
  void everyAgeDeckHoldsTwentyCardsOfItsAge() {
    DuelState state = start(5);

    for (int age = 1; age <= 3; age++) {
      List<ComponentId> deck = in(state, Setup.ageDeck(age));
      assertThat(deck).hasSize(20);
      int n = age;
      assertThat(deck.stream().filter(id -> RULES.catalog().get(id).tags().contains(Tag.age(n))))
          .hasSize(age == 3 ? 17 : 20);
    }
  }

  @Test
  void threeCardsPerAgeAndFourGuildsGoToTheBox() {
    DuelState state = start(5);
    List<ComponentId> box = in(state, "box");

    assertThat(box.stream().filter(id -> id.value().startsWith("a1-"))).hasSize(3);
    assertThat(box.stream().filter(id -> id.value().startsWith("a2-"))).hasSize(3);
    assertThat(box.stream().filter(id -> id.value().startsWith("a3-"))).hasSize(3);
    assertThat(box.stream().filter(id -> id.value().startsWith("guild-"))).hasSize(4);
  }

  @Test
  void ageThreeDeckHoldsThreeGuilds() {
    DuelState state = start(9);

    assertThat(
            in(state, Setup.ageDeck(3)).stream()
                .filter(id -> RULES.catalog().get(id).tags().contains(Tag.GUILD)))
        .hasSize(3);
  }

  @Test
  void fourWondersAreRevealedAndEightWaitInThePool() {
    DuelState state = start(2);

    assertThat(in(state, "draft")).hasSize(4);
    assertThat(in(state, Setup.WONDER_POOL)).hasSize(8);
  }

  @Test
  void sameSeedGivesTheSameSetupAndOtherSeedsDiffer() {
    assertThat(start(11)).isEqualTo(start(11));
    assertThat(start(11).placements()).isNotEqualTo(start(12).placements());
  }

  @Test
  void theRandomStreamAdvancesPastEverythingDrawn() {
    assertThat(start(1).rng().draws()).isGreaterThan(0);
  }

  @Test
  void aCatalogThatDoesNotFitTheRulesIsRejectedByName() {
    assertThatThrownBy(
            () ->
                Setup.newGame(
                    SetupFixtures.rules(SetupFixtures.pieces(23, 23, 20, 7, 12, 4)), 1, P1, P2))
        .hasMessageContaining("progress tokens");
    assertThatThrownBy(
            () ->
                Setup.newGame(
                    SetupFixtures.rules(SetupFixtures.pieces(23, 23, 20, 2, 12, 10)), 1, P1, P2))
        .hasMessageContaining("guilds");
    assertThatThrownBy(
            () ->
                Setup.newGame(
                    SetupFixtures.rules(SetupFixtures.pieces(23, 23, 20, 7, 7, 10)), 1, P1, P2))
        .hasMessageContaining("wonders");
    assertThatThrownBy(
            () ->
                Setup.newGame(
                    SetupFixtures.rules(SetupFixtures.pieces(23, 22, 20, 7, 12, 10)), 1, P1, P2))
        .hasMessageContaining("age 2");
  }

  @Test
  void aPlayerCannotPlayThemself() {
    assertThatThrownBy(() -> Setup.newGame(RULES, 1, P1, P1))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void aCardDealtFaceDownNeedsABack() {
    List<Component> pieces = new ArrayList<>(SetupFixtures.pieces(23, 23, 20, 7, 12, 10));
    int at = 0;
    pieces.set(
        at, SetupFixtures.piece(pieces.get(at).id().value(), ComponentType.CARD, Tag.age(1)));
    RuleSet rules = SetupFixtures.rules(pieces);

    assertThatThrownBy(() -> Setup.newGame(rules, 1, P1, P2))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining(pieces.get(at).id().value())
        .hasMessageContaining("back");
  }

  @Test
  void theDeclaredSetupParametersDriveTheStart() {
    RuleSet rules = SetupFixtures.baseSized();
    SetupParams params = rules.setup();

    DuelState state = Setup.newGame(rules, 1, P1, P2);

    assertThat(state.player(P1).coins()).isEqualTo(params.startingCoins());
    assertThat(in(state, "board")).hasSize(params.progressOnBoard());
  }
}
