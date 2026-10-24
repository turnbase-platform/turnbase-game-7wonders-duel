package io.turnbase.games.sevenwondersduel.science;

import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P1;
import static org.assertj.core.api.Assertions.assertThat;

import io.turnbase.games.sevenwondersduel.agenda.ComponentOption;
import io.turnbase.games.sevenwondersduel.agenda.DecisionStep;
import io.turnbase.games.sevenwondersduel.catalog.ComponentType;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.catalog.Tag;
import io.turnbase.games.sevenwondersduel.effect.Fixtures;
import io.turnbase.games.sevenwondersduel.effect.GainCoins;
import io.turnbase.games.sevenwondersduel.effect.Resolution;
import io.turnbase.games.sevenwondersduel.effect.ScienceCheck;
import io.turnbase.games.sevenwondersduel.state.Phase;
import io.turnbase.games.sevenwondersduel.state.Victory;
import io.turnbase.games.sevenwondersduel.turn.TurnFixtures.Part;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ScienceCheckTest {

  private static ScienceSymbol symbol(String id) {
    return new ScienceSymbol(id);
  }

  /** A green-like card: shows the symbols and checks them when built. */
  private static Part green(String id, String... symbols) {
    return Part.card(id)
        .withModifiers(
            java.util.Arrays.stream(symbols)
                .map(
                    s -> (io.turnbase.games.sevenwondersduel.effect.Modifier) new Symbol(symbol(s)))
                .toArray(io.turnbase.games.sevenwondersduel.effect.Modifier[]::new))
        .withEffects(new ScienceCheck());
  }

  private static Part token(String id) {
    return Part.of(id, ComponentType.TOKEN).tagged(Tag.PROGRESS);
  }

  private static final Part PAY = token("pay").withEffects(new GainCoins(6));
  private static final Part PLAIN = token("plain");
  private static final Part LAW =
      token("law").withModifiers(new Symbol(symbol("scales"))).withEffects(new ScienceCheck());

  /** {@code held} sit in P1's city, the tokens on the board; the source is then resolved. */
  private static Resolution resolve(Part source, List<Part> held, Part... board) {
    List<Part> all = new ArrayList<>(held);
    all.add(source);
    all.addAll(List.of(board));
    RuleSet rules = Fixtures.rules(all.toArray(Part[]::new));
    List<Object> placed = new ArrayList<>();
    for (Part p : held) {
      placed.add(p.id());
      placed.add(Fixtures.city(P1));
    }
    placed.add(source.id());
    placed.add(Fixtures.city(P1));
    for (Part t : board) {
      placed.add(t.id());
      placed.add(Fixtures.zone("board"));
    }
    Resolution r = new Resolution(Fixtures.state(rules, P1, 7, 7, placed.toArray()), rules);
    r.resolveEffects(source.id(), P1);
    return r;
  }

  private static List<Object> offered(Resolution r) {
    DecisionStep step = (DecisionStep) r.state().agenda().top();
    return new ArrayList<>(step.options());
  }

  @Test
  void theFirstPairOffersEveryProgressTokenOnTheBoard() {
    Resolution r = resolve(green("b", "wheel"), List.of(green("a", "wheel")), PAY, PLAIN);

    assertThat(offered(r))
        .containsExactly(new ComponentOption(PAY.id()), new ComponentOption(PLAIN.id()));
  }

  @Test
  void thePickedTokenGoesToTheCityAndItsEffectsRun() {
    Resolution r = resolve(green("b", "wheel"), List.of(green("a", "wheel")), PAY, PLAIN);

    r.resume(new ComponentOption(PAY.id()));
    r.drain();

    assertThat(
            io.turnbase.games.sevenwondersduel.zone.Placements.locationOf(
                r.state().placements(), PAY.id()))
        .isEqualTo(Fixtures.city(P1));
    assertThat(r.state().player(P1).coins()).isEqualTo(13);
    assertThat(r.state().agenda().isEmpty()).isTrue();
  }

  @Test
  void aThirdSymbolOfAKindGrantsNothing() {
    Resolution r =
        resolve(green("c", "wheel"), List.of(green("a", "wheel"), green("b", "wheel")), PAY);

    assertThat(r.state().agenda().isEmpty()).isTrue();
  }

  @Test
  void aPairWithAnEmptyBoardGrantsNothingAndTheGameGoesOn() {
    Resolution r = resolve(green("b", "wheel"), List.of(green("a", "wheel")));

    assertThat(r.state().agenda().isEmpty()).isTrue();
    assertThat(r.state().phase()).isEqualTo(Phase.AGE);
  }

  @Test
  void aSingleSymbolGrantsNothing() {
    Resolution r = resolve(green("a", "wheel"), List.of(), PAY);

    assertThat(r.state().agenda().isEmpty()).isTrue();
  }

  @Test
  void sixDifferentSymbolsWinEvenWhenTheSixthAlsoCompletesAPair() {
    List<Part> held =
        List.of(
            green("a", "wheel"),
            green("b", "scales"),
            green("c", "pen"),
            green("d", "tablet"),
            green("e", "mortar"));
    Resolution r = resolve(green("f", "wheel", "sphere"), held, PAY);

    assertThat(r.state().phase()).isEqualTo(Phase.OVER);
    assertThat(r.state().victory()).isEqualTo(new Victory(P1, Victory.Reason.SCIENCE));
    assertThat(r.state().agenda().isEmpty()).isTrue();
  }

  @Test
  void aSourceWithTwoSymbolsChecksBoth() {
    Resolution r = resolve(green("b", "pen", "wheel"), List.of(green("a", "wheel")), PAY);

    assertThat(offered(r)).containsExactly(new ComponentOption(PAY.id()));
  }

  @Test
  void aLawTokenTakenAsThePickCanCompleteAnotherPair() {
    Resolution r =
        resolve(
            green("b", "wheel"), List.of(green("a", "wheel"), green("s", "scales")), LAW, PLAIN);

    r.resume(new ComponentOption(LAW.id()));
    r.drain();

    assertThat(offered(r)).containsExactly(new ComponentOption(PLAIN.id()));
  }
}
