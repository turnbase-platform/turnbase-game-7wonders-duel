package io.turnbase.games.sevenwondersduel.snapshot;

import io.turnbase.games.sevenwondersduel.SevenWondersDuelState;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PackId;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.id.ZoneId;
import io.turnbase.games.sevenwondersduel.rng.RngCursor;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.state.Phase;
import io.turnbase.games.sevenwondersduel.state.PlayerState;
import io.turnbase.games.sevenwondersduel.zone.Location;
import io.turnbase.games.sevenwondersduel.zone.Placement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Lossless, deterministic mapping between the domain {@link DuelState} and the GDK-compatible
 * {@link SevenWondersDuelState}. Unordered knowledge sets are written sorted so equal states give
 * equal snapshots.
 */
public final class Snapshots {

  private Snapshots() {}

  public static SevenWondersDuelState toSnapshot(DuelState state) {
    Map<UUID, PlayerData> players = new HashMap<>();
    state
        .players()
        .forEach((id, p) -> players.put(id.value(), new PlayerData(p.coins(), p.playAgain())));
    Map<UUID, List<String>> knowledge = new HashMap<>();
    state
        .knowledge()
        .forEach(
            (id, known) ->
                knowledge.put(
                    id.value(), known.stream().map(ComponentId::value).sorted().toList()));
    return new SevenWondersDuelState(
        state.phase().name(),
        state.active().value(),
        state.placements().stream()
            .map(p -> new PlacementData(p.component().value(), p.at().zone().value(), p.at().key()))
            .toList(),
        Map.copyOf(players),
        Map.copyOf(knowledge),
        state.rng().seed(),
        state.rng().draws(),
        state.packs().stream().map(PackId::value).toList());
  }

  public static DuelState fromSnapshot(SevenWondersDuelState snapshot) {
    Map<PlayerId, PlayerState> players = new HashMap<>();
    snapshot
        .players()
        .forEach(
            (id, p) -> players.put(PlayerId.of(id), new PlayerState(p.coins(), p.playAgain())));
    Map<PlayerId, Set<ComponentId>> knowledge = new HashMap<>();
    snapshot
        .knowledge()
        .forEach(
            (id, known) -> {
              Set<ComponentId> ids = new HashSet<>();
              known.forEach(c -> ids.add(ComponentId.of(c)));
              knowledge.put(PlayerId.of(id), ids);
            });
    return new DuelState(
        phase(snapshot.phase()),
        PlayerId.of(snapshot.active()),
        snapshot.placements().stream()
            .map(
                p ->
                    new Placement(
                        ComponentId.of(p.component()), new Location(ZoneId.of(p.zone()), p.key())))
            .toList(),
        players,
        knowledge,
        new RngCursor(snapshot.rngSeed(), snapshot.rngDraws()),
        snapshot.packs().stream().map(PackId::of).toList());
  }

  private static Phase phase(String name) {
    try {
      return Phase.valueOf(name);
    } catch (IllegalArgumentException | NullPointerException e) {
      throw new IllegalArgumentException("unknown phase: " + name, e);
    }
  }
}
