package io.turnbase.games.sevenwondersduel.effect;

import static io.turnbase.games.sevenwondersduel.effect.ResolutionTest.P1;
import static io.turnbase.games.sevenwondersduel.effect.ResolutionTest.P2;
import static org.assertj.core.api.Assertions.assertThat;

import io.turnbase.games.sevenwondersduel.agenda.Agenda;
import io.turnbase.games.sevenwondersduel.catalog.BasePack;
import io.turnbase.games.sevenwondersduel.catalog.Component;
import io.turnbase.games.sevenwondersduel.catalog.ComponentType;
import io.turnbase.games.sevenwondersduel.catalog.ContentPack;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PackId;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.id.ZoneId;
import io.turnbase.games.sevenwondersduel.rng.RngCursor;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.state.Pawn;
import io.turnbase.games.sevenwondersduel.state.Phase;
import io.turnbase.games.sevenwondersduel.state.PlayerState;
import io.turnbase.games.sevenwondersduel.zone.Location;
import io.turnbase.games.sevenwondersduel.zone.Placements;
import io.turnbase.games.sevenwondersduel.zone.Visibility;
import io.turnbase.games.sevenwondersduel.zone.ZoneKind;
import io.turnbase.games.sevenwondersduel.zone.ZoneRegistry;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class OwnershipTest {

  interface Discount extends Modifier {}

  record Cheap() implements Discount {}

  record Other() implements Modifier {}

  record Card(ComponentId id, List<Modifier> modifiers) implements Component {
    @Override
    public String name() {
      return id.value();
    }

    @Override
    public ComponentType type() {
      return ComponentType.CARD;
    }
  }

  private static final ZoneId CITY = ZoneId.of("city");
  private static final ZoneId DISCARD = ZoneId.of("discard");
  private static final ComponentId A = ComponentId.of("a");
  private static final ComponentId B = ComponentId.of("b");

  private static final ContentPack PACK =
      new ContentPack() {
        @Override
        public PackId id() {
          return PackId.of("test");
        }

        @Override
        public List<ZoneKind> zoneKinds() {
          return List.of(
              new ZoneKind(CITY, Visibility.PUBLIC, false, false, true),
              new ZoneKind(DISCARD, Visibility.PUBLIC, true, false, false));
        }

        @Override
        public List<Component> components() {
          return List.of(
              new Card(A, List.of(new Cheap(), new Other())), new Card(B, List.of(new Other())));
        }
      };
  private static final RuleSet RULES = RuleSet.assemble(List.of(PACK));

  private static Location city(PlayerId p) {
    return new Location(CITY, p.value().toString());
  }

  private static DuelState with(Location a, Location b) {
    ZoneRegistry zones = RULES.zones();
    var placements = Placements.place(List.of(), A, a, zones);
    placements = Placements.place(placements, B, b, zones);
    return new DuelState(
        Phase.AGE,
        P1,
        1,
        P1,
        null,
        placements,
        Map.of(P1, new PlayerState(7, false), P2, new PlayerState(7, false)),
        Map.of(P1, Set.of(), P2, Set.of()),
        RngCursor.start(1),
        RULES.packIds(),
        Agenda.EMPTY,
        Pawn.CENTRE);
  }

  @Test
  void liveComponentsAreThoseInLiveZonesKeyedByThePlayer() {
    DuelState state = with(city(P1), city(P1));

    assertThat(Ownership.liveComponents(state, RULES.zones(), P1)).containsExactly(A, B);
    assertThat(Ownership.liveComponents(state, RULES.zones(), P2)).isEmpty();
  }

  @Test
  void componentInDiscardOrInOpponentsCityIsNotLive() {
    DuelState state = with(Location.of(DISCARD), city(P2));

    assertThat(Ownership.liveComponents(state, RULES.zones(), P1)).isEmpty();
    assertThat(Ownership.liveComponents(state, RULES.zones(), P2)).containsExactly(B);
  }

  @Test
  void modifiersAreCollectedByTypeFromLiveComponentsOnly() {
    DuelState state = with(city(P1), Location.of(DISCARD));

    assertThat(Ownership.modifiers(state, RULES, P1, Discount.class)).containsExactly(new Cheap());
    assertThat(Ownership.modifiers(state, RULES, P1, Modifier.class))
        .containsExactly(new Cheap(), new Other());
  }

  @Test
  void modifiersFollowTheComponentWhenItMoves() {
    DuelState before = with(city(P1), Location.of(DISCARD));
    DuelState after =
        before.withPlacements(Placements.move(before.placements(), A, city(P2), RULES.zones()));

    assertThat(Ownership.modifiers(before, RULES, P1, Discount.class)).hasSize(1);
    assertThat(Ownership.modifiers(after, RULES, P1, Discount.class)).isEmpty();
    assertThat(Ownership.modifiers(after, RULES, P2, Discount.class)).hasSize(1);
  }

  @Test
  void baseZonesMarkCityAndBuiltWondersLive() {
    List<String> live =
        new BasePack()
            .zoneKinds().stream().filter(ZoneKind::live).map(k -> k.id().value()).sorted().toList();

    assertThat(live).containsExactly("city", "wonders.built");
  }
}
