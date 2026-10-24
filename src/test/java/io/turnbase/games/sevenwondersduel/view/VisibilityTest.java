package io.turnbase.games.sevenwondersduel.view;

import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P1;
import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P2;
import static io.turnbase.games.sevenwondersduel.effect.Fixtures.zone;
import static org.assertj.core.api.Assertions.assertThat;

import io.turnbase.games.sevenwondersduel.catalog.BasePack;
import io.turnbase.games.sevenwondersduel.catalog.Component;
import io.turnbase.games.sevenwondersduel.catalog.ContentPack;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.effect.Fixtures;
import io.turnbase.games.sevenwondersduel.effect.Fixtures.Card;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PackId;
import io.turnbase.games.sevenwondersduel.id.ZoneId;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.zone.Location;
import io.turnbase.games.sevenwondersduel.zone.Visibility;
import io.turnbase.games.sevenwondersduel.zone.ZoneKind;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class VisibilityTest {

  private static final Card CARD = Card.of("card");
  private static final ComponentId ID = CARD.id();
  private static final Viewer ONE = new Viewer.Seat(P1);
  private static final Viewer TWO = new Viewer.Seat(P2);
  private static final Viewer SPECTATOR = new Viewer.Spectator();

  private static final ContentPack SECRET =
      new ContentPack() {
        @Override
        public PackId id() {
          return PackId.of("secret");
        }

        @Override
        public List<ZoneKind> zoneKinds() {
          return List.of(new ZoneKind(ZoneId.of("hand"), Visibility.OWNER, false, false));
        }

        @Override
        public List<Component> components() {
          return List.of(CARD);
        }
      };

  private static final RuleSet RULES = RuleSet.assemble(List.of(new BasePack(), SECRET));

  private static DuelState at(Location where) {
    return Fixtures.state(RULES, P1, 0, 0, ID, where);
  }

  @Test
  void aPublicZoneIsSeenByEveryone() {
    DuelState s = at(zone("discard"));

    for (Viewer viewer : List.of(ONE, TWO, SPECTATOR)) {
      assertThat(Views.canSee(s, RULES, viewer, ID)).isTrue();
    }
  }

  @Test
  void aHiddenZoneIsSeenByNobodyWhoHasNotSeenTheComponent() {
    for (String hidden : List.of("deck", "structure.down", "box")) {
      DuelState s = at(zone(hidden));
      for (Viewer viewer : List.of(ONE, TWO, SPECTATOR)) {
        assertThat(Views.canSee(s, RULES, viewer, ID)).as(hidden).isFalse();
      }
    }
  }

  @Test
  void aSeatSeesWhatItHasSeenButASpectatorDoesNot() {
    DuelState s = at(zone("underWonder")).withKnowledge(P1, Set.of(ID));

    assertThat(Views.canSee(s, RULES, ONE, ID)).isTrue();
    assertThat(Views.canSee(s, RULES, TWO, ID)).isFalse();
    assertThat(Views.canSee(s, RULES, SPECTATOR, ID)).isFalse();
  }

  @Test
  void anOwnerZoneIsSeenByItsOwnerOnly() {
    DuelState s = at(new Location(ZoneId.of("hand"), P2.value().toString()));

    assertThat(Views.canSee(s, RULES, TWO, ID)).isTrue();
    assertThat(Views.canSee(s, RULES, ONE, ID)).isFalse();
    assertThat(Views.canSee(s, RULES, SPECTATOR, ID)).isFalse();
  }
}
