package io.turnbase.games.sevenwondersduel.view;

import static org.assertj.core.api.Assertions.assertThat;

import io.turnbase.games.sevenwondersduel.catalog.BasePack;
import io.turnbase.games.sevenwondersduel.catalog.Component;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.content.BaseContent;
import io.turnbase.games.sevenwondersduel.effect.Badge;
import io.turnbase.games.sevenwondersduel.effect.Badged;
import io.turnbase.games.sevenwondersduel.effect.ScienceCheck;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class CatalogViewsTest {

  private static final RuleSet RULES = RuleSet.assemble(List.of(new BasePack(), new BaseContent()));
  private static final CatalogView VIEW = CatalogViews.of(RULES);

  private static CatalogEntry entry(String name) {
    return VIEW.entries().stream().filter(e -> e.name().equals(name)).findFirst().orElseThrow();
  }

  private static Stream<Badged> mechanics(Component component) {
    return Stream.of(
            component.effects().stream(),
            component.modifiers().stream(),
            component.triggers().stream())
        .flatMap(s -> s)
        .map(Badged.class::cast);
  }

  @Test
  void everyComponentOfThePacksIsListedOnceInCatalogOrder() {
    assertThat(VIEW.entries())
        .extracting(CatalogEntry::id)
        .containsExactlyElementsOf(
            RULES.catalog().all().stream().map(c -> c.id().value()).toList());
  }

  @Test
  void everyBaseMechanicDescribesItselfExceptTheScienceCheck() {
    for (Component component : RULES.catalog().all()) {
      mechanics(component)
          .filter(m -> !(m instanceof ScienceCheck))
          .forEach(m -> assertThat(m.badges()).as(component.name() + ": " + m).isNotEmpty());
    }
  }

  @Test
  void countedBadgesNameWhatTheyCount() {
    for (CatalogEntry e : VIEW.entries()) {
      e.badges().stream()
          .filter(b -> b.kind().endsWith("Per"))
          .forEach(b -> assertThat(b.of()).as(e.name()).isNotEmpty());
    }
  }

  @Test
  void anEntryCarriesItsBadgesInEffectModifierTriggerOrder() {
    assertThat(entry("Lumber Yard").badges()).contains(Badge.of("produces", 1, "wood"));
    assertThat(entry("Lumber Yard").type()).isEqualTo("CARD");
    assertThat(entry("Lumber Yard").back()).isNotNull();
  }

  @Test
  void aCostIsListedAsCoinsResourcesAndChainSymbols() {
    CostView stable = entry("Stable").cost();
    CostView barracks = entry("Barracks").cost();

    assertThat(stable.coins()).isZero();
    assertThat(stable.resources()).containsExactly(new ResourceCount("wood", 1));
    assertThat(stable.freedBy()).isEmpty();
    assertThat(barracks.coins()).isEqualTo(3);
    assertThat(barracks.freedBy()).containsExactly("sword");
  }

  @Test
  void tagsAreSortedSoTheMessageIsDeterministic() {
    assertThat(entry("Stable").tags()).isSorted().contains("age:1", "color:red");
    assertThat(entry("Stable").badges()).contains(Badge.of("chain", 0, "horseshoe"));
  }
}
