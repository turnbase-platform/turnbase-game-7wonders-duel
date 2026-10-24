package io.turnbase.games.sevenwondersduel.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.LayoutId;
import io.turnbase.games.sevenwondersduel.id.PackId;
import io.turnbase.games.sevenwondersduel.id.ZoneId;
import io.turnbase.games.sevenwondersduel.structure.Layout;
import io.turnbase.games.sevenwondersduel.zone.Visibility;
import io.turnbase.games.sevenwondersduel.zone.ZoneKind;
import java.util.List;
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
    assertThat(rules.zones().knows(ZoneId.of("deck"))).isTrue();
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
}
