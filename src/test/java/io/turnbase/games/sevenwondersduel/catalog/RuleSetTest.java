package io.turnbase.games.sevenwondersduel.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.turnbase.games.sevenwondersduel.id.BackId;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.LayoutId;
import io.turnbase.games.sevenwondersduel.id.PackId;
import io.turnbase.games.sevenwondersduel.id.ZoneId;
import io.turnbase.games.sevenwondersduel.structure.Layout;
import io.turnbase.games.sevenwondersduel.zone.Visibility;
import io.turnbase.games.sevenwondersduel.zone.ZoneKind;
import io.turnbase.games.sevenwondersduel.zone.Zones;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class RuleSetTest {

  private record TestComponent(ComponentId id, String name, ComponentType type)
      implements Component {}

  private record TestPack(
      PackId id, List<ZoneKind> zoneKinds, List<Component> components, List<Layout> layouts)
      implements ContentPack {
    TestPack(PackId id, List<ZoneKind> zoneKinds, List<Component> components) {
      this(id, zoneKinds, components, List.of());
    }
  }

  private static ZoneKind zone(String id) {
    return new ZoneKind(ZoneId.of(id), Visibility.PUBLIC, false, false);
  }

  private static Component component(String id, ComponentType type) {
    return new TestComponent(ComponentId.of(id), "Name " + id, type);
  }

  private static final ContentPack BASE =
      new TestPack(
          PackId.of("base"),
          List.of(zone("deck"), zone("city")),
          List.of(
              component("c1", ComponentType.CARD),
              component("w1", ComponentType.WONDER),
              component("c2", ComponentType.CARD)));

  private static final ContentPack AGORA =
      new TestPack(
          PackId.of("agora"),
          List.of(zone("senate")),
          List.of(component("t1", ComponentType.TOKEN)));

  @Test
  void assembleMergesZonesAndComponents() {
    RuleSet rules = RuleSet.assemble(List.of(BASE, AGORA));

    assertThat(rules.packIds()).containsExactly(PackId.of("base"), PackId.of("agora"));
    assertThat(rules.zones().knows(Zones.DECK)).isTrue();
    assertThat(rules.zones().knows(ZoneId.of("senate"))).isTrue();
    assertThat(rules.catalog().get(ComponentId.of("t1")).type()).isEqualTo(ComponentType.TOKEN);
  }

  @Test
  void catalogLooksUpById() {
    Catalog catalog = RuleSet.assemble(List.of(BASE)).catalog();

    assertThat(catalog.get(ComponentId.of("w1")).name()).isEqualTo("Name w1");
  }

  @Test
  void catalogFiltersByTypeInRegistrationOrder() {
    Catalog catalog = RuleSet.assemble(List.of(BASE, AGORA)).catalog();

    assertThat(catalog.ofType(ComponentType.CARD))
        .extracting(c -> c.id().value())
        .containsExactly("c1", "c2");
  }

  @Test
  void duplicateZoneAcrossPacksNamesBothPacksAndId() {
    ContentPack clash = new TestPack(PackId.of("clash"), List.of(zone("city")), List.of());

    assertThatThrownBy(() -> RuleSet.assemble(List.of(BASE, clash)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("base")
        .hasMessageContaining("clash")
        .hasMessageContaining("city");
  }

  @Test
  void duplicateComponentAcrossPacksNamesBothPacksAndId() {
    ContentPack clash =
        new TestPack(PackId.of("clash"), List.of(), List.of(component("c1", ComponentType.CARD)));

    assertThatThrownBy(() -> RuleSet.assemble(List.of(BASE, clash)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("base")
        .hasMessageContaining("clash")
        .hasMessageContaining("c1");
  }

  @Test
  void unknownComponentIdThrowsWithId() {
    Catalog catalog = RuleSet.assemble(List.of(BASE)).catalog();

    assertThatThrownBy(() -> catalog.get(ComponentId.of("nope")))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("nope");
  }

  private static Layout layout(String id) {
    return new Layout(LayoutId.of(id), List.of(new Layout.Row(true, List.of(0))));
  }

  private static ContentPack packWith(String pack, Layout... layouts) {
    return new TestPack(PackId.of(pack), List.of(), List.of(), List.of(layouts));
  }

  @Test
  void layoutIsLookedUpById() {
    RuleSet rules = RuleSet.assemble(List.of(packWith("base", layout("a"), layout("b"))));

    assertThat(rules.layout(LayoutId.of("b"))).isEqualTo(layout("b"));
  }

  @Test
  void unknownLayoutIsRejectedByName() {
    RuleSet rules = RuleSet.assemble(List.of(packWith("base", layout("a"))));

    assertThatThrownBy(() -> rules.layout(LayoutId.of("missing")))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("missing");
  }

  @Test
  void duplicateLayoutAcrossPacksIsRejected() {
    assertThatThrownBy(
            () ->
                RuleSet.assemble(
                    List.of(packWith("base", layout("a")), packWith("agora", layout("a")))))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("duplicate layout 'a'");
  }

  private record AgePack(PackId id, Map<Integer, LayoutId> ageLayouts) implements ContentPack {
    @Override
    public List<ZoneKind> zoneKinds() {
      return List.of();
    }

    @Override
    public List<Component> components() {
      return List.of();
    }
  }

  private static ContentPack agePack(String pack, Map<Integer, LayoutId> mapping) {
    return new AgePack(PackId.of(pack), mapping);
  }

  @Test
  void baseMapsEachAgeToItsLayout() {
    RuleSet rules = RuleSet.assemble(List.of(new BasePack()));

    assertThat(rules.layoutForAge(1)).isEqualTo(rules.layout(LayoutId.of("age1")));
    assertThat(rules.layoutForAge(2)).isEqualTo(rules.layout(LayoutId.of("age2")));
    assertThat(rules.layoutForAge(3)).isEqualTo(rules.layout(LayoutId.of("age3")));
  }

  @Test
  void unmappedAgeIsRejectedByName() {
    RuleSet rules = RuleSet.assemble(List.of(new BasePack()));

    assertThatThrownBy(() -> rules.layoutForAge(4))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("age 4");
  }

  @Test
  void mappingToAnUnknownLayoutIsRejectedAtAssembly() {
    assertThatThrownBy(
            () -> RuleSet.assemble(List.of(agePack("odd", Map.of(1, LayoutId.of("nowhere"))))))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("nowhere");
  }

  @Test
  void duplicateAgeMappingIsRejected() {
    ContentPack other = agePack("agora", Map.of(1, LayoutId.of("age2")));

    assertThatThrownBy(() -> RuleSet.assemble(List.of(new BasePack(), other)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("duplicate age '1'");
  }

  private record BackedCard(ComponentId id, BackId backId) implements Component {
    @Override
    public String name() {
      return id.value();
    }

    @Override
    public ComponentType type() {
      return ComponentType.CARD;
    }

    @Override
    public Optional<BackId> back() {
      return Optional.of(backId);
    }
  }

  private record BackPack(PackId id, List<BackId> backs, List<Component> components)
      implements ContentPack {
    @Override
    public List<ZoneKind> zoneKinds() {
      return List.of();
    }
  }

  @Test
  void aDeclaredBackIsAccepted() {
    ContentPack pack =
        new BackPack(
            PackId.of("p"),
            List.of(BackId.of("brown")),
            List.of(new BackedCard(ComponentId.of("c"), BackId.of("brown"))));

    assertThat(RuleSet.assemble(List.of(pack)).catalog().get(ComponentId.of("c")).back())
        .contains(BackId.of("brown"));
  }

  @Test
  void aBackNoPackDeclaresIsRejectedByCardAndBack() {
    ContentPack pack =
        new BackPack(
            PackId.of("p"),
            List.of(),
            List.of(new BackedCard(ComponentId.of("c"), BackId.of("brown"))));

    assertThatThrownBy(() -> RuleSet.assemble(List.of(pack)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("'c'")
        .hasMessageContaining("brown");
  }

  @Test
  void aBackMayBeDeclaredByAnotherPack() {
    ContentPack declares = new BackPack(PackId.of("a"), List.of(BackId.of("brown")), List.of());
    ContentPack uses =
        new BackPack(
            PackId.of("b"),
            List.of(),
            List.of(new BackedCard(ComponentId.of("c"), BackId.of("brown"))));

    assertThat(RuleSet.assemble(List.of(declares, uses)).catalog().get(ComponentId.of("c")))
        .isNotNull();
  }

  @Test
  void duplicateBackAcrossPacksNamesBothPacksAndId() {
    ContentPack one = new BackPack(PackId.of("one"), List.of(BackId.of("brown")), List.of());
    ContentPack two = new BackPack(PackId.of("two"), List.of(BackId.of("brown")), List.of());

    assertThatThrownBy(() -> RuleSet.assemble(List.of(one, two)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("duplicate back 'brown'")
        .hasMessageContaining("one")
        .hasMessageContaining("two");
  }
}
