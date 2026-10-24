package io.turnbase.games.sevenwondersduel.snapshot;

import io.turnbase.games.sevenwondersduel.SevenWondersDuelState;
import io.turnbase.games.sevenwondersduel.agenda.Agenda;
import io.turnbase.games.sevenwondersduel.agenda.ComponentOption;
import io.turnbase.games.sevenwondersduel.agenda.DecisionStep;
import io.turnbase.games.sevenwondersduel.agenda.EffectStep;
import io.turnbase.games.sevenwondersduel.agenda.Option;
import io.turnbase.games.sevenwondersduel.agenda.PlayerOption;
import io.turnbase.games.sevenwondersduel.agenda.Step;
import io.turnbase.games.sevenwondersduel.agenda.TurnEndStep;
import io.turnbase.games.sevenwondersduel.event.EffectRef;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PackId;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.id.ZoneId;
import io.turnbase.games.sevenwondersduel.rng.RngCursor;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.state.Pawn;
import io.turnbase.games.sevenwondersduel.state.Phase;
import io.turnbase.games.sevenwondersduel.state.PlayerState;
import io.turnbase.games.sevenwondersduel.state.Victory;
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

  private static final String EFFECT = "EFFECT";
  private static final String DECISION = "DECISION";
  private static final String TURN_END = "TURN_END";
  private static final String COMPONENT = "COMPONENT";
  private static final String PLAYER = "PLAYER";

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
        state.age(),
        state.first().value(),
        state.deciding() == null ? null : state.deciding().value(),
        state.placements().stream()
            .map(p -> new PlacementData(p.component().value(), p.at().zone().value(), p.at().key()))
            .toList(),
        Map.copyOf(players),
        Map.copyOf(knowledge),
        state.rng().seed(),
        state.rng().draws(),
        state.packs().stream().map(PackId::value).toList(),
        state.agenda().steps().stream().map(Snapshots::stepData).toList(),
        state.pawn().side() == null ? null : state.pawn().side().value(),
        state.pawn().space(),
        state.victory() == null || state.victory().winner() == null
            ? null
            : state.victory().winner().value(),
        state.victory() == null ? null : state.victory().reason().name(),
        Map.of());
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
        snapshot.age(),
        PlayerId.of(snapshot.first()),
        snapshot.deciding() == null ? null : PlayerId.of(snapshot.deciding()),
        snapshot.placements().stream()
            .map(
                p ->
                    new Placement(
                        ComponentId.of(p.component()), new Location(ZoneId.of(p.zone()), p.key())))
            .toList(),
        players,
        knowledge,
        new RngCursor(snapshot.rngSeed(), snapshot.rngDraws()),
        snapshot.packs().stream().map(PackId::of).toList(),
        new Agenda(snapshot.agenda().stream().map(Snapshots::step).toList()),
        new Pawn(
            snapshot.pawnSide() == null ? null : PlayerId.of(snapshot.pawnSide()),
            snapshot.pawnSpace()),
        victory(snapshot));
  }

  private static Victory victory(SevenWondersDuelState snapshot) {
    if (snapshot.victoryWinner() == null && snapshot.victoryReason() == null) {
      return null;
    }
    if (snapshot.victoryReason() == null) {
      throw new IllegalArgumentException("a victory needs a reason");
    }
    PlayerId winner =
        snapshot.victoryWinner() == null ? null : PlayerId.of(snapshot.victoryWinner());
    return new Victory(winner, Victory.Reason.valueOf(snapshot.victoryReason()));
  }

  private static StepData stepData(Step step) {
    return switch (step) {
      case EffectStep e ->
          new StepData(
              EFFECT,
              e.owner().value(),
              e.effect().source().value(),
              e.effect().index(),
              null,
              List.of());
      case DecisionStep d ->
          new StepData(
              DECISION,
              d.owner().value(),
              d.effect().source().value(),
              d.effect().index(),
              d.decider().value(),
              d.options().stream().map(Snapshots::optionData).toList());
      case TurnEndStep t -> new StepData(TURN_END, null, null, 0, null, List.of());
    };
  }

  private static Step step(StepData data) {
    if (TURN_END.equals(data.kind())) {
      return new TurnEndStep();
    }
    EffectRef ref = new EffectRef(ComponentId.of(data.source()), data.index());
    PlayerId owner = PlayerId.of(data.owner());
    return switch (data.kind()) {
      case EFFECT -> new EffectStep(ref, owner);
      case DECISION ->
          new DecisionStep(
              PlayerId.of(data.decider()),
              owner,
              ref,
              data.options().stream().map(Snapshots::option).toList());
      default -> throw new IllegalArgumentException("unknown step kind: " + data.kind());
    };
  }

  private static OptionData optionData(Option option) {
    return switch (option) {
      case ComponentOption c -> new OptionData(COMPONENT, c.component().value());
      case PlayerOption p -> new OptionData(PLAYER, p.player().value().toString());
    };
  }

  private static Option option(OptionData data) {
    return switch (data.kind()) {
      case COMPONENT -> new ComponentOption(ComponentId.of(data.value()));
      case PLAYER -> new PlayerOption(PlayerId.of(UUID.fromString(data.value())));
      default -> throw new IllegalArgumentException("unknown option kind: " + data.kind());
    };
  }

  private static Phase phase(String name) {
    try {
      return Phase.valueOf(name);
    } catch (IllegalArgumentException | NullPointerException e) {
      throw new IllegalArgumentException("unknown phase: " + name, e);
    }
  }
}
