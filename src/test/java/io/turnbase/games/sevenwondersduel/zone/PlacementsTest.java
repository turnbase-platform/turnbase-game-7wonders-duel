package io.turnbase.games.sevenwondersduel.zone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.ZoneId;
import java.util.List;
import org.junit.jupiter.api.Test;

class PlacementsTest {

  private static final ZoneId DECK = ZoneId.of("deck");
  private static final ZoneId CITY = ZoneId.of("city");
  private static final ZoneId DESTROYED = ZoneId.of("destroyed");
  private static final ZoneRegistry ZONES =
      ZoneRegistry.of(
          List.of(
              new ZoneKind(DECK, Visibility.NONE, true, false),
              new ZoneKind(CITY, Visibility.PUBLIC, false, false),
              new ZoneKind(DESTROYED, Visibility.PUBLIC, false, true)));

  private static final ComponentId A = ComponentId.of("a");
  private static final ComponentId B = ComponentId.of("b");

  private static List<Placement> deckOfAB() {
    List<Placement> all = Placements.place(List.of(), A, Location.of(DECK), ZONES);
    return Placements.place(all, B, Location.of(DECK), ZONES);
  }

  @Test
  void placeAddsAndRejectsDuplicates() {
    List<Placement> all = deckOfAB();

    assertThat(Placements.idsIn(all, DECK)).containsExactly(A, B);
    assertThatThrownBy(() -> Placements.place(all, A, Location.of(CITY), ZONES))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("a");
  }

  @Test
  void placeIntoUnknownZoneFails() {
    assertThatThrownBy(() -> Placements.place(List.of(), A, Location.of(ZoneId.of("moon")), ZONES))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("moon");
  }

  @Test
  void moveChangesLocationAndKeepsOtherPlacements() {
    List<Placement> moved = Placements.move(deckOfAB(), A, new Location(CITY, "p1"), ZONES);

    assertThat(Placements.locationOf(moved, A)).isEqualTo(new Location(CITY, "p1"));
    assertThat(Placements.locationOf(moved, B)).isEqualTo(Location.of(DECK));
  }

  @Test
  void moveAppendsToEndOfOrderedZone() {
    List<Placement> away = Placements.move(deckOfAB(), A, Location.of(CITY), ZONES);
    List<Placement> back = Placements.move(away, A, Location.of(DECK), ZONES);

    assertThat(Placements.idsIn(back, DECK)).containsExactly(B, A);
  }

  @Test
  void moveOutOfTerminalZoneIsRejected() {
    List<Placement> all = Placements.place(List.of(), A, Location.of(DESTROYED), ZONES);

    assertThatThrownBy(() -> Placements.move(all, A, Location.of(CITY), ZONES))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("destroyed");
    assertThat(Placements.locationOf(all, A)).isEqualTo(Location.of(DESTROYED));
  }

  @Test
  void movingIntoATerminalZoneIsAllowed() {
    List<Placement> moved = Placements.move(deckOfAB(), A, Location.of(DESTROYED), ZONES);

    assertThat(Placements.locationOf(moved, A)).isEqualTo(Location.of(DESTROYED));
  }

  @Test
  void moveUnknownComponentAndUnknownZoneFail() {
    List<Placement> all = deckOfAB();

    assertThatThrownBy(() -> Placements.move(all, ComponentId.of("zzz"), Location.of(CITY), ZONES))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("zzz");
    assertThatThrownBy(() -> Placements.move(all, A, Location.of(ZoneId.of("moon")), ZONES))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("moon");
  }

  @Test
  void idsInFiltersByZoneAndByLocation() {
    List<Placement> all = deckOfAB();
    all = Placements.move(all, A, new Location(CITY, "p1"), ZONES);
    all = Placements.move(all, B, new Location(CITY, "p2"), ZONES);

    assertThat(Placements.idsIn(all, CITY)).containsExactly(A, B);
    assertThat(Placements.idsIn(all, new Location(CITY, "p1"))).containsExactly(A);
  }

  @Test
  void operationsDoNotMutateTheInput() {
    List<Placement> original = deckOfAB();
    List<Placement> copy = List.copyOf(original);

    Placements.move(original, A, Location.of(CITY), ZONES);

    assertThat(original).isEqualTo(copy);
  }
}
