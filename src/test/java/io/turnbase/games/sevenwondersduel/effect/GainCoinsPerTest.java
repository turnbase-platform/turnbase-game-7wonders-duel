package io.turnbase.games.sevenwondersduel.effect;

import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P1;
import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P2;
import static org.assertj.core.api.Assertions.assertThat;

import io.turnbase.games.sevenwondersduel.catalog.Component;
import io.turnbase.games.sevenwondersduel.catalog.ComponentType;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.catalog.Tag;
import io.turnbase.games.sevenwondersduel.event.CoinsChanged;
import io.turnbase.games.sevenwondersduel.event.EventRecord;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class GainCoinsPerTest {

  private static final Tag GREY = Tag.color("grey");

  private record Tagged(ComponentId id, Set<Tag> tags, List<Effect> effects) implements Component {
    static Tagged grey(String id) {
      return new Tagged(ComponentId.of(id), Set.of(GREY), List.of());
    }

    @Override
    public String name() {
      return id.value();
    }

    @Override
    public ComponentType type() {
      return ComponentType.CARD;
    }
  }

  private static Resolution resolve(Tagged source, Tagged... others) {
    Component[] all = new Component[others.length + 1];
    all[0] = source;
    System.arraycopy(others, 0, all, 1, others.length);
    RuleSet rules = Fixtures.rules(all);
    Object[] placed = new Object[all.length * 2];
    placed[0] = source.id();
    placed[1] = Fixtures.city(P1);
    for (int i = 0; i < others.length; i++) {
      placed[2 + 2 * i] = others[i].id();
      placed[3 + 2 * i] = i % 2 == 0 ? Fixtures.city(P1) : Fixtures.city(P2);
    }
    Resolution r = new Resolution(Fixtures.state(rules, P1, 7, 7, placed), rules);
    r.resolveEffects(source.id(), P1);
    return r;
  }

  private static Tagged payer(Effect effect) {
    return new Tagged(ComponentId.of("payer"), Set.of(), List.of(effect));
  }

  @Test
  void paysEachForEveryLiveTaggedComponentOfTheOwner() {
    Resolution r =
        resolve(
            payer(new GainCoinsPer(Census.of(GREY), 3)),
            Tagged.grey("g1"),
            Tagged.grey("g2"),
            Tagged.grey("g3"));

    // g1 and g3 are the owner's, g2 the opponent's
    assertThat(r.state().player(P1).coins()).isEqualTo(13);
  }

  @Test
  void countsTheSourceItselfWhenItCarriesTheTag() {
    Tagged self =
        new Tagged(
            ComponentId.of("self"), Set.of(GREY), List.of(new GainCoinsPer(Census.of(GREY), 1)));

    assertThat(resolve(self).state().player(P1).coins()).isEqualTo(8);
  }

  @Test
  void nothingToCountPaysNothingAndEmitsNothing() {
    Resolution r = resolve(payer(new GainCoinsPer(Census.of(GREY), 3)));

    assertThat(r.state().player(P1).coins()).isEqualTo(7);
    assertThat(r.events()).extracting(EventRecord::event).noneMatch(CoinsChanged.class::isInstance);
  }
}
