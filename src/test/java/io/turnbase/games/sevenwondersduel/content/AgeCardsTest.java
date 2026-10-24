package io.turnbase.games.sevenwondersduel.content;

import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P1;
import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P2;
import static org.assertj.core.api.Assertions.assertThat;

import io.turnbase.games.sevenwondersduel.catalog.BasePack;
import io.turnbase.games.sevenwondersduel.catalog.Component;
import io.turnbase.games.sevenwondersduel.catalog.ComponentType;
import io.turnbase.games.sevenwondersduel.catalog.ContentPack;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.catalog.Tag;
import io.turnbase.games.sevenwondersduel.cost.ChainSource;
import io.turnbase.games.sevenwondersduel.cost.ChainSymbol;
import io.turnbase.games.sevenwondersduel.effect.Fixtures;
import io.turnbase.games.sevenwondersduel.effect.Resolution;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PackId;
import io.turnbase.games.sevenwondersduel.zone.Location;
import io.turnbase.games.sevenwondersduel.zone.ZoneKind;
import io.turnbase.games.sevenwondersduel.zone.Zones;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class AgeCardsTest {

  private static final RuleSet RULES = RuleSet.assemble(List.of(new BasePack(), new BaseContent()));
  private static final List<Component> CARDS = RULES.catalog().ofType(ComponentType.CARD);

  @Test
  void sixtySixDistinctCardsAcrossTheThreeAges() {
    assertThat(CARDS).hasSize(66);
    assertThat(CARDS.stream().map(Component::id).distinct()).hasSize(66);
    for (int age = 1; age <= 3; age++) {
      assertThat(RULES.catalog().tagged(Tag.age(age))).hasSize(age == 3 ? 20 : 23);
    }
  }

  @Test
  void everyCardHasABackADeclaredByThePackAndOneColour() {
    assertThat(CARDS)
        .allSatisfy(
            card -> {
              assertThat(card.back()).as(card.name()).isPresent();
              assertThat(Backs.ALL).contains(card.back().get());
              assertThat(card.tags().stream().filter(Colors.ALL::contains)).hasSize(1);
            });
  }

  @Test
  void everyChainSymbolIsGivenByOneCardAndFreesAtLeastOne() {
    List<ChainSymbol> given =
        CARDS.stream()
            .flatMap(c -> c.modifiers().stream())
            .filter(ChainSource.class::isInstance)
            .flatMap(m -> ((ChainSource) m).symbols().stream())
            .toList();
    Set<ChainSymbol> freeing = new HashSet<>();
    CARDS.forEach(c -> freeing.addAll(c.cost().freedBy()));

    assertThat(given).hasSize(17).doesNotHaveDuplicates();
    assertThat(freeing).containsExactlyInAnyOrderElementsOf(given);
  }

  @Test
  void aCardIsNeverFreedByTheSymbolItGives() {
    assertThat(CARDS)
        .allSatisfy(
            card -> {
              Set<ChainSymbol> own =
                  card.modifiers().stream()
                      .filter(ChainSource.class::isInstance)
                      .flatMap(m -> ((ChainSource) m).symbols().stream())
                      .collect(Collectors.toSet());
              assertThat(Collections.disjoint(own, card.cost().freedBy())).as(card.name()).isTrue();
            });
  }

  @Test
  void lighthouseCountsItselfAmongTheYellowCardsInTheCity() {
    ComponentId lighthouse = ComponentId.of("lighthouse");
    ComponentId tavern = ComponentId.of("tavern");
    ComponentId press = ComponentId.of("press");
    Resolution r =
        new Resolution(
            Fixtures.state(
                RULES,
                P1,
                7,
                7,
                lighthouse,
                Fixtures.city(P1),
                tavern,
                Fixtures.city(P1),
                press,
                Fixtures.city(P2)),
            RULES);

    r.resolveEffects(lighthouse, P1);

    assertThat(r.state().player(P1).coins()).isEqualTo(9);
  }

  private record TestWonder(ComponentId id) implements Component {
    @Override
    public String name() {
      return id.value();
    }

    @Override
    public ComponentType type() {
      return ComponentType.WONDER;
    }

    @Override
    public Set<Tag> tags() {
      return Set.of(Tag.WONDER);
    }
  }

  private static int arenaPaysWith(Location wonderAt) {
    ContentPack wonders =
        new ContentPack() {
          @Override
          public PackId id() {
            return PackId.of("test-wonders");
          }

          @Override
          public List<ZoneKind> zoneKinds() {
            return List.of();
          }

          @Override
          public List<Component> components() {
            return List.of(new TestWonder(ComponentId.of("w")));
          }
        };
    RuleSet rules = RuleSet.assemble(List.of(new BasePack(), new BaseContent(), wonders));
    ComponentId arena = ComponentId.of("arena");
    Resolution r =
        new Resolution(
            Fixtures.state(
                rules, P1, 7, 7, arena, Fixtures.city(P1), ComponentId.of("w"), wonderAt),
            rules);

    r.resolveEffects(arena, P1);

    return r.state().player(P1).coins() - 7;
  }

  @Test
  void arenaPaysTwoPerConstructedWonder() {
    assertThat(arenaPaysWith(new Location(Zones.WONDERS_BUILT, P1.value().toString())))
        .isEqualTo(2);
  }

  @Test
  void arenaIgnoresWondersStillUnbuilt() {
    assertThat(arenaPaysWith(new Location(Zones.WONDERS_UNBUILT, P1.value().toString()))).isZero();
  }

  @Test
  void chamberOfCommerceWithoutGreyCardsPaysNothing() {
    ComponentId chamber = ComponentId.of("chamber-of-commerce");
    Resolution r =
        new Resolution(Fixtures.state(RULES, P1, 7, 7, chamber, Fixtures.city(P1)), RULES);

    r.resolveEffects(chamber, P1);

    assertThat(r.state().player(P1).coins()).isEqualTo(7);
  }

  @Test
  void eachAgeFillsItsLayoutAfterSetupRemovesThreeCardsAndAddsThreeGuildsToAgeThree() {
    for (int age = 1; age <= 3; age++) {
      int dealt = RULES.catalog().tagged(Tag.age(age)).size() + (age == 3 ? 3 : 0) - 3;
      assertThat(dealt).as("age " + age).isEqualTo(RULES.layoutForAge(age).size());
    }
  }
}
