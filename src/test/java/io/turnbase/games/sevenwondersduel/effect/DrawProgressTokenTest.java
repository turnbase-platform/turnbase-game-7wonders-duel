package io.turnbase.games.sevenwondersduel.effect;

import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P1;
import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P2;
import static org.assertj.core.api.Assertions.assertThat;

import io.turnbase.games.sevenwondersduel.agenda.ComponentOption;
import io.turnbase.games.sevenwondersduel.agenda.DecisionStep;
import io.turnbase.games.sevenwondersduel.catalog.Component;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.catalog.Tag;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.zone.Placements;
import io.turnbase.games.sevenwondersduel.zone.Spots;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class DrawProgressTokenTest {

  private static final Piece SOURCE = Piece.of("source").with(new DrawProgressToken(3));
  private static final List<Piece> TOKENS =
      IntStream.range(0, 5)
          .mapToObj(i -> Piece.of("token-" + i, Tag.PROGRESS).with(new GainCoins(i + 1)))
          .toList();
  private static final RuleSet RULES = rules();

  private static RuleSet rules() {
    List<Component> all = new ArrayList<>(TOKENS);
    all.add(SOURCE);
    return Fixtures.rules(all.toArray(Component[]::new));
  }

  private static Resolution start(int inBox, long seed) {
    List<Object> placed = new ArrayList<>();
    for (int i = 0; i < inBox; i++) {
      placed.add(TOKENS.get(i).id());
      placed.add(Spots.BOX);
    }
    var state = Fixtures.state(RULES, P1, 7, 7, placed.toArray());
    Resolution r =
        new Resolution(
            state.withRng(new io.turnbase.games.sevenwondersduel.rng.RngCursor(seed, 0)), RULES);
    r.queueEffects(SOURCE.id(), P1);
    r.drain();
    return r;
  }

  private static List<ComponentId> offered(Resolution r) {
    if (r.state().agenda().isEmpty() || !(r.state().agenda().top() instanceof DecisionStep step)) {
      return List.of();
    }
    return step.options().stream().map(o -> ((ComponentOption) o).component()).toList();
  }

  @Test
  void threeOfFiveAreOfferedAndOnlyTheOwnerSeesThem() {
    Resolution r = start(5, 1);

    List<ComponentId> offered = offered(r);

    assertThat(offered).hasSize(3).doesNotHaveDuplicates();
    assertThat(r.state().knowledge().get(P1)).containsAll(offered);
    assertThat(r.state().knowledge().get(P2)).isEmpty();
  }

  @Test
  void fewerThanThreeOffersWhatIsThere() {
    assertThat(offered(start(2, 1))).hasSize(2);
    assertThat(offered(start(1, 1))).hasSize(1);
  }

  @Test
  void anEmptyBoxAsksNothing() {
    Resolution r = start(0, 1);

    assertThat(offered(r)).isEmpty();
    assertThat(r.state().agenda().isEmpty()).isTrue();
  }

  @Test
  void theSameSeedOffersTheSameTokens() {
    assertThat(offered(start(5, 7))).isEqualTo(offered(start(5, 7)));
    assertThat(IntStream.range(0, 20).mapToObj(s -> offered(start(5, s))).distinct().count())
        .isGreaterThan(1);
  }

  @Test
  void thePickJoinsTheOwnersCityWithItsEffectsAndTheRestStayInTheBox() {
    Resolution r = start(5, 3);
    List<ComponentId> offered = offered(r);
    ComponentId pick = offered.get(1);

    r.resume(new ComponentOption(pick));
    r.drain();

    assertThat(Placements.locationOf(r.state().placements(), pick)).isEqualTo(Fixtures.city(P1));
    assertThat(Placements.idsIn(r.state().placements(), Spots.BOX)).hasSize(4);
    int index = Integer.parseInt(pick.value().substring("token-".length()));
    assertThat(r.state().player(P1).coins()).isEqualTo(7 + index + 1);
  }
}
