package io.turnbase.games.sevenwondersduel.view;

import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P1;
import static org.assertj.core.api.Assertions.assertThat;

import io.turnbase.games.sevenwondersduel.catalog.BasePack;
import io.turnbase.games.sevenwondersduel.catalog.Component;
import io.turnbase.games.sevenwondersduel.catalog.ComponentType;
import io.turnbase.games.sevenwondersduel.catalog.ContentPack;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.effect.Fixtures;
import io.turnbase.games.sevenwondersduel.id.BackId;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PackId;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.zone.Location;
import io.turnbase.games.sevenwondersduel.zone.ZoneKind;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** B-BACK-1: a face-down card shows its back to everybody, and nothing else about it. */
class BackGroupsTest {

  private record Backed(ComponentId id, Optional<BackId> back) implements Component {
    static Backed of(String id, String back) {
      return new Backed(
          ComponentId.of(id), back == null ? Optional.empty() : Optional.of(BackId.of(back)));
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

  private static final List<Component> CARDS =
      List.of(
          Backed.of("a", "age1"),
          Backed.of("b", "age1"),
          Backed.of("c", "guild"),
          Backed.of("loose", null));

  private static final ContentPack PACK =
      new ContentPack() {
        @Override
        public PackId id() {
          return PackId.of("backs");
        }

        @Override
        public List<ZoneKind> zoneKinds() {
          return List.of();
        }

        @Override
        public List<BackId> backs() {
          return List.of(BackId.of("age1"), BackId.of("guild"));
        }

        @Override
        public List<Component> components() {
          return CARDS;
        }
      };

  private static final RuleSet RULES = RuleSet.assemble(List.of(new BasePack(), PACK));
  private static final Location SLOT = Fixtures.zone("deck");

  private static DuelState faceDown() {
    return Fixtures.state(
        RULES,
        P1,
        0,
        0,
        ComponentId.of("a"),
        SLOT,
        ComponentId.of("b"),
        SLOT,
        ComponentId.of("c"),
        SLOT,
        ComponentId.of("loose"),
        Fixtures.zone("box"));
  }

  @Test
  void hiddenCardsAtOnePlaceAreGroupedByBack() {
    DuelView view = Views.of(faceDown(), RULES, new Viewer.Spectator());

    assertThat(view.hidden())
        .containsExactly(new HiddenView("box", "", null, 1), new HiddenView("deck", "", null, 3));
  }

  @Test
  void aFaceDownStructureCardShowsItsBackPerSlotPlace() {
    Location down =
        new io.turnbase.games.sevenwondersduel.structure.Slot(0, 0)
            .location(io.turnbase.games.sevenwondersduel.zone.Zones.STRUCTURE_DOWN);
    Location down2 =
        new io.turnbase.games.sevenwondersduel.structure.Slot(0, 1)
            .location(io.turnbase.games.sevenwondersduel.zone.Zones.STRUCTURE_DOWN);
    DuelState state =
        Fixtures.state(RULES, P1, 0, 0, ComponentId.of("a"), down, ComponentId.of("c"), down2);

    assertThat(Views.of(state, RULES, new Viewer.Spectator()).hidden())
        .extracting(HiddenView::back)
        .containsExactlyInAnyOrder("age1", "guild");
  }

  @Test
  void everySeatSeesTheSameBacks() {
    DuelState state = faceDown();

    assertThat(Views.of(state, RULES, new Viewer.Seat(P1)).hidden())
        .isEqualTo(Views.of(state, RULES, new Viewer.Spectator()).hidden());
  }

  @Test
  void theViewNamesNoHiddenIdentity() {
    String view = Views.of(faceDown(), RULES, new Viewer.Spectator()).toString();

    assertThat(view).doesNotContain("component=a").doesNotContain("\"a\"").doesNotContain("loose");
  }
}
