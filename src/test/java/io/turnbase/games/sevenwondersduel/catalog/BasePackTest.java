package io.turnbase.games.sevenwondersduel.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.turnbase.games.sevenwondersduel.SevenWondersDuelState;
import io.turnbase.games.sevenwondersduel.agenda.Agenda;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PackId;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.id.ZoneId;
import io.turnbase.games.sevenwondersduel.rng.RngCursor;
import io.turnbase.games.sevenwondersduel.snapshot.Snapshots;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.state.Pawn;
import io.turnbase.games.sevenwondersduel.state.Phase;
import io.turnbase.games.sevenwondersduel.state.PlayerState;
import io.turnbase.games.sevenwondersduel.zone.Location;
import io.turnbase.games.sevenwondersduel.zone.Placement;
import io.turnbase.games.sevenwondersduel.zone.Placements;
import io.turnbase.games.sevenwondersduel.zone.ZoneRegistry;
import io.turnbase.games.sevenwondersduel.zone.Zones;
import io.turnbase.serialization.GdkSerialization;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class BasePackTest {

  private static final List<String> ZONES =
      List.of(
          "deck",
          "structure.up",
          "structure.down",
          "city",
          "underWonder",
          "discard",
          "draft",
          "wonders.unbuilt",
          "wonders.built",
          "board",
          "box",
          "military",
          "destroyed");

  private static final RuleSet RULES = RuleSet.assemble(List.of(new BasePack()));
  private static final ZoneRegistry ZONE_REGISTRY = RULES.zones();

  private static final PlayerId P1 =
      PlayerId.of(UUID.fromString("00000000-0000-0000-0000-000000000001"));
  private static final PlayerId P2 =
      PlayerId.of(UUID.fromString("00000000-0000-0000-0000-000000000002"));

  @Test
  void ruleSetAssemblesAndKnowsEveryZone() {
    assertThat(RULES.packIds()).containsExactly(PackId.of("base"));
    for (String zone : ZONES) {
      assertThat(ZONE_REGISTRY.knows(ZoneId.of(zone))).as(zone).isTrue();
    }
  }

  @Test
  void onlyDestroyedIsTerminal() {
    for (String zone : ZONES) {
      assertThat(ZONE_REGISTRY.kind(ZoneId.of(zone)).terminal())
          .as(zone)
          .isEqualTo(zone.equals("destroyed"));
    }
  }

  @Test
  void boxedComponentCanBeDrawnButDestroyedCannot() {
    ComponentId token = ComponentId.of("token-1");
    ComponentId wonder = ComponentId.of("wonder-1");
    List<Placement> all = Placements.place(List.of(), token, Location.of(Zones.BOX), ZONE_REGISTRY);
    all = Placements.place(all, wonder, Location.of(Zones.DESTROYED), ZONE_REGISTRY);

    List<Placement> drawn = Placements.move(all, token, Location.of(Zones.BOARD), ZONE_REGISTRY);
    assertThat(Placements.idsIn(drawn, Zones.BOARD)).containsExactly(token);

    List<Placement> stuck = all;
    assertThatThrownBy(
            () ->
                Placements.move(
                    stuck,
                    wonder,
                    new Location(Zones.WONDERS_UNBUILT, P1.value().toString()),
                    ZONE_REGISTRY))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void faceDownCardRevealsByMove() {
    ComponentId card = ComponentId.of("card-1");
    Location down = new Location(Zones.STRUCTURE_DOWN, "2:1");
    Location up = new Location(Zones.STRUCTURE_UP, "2:1");
    List<Placement> all = Placements.place(List.of(), card, down, ZONE_REGISTRY);

    List<Placement> revealed = Placements.move(all, card, up, ZONE_REGISTRY);

    assertThat(Placements.locationOf(revealed, card)).isEqualTo(up);
    assertThat(Placements.idsIn(revealed, Zones.STRUCTURE_DOWN)).isEmpty();
  }

  @Test
  void endToEndThroughSnapshot() {
    ComponentId a = ComponentId.of("card-a");
    ComponentId b = ComponentId.of("card-b");
    ComponentId w = ComponentId.of("wonder-w");
    Location city = new Location(Zones.CITY, P1.value().toString());
    List<Placement> all = Placements.place(List.of(), a, Location.of(Zones.DECK), ZONE_REGISTRY);
    all = Placements.place(all, b, Location.of(Zones.DECK), ZONE_REGISTRY);
    all = Placements.place(all, w, Location.of(Zones.DRAFT), ZONE_REGISTRY);
    all = Placements.move(all, a, city, ZONE_REGISTRY);
    DuelState state =
        new DuelState(
            Phase.AGE,
            P1,
            1,
            P1,
            null,
            all,
            Map.of(P1, new PlayerState(7, false), P2, new PlayerState(7, true)),
            Map.of(P1, Set.of(a), P2, Set.of()),
            new RngCursor(5, 3),
            RULES.packIds(),
            Agenda.EMPTY,
            Pawn.CENTRE,
            null);

    GdkSerialization serialization = new GdkSerialization();
    String json = serialization.writeState(Snapshots.toSnapshot(state), Long.MAX_VALUE);
    DuelState restored =
        Snapshots.fromSnapshot(serialization.readState(json, SevenWondersDuelState.class));

    assertThat(restored).isEqualTo(state);
    assertThat(Placements.idsIn(restored.placements(), city)).containsExactly(a);
    assertThat(Placements.idsIn(restored.placements(), Zones.DECK)).containsExactly(b);
    assertThat(Placements.idsIn(restored.placements(), Zones.DRAFT)).containsExactly(w);
  }
}
