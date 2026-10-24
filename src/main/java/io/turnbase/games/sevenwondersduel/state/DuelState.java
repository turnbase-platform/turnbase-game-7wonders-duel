package io.turnbase.games.sevenwondersduel.state;

import io.turnbase.games.sevenwondersduel.agenda.Agenda;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PackId;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.rng.RngCursor;
import io.turnbase.games.sevenwondersduel.zone.Placement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The domain model of one game: an immutable value. It is deliberately free of the GDK
 * serialization rules; {@code snapshot.Snapshots} maps it to and from the wire state.
 *
 * @param phase the current stage
 * @param active the player whose turn it is
 * @param placements where every component currently is
 * @param players the two seats and their scalar state
 * @param knowledge for each player, the components whose identity they have seen
 * @param rng position in the seeded random stream
 * @param packs the content packs the game was set up with
 * @param agenda the pending steps: effects still to apply and decisions still to be answered
 */
public record DuelState(
    Phase phase,
    PlayerId active,
    List<Placement> placements,
    Map<PlayerId, PlayerState> players,
    Map<PlayerId, Set<ComponentId>> knowledge,
    RngCursor rng,
    List<PackId> packs,
    Agenda agenda) {

  public DuelState {
    placements = List.copyOf(placements);
    players = Map.copyOf(players);
    knowledge = copyOfSets(knowledge);
    packs = List.copyOf(packs);
    if (players.size() != 2) {
      throw new IllegalArgumentException("a duel has exactly two players: " + players.size());
    }
    if (!players.containsKey(active)) {
      throw new IllegalArgumentException("active player is not seated: " + active);
    }
    for (PlayerId known : knowledge.keySet()) {
      if (!players.containsKey(known)) {
        throw new IllegalArgumentException("knowledge for a player not seated: " + known.value());
      }
    }
    Set<ComponentId> seen = new HashSet<>();
    for (Placement placement : placements) {
      if (!seen.add(placement.component())) {
        throw new IllegalArgumentException(
            "component placed more than once: " + placement.component().value());
      }
    }
  }

  public PlayerState player(PlayerId id) {
    PlayerState state = players.get(id);
    if (state == null) {
      throw new IllegalArgumentException("unknown player: " + id.value());
    }
    return state;
  }

  public PlayerId opponentOf(PlayerId id) {
    player(id);
    return players.keySet().stream().filter(p -> !p.equals(id)).findFirst().orElseThrow();
  }

  public DuelState withPhase(Phase phase) {
    return new DuelState(phase, active, placements, players, knowledge, rng, packs, agenda);
  }

  public DuelState withActive(PlayerId active) {
    return new DuelState(phase, active, placements, players, knowledge, rng, packs, agenda);
  }

  public DuelState withPlacements(List<Placement> placements) {
    return new DuelState(phase, active, placements, players, knowledge, rng, packs, agenda);
  }

  public DuelState withPlayer(PlayerId id, PlayerState state) {
    player(id);
    Map<PlayerId, PlayerState> next = new HashMap<>(players);
    next.put(id, state);
    return new DuelState(phase, active, placements, next, knowledge, rng, packs, agenda);
  }

  public DuelState withKnowledge(PlayerId id, Set<ComponentId> known) {
    player(id);
    Map<PlayerId, Set<ComponentId>> next = new HashMap<>(knowledge);
    next.put(id, known);
    return new DuelState(phase, active, placements, players, next, rng, packs, agenda);
  }

  public DuelState withAgenda(Agenda agenda) {
    return new DuelState(phase, active, placements, players, knowledge, rng, packs, agenda);
  }

  public DuelState withRng(RngCursor rng) {
    return new DuelState(phase, active, placements, players, knowledge, rng, packs, agenda);
  }

  private static Map<PlayerId, Set<ComponentId>> copyOfSets(Map<PlayerId, Set<ComponentId>> in) {
    Map<PlayerId, Set<ComponentId>> out = new HashMap<>();
    in.forEach((player, ids) -> out.put(player, Set.copyOf(ids)));
    return Map.copyOf(out);
  }
}
