package io.turnbase.games.sevenwondersduel.effect;

import io.turnbase.games.sevenwondersduel.agenda.Agenda;
import io.turnbase.games.sevenwondersduel.agenda.ComponentOption;
import io.turnbase.games.sevenwondersduel.agenda.Option;
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
import io.turnbase.games.sevenwondersduel.zone.Placement;
import io.turnbase.games.sevenwondersduel.zone.Placements;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Shared test fixtures: a tiny catalog of behaviour-carrying cards over the base zones. */
public final class Fixtures {

  public static final PlayerId P1 =
      PlayerId.of(UUID.fromString("00000000-0000-0000-0000-000000000001"));
  public static final PlayerId P2 =
      PlayerId.of(UUID.fromString("00000000-0000-0000-0000-000000000002"));

  public record Card(
      ComponentId id, List<Effect> effects, List<Modifier> modifiers, List<Trigger<?>> triggers)
      implements Component {

    public static Card of(String id) {
      return new Card(ComponentId.of(id), List.of(), List.of(), List.of());
    }

    public Card withEffects(Effect... effects) {
      return new Card(id, List.of(effects), modifiers, triggers);
    }

    public Card withTriggers(Trigger<?>... triggers) {
      return new Card(id, effects, modifiers, List.of(triggers));
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

  /**
   * Asks the owner to pick one component from the board; the pick goes into the owner's city. Each
   * option is a {@link ComponentOption}.
   */
  public static final class Choose implements Effect {
    @Override
    public void apply(Resolution resolution, PlayerId owner) {
      resolution.decide(
          owner,
          Placements.idsIn(resolution.state().placements(), ZoneId.of("board")).stream()
              .<Option>map(ComponentOption::new)
              .toList());
    }

    @Override
    public void resume(Resolution resolution, PlayerId owner, Option answer) {
      resolution.move(((ComponentOption) answer).component(), city(owner));
    }
  }

  public static RuleSet rules(Component... components) {
    ContentPack pack =
        new ContentPack() {
          @Override
          public PackId id() {
            return PackId.of("fixture");
          }

          @Override
          public List<io.turnbase.games.sevenwondersduel.zone.ZoneKind> zoneKinds() {
            return List.of();
          }

          @Override
          public List<Component> components() {
            return List.of(components);
          }
        };
    return RuleSet.assemble(List.of(new BasePack(), pack));
  }

  public static Location city(PlayerId player) {
    return new Location(
        io.turnbase.games.sevenwondersduel.id.ZoneId.of("city"), player.value().toString());
  }

  public static Location zone(String id) {
    return Location.of(io.turnbase.games.sevenwondersduel.id.ZoneId.of(id));
  }

  /** A state with the given treasuries; {@code placed} alternates component, location. */
  public static DuelState state(
      RuleSet rules, PlayerId active, int coins1, int coins2, Object... placed) {
    List<Placement> placements = new ArrayList<>();
    for (int i = 0; i < placed.length; i += 2) {
      placements =
          Placements.place(
              placements, (ComponentId) placed[i], (Location) placed[i + 1], rules.zones());
    }
    return new DuelState(
        Phase.AGE,
        active,
        1,
        active,
        null,
        placements,
        Map.of(P1, new PlayerState(coins1, false), P2, new PlayerState(coins2, false)),
        Map.of(P1, Set.of(), P2, Set.of()),
        RngCursor.start(1),
        rules.packIds(),
        Agenda.EMPTY,
        Pawn.CENTRE);
  }

  private Fixtures() {}
}
