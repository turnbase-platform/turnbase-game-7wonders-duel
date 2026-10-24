package io.turnbase.games.sevenwondersduel.snapshot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.turnbase.games.sevenwondersduel.SevenWondersDuelState;
import io.turnbase.games.sevenwondersduel.agenda.Agenda;
import io.turnbase.games.sevenwondersduel.agenda.ComponentOption;
import io.turnbase.games.sevenwondersduel.agenda.DecisionStep;
import io.turnbase.games.sevenwondersduel.agenda.EffectStep;
import io.turnbase.games.sevenwondersduel.agenda.PlayerOption;
import io.turnbase.games.sevenwondersduel.event.EffectRef;
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
import io.turnbase.serialization.GdkSerialization;
import java.io.IOException;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class SnapshotsTest {

  private static final PlayerId P1 =
      PlayerId.of(UUID.fromString("00000000-0000-0000-0000-000000000001"));
  private static final PlayerId P2 =
      PlayerId.of(UUID.fromString("00000000-0000-0000-0000-000000000002"));

  private static DuelState sample() {
    return new DuelState(
        Phase.AGE,
        P2,
        List.of(
            new Placement(ComponentId.of("c1"), new Location(ZoneId.of("city"), "p1")),
            new Placement(ComponentId.of("c2"), Location.of(ZoneId.of("deck")))),
        Map.of(P1, new PlayerState(7, false), P2, new PlayerState(0, true)),
        Map.of(P1, Set.of(ComponentId.of("c2"), ComponentId.of("c1")), P2, Set.of()),
        new RngCursor(99, 12),
        List.of(PackId.of("base")),
        new Agenda(
            List.of(
                new EffectStep(new EffectRef(ComponentId.of("c1"), 1), P1),
                new DecisionStep(
                    P2,
                    new EffectRef(ComponentId.of("c2"), 0),
                    List.of(new ComponentOption(ComponentId.of("t1")), new PlayerOption(P1))))));
  }

  @Test
  void domainSurvivesSnapshotRoundTrip() {
    DuelState original = sample();

    assertThat(Snapshots.fromSnapshot(Snapshots.toSnapshot(original))).isEqualTo(original);
  }

  @Test
  void snapshotSurvivesJsonRoundTrip() {
    GdkSerialization serialization = new GdkSerialization();
    SevenWondersDuelState snapshot = Snapshots.toSnapshot(sample());

    String json = serialization.writeState(snapshot, Long.MAX_VALUE);
    SevenWondersDuelState read = serialization.readState(json, SevenWondersDuelState.class);

    assertThat(read).isEqualTo(snapshot);
    assertThat(Snapshots.fromSnapshot(read)).isEqualTo(sample());
  }

  @Test
  void emptyCollectionsRoundTrip() {
    DuelState empty =
        sample().withPlacements(List.of()).withKnowledge(P1, Set.of()).withRng(RngCursor.start(0));
    DuelState noPacks =
        new DuelState(
            empty.phase(),
            empty.active(),
            empty.placements(),
            empty.players(),
            empty.knowledge(),
            empty.rng(),
            List.of(),
            empty.agenda());

    assertThat(Snapshots.fromSnapshot(Snapshots.toSnapshot(noPacks))).isEqualTo(noPacks);
  }

  @Test
  void knowledgeIsWrittenSorted() {
    Set<ComponentId> ascending =
        new LinkedHashSet<>(List.of(ComponentId.of("a"), ComponentId.of("b")));
    Set<ComponentId> descending =
        new LinkedHashSet<>(List.of(ComponentId.of("b"), ComponentId.of("a")));

    SevenWondersDuelState first = Snapshots.toSnapshot(sample().withKnowledge(P1, ascending));
    SevenWondersDuelState second = Snapshots.toSnapshot(sample().withKnowledge(P1, descending));

    assertThat(first).isEqualTo(second);
    assertThat(first.knowledge().get(P1.value())).containsExactly("a", "b");
  }

  @Test
  void unknownPhaseAndBlankIdAreRejectedByName() {
    SevenWondersDuelState good = Snapshots.toSnapshot(sample());

    SevenWondersDuelState badPhase =
        new SevenWondersDuelState(
            "NOT_A_PHASE",
            good.active(),
            good.placements(),
            good.players(),
            good.knowledge(),
            good.rngSeed(),
            good.rngDraws(),
            good.packs(),
            good.agenda());
    SevenWondersDuelState blankId =
        new SevenWondersDuelState(
            good.phase(),
            good.active(),
            List.of(new PlacementData(" ", "deck", "")),
            good.players(),
            good.knowledge(),
            good.rngSeed(),
            good.rngDraws(),
            good.packs(),
            good.agenda());

    assertThatThrownBy(() -> Snapshots.fromSnapshot(badPhase))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("NOT_A_PHASE");
    assertThatThrownBy(() -> Snapshots.fromSnapshot(blankId))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void unknownStepKindIsRejectedByName() {
    SevenWondersDuelState good = Snapshots.toSnapshot(sample());
    SevenWondersDuelState bad =
        new SevenWondersDuelState(
            good.phase(),
            good.active(),
            good.placements(),
            good.players(),
            good.knowledge(),
            good.rngSeed(),
            good.rngDraws(),
            good.packs(),
            List.of(new StepData("NOT_A_KIND", P1.value(), "c1", 0, List.of())));

    assertThatThrownBy(() -> Snapshots.fromSnapshot(bad))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("NOT_A_KIND");
  }

  @Test
  void snapshotTypesObeyGdkSerializationRules() throws Exception {
    List<Class<?>> records = new ArrayList<>(List.of(SevenWondersDuelState.class));
    Path dir = Path.of("target/classes/io/turnbase/games/sevenwondersduel/snapshot");
    try (Stream<Path> files = Files.list(dir)) {
      for (Path file : files.filter(f -> f.toString().endsWith(".class")).toList()) {
        String name = file.getFileName().toString().replace(".class", "");
        records.add(Class.forName("io.turnbase.games.sevenwondersduel.snapshot." + name));
      }
    }

    assertThat(records).hasSizeGreaterThanOrEqualTo(4);
    for (Class<?> type : records) {
      if (type == Snapshots.class) {
        continue;
      }
      assertThat(type.isRecord()).as("%s must be a record", type.getName()).isTrue();
      for (RecordComponent component : type.getRecordComponents()) {
        assertGdkSafe(component.getGenericType(), type.getSimpleName() + "." + component.getName());
      }
    }
  }

  private static void assertGdkSafe(Type type, String where) {
    if (type instanceof ParameterizedType parameterized) {
      Class<?> raw = (Class<?>) parameterized.getRawType();
      Type[] args = parameterized.getActualTypeArguments();
      if (Map.class.isAssignableFrom(raw)) {
        Class<?> key = (Class<?>) args[0];
        assertThat(
                key == String.class
                    || key == UUID.class
                    || key.isEnum()
                    || Set.of(Integer.class, Long.class, Short.class, Byte.class).contains(key))
            .as("%s: Map key %s", where, key.getName())
            .isTrue();
      }
      for (Type arg : args) {
        assertGdkSafe(arg, where);
      }
    } else if (type instanceof GenericArrayType) {
      throw new AssertionError(where + ": arrays are not allowed");
    } else {
      Class<?> cls = (Class<?>) type;
      if (cls.isPrimitive()) {
        return;
      }
      boolean container = cls == List.class || cls == Map.class;
      assertThat(cls != Object.class && (container || !cls.isInterface()))
          .as("%s: %s is Object or an interface", where, cls.getName())
          .isTrue();
      assertThat(java.lang.reflect.Modifier.isAbstract(cls.getModifiers()) && !container)
          .as("%s: %s is abstract", where, cls.getName())
          .isFalse();
    }
  }

  @Test
  void domainPackagesDoNotImportSnapshot() throws IOException {
    Path root = Path.of("src/main/java/io/turnbase/games/sevenwondersduel");
    for (String pkg : List.of("id", "rng", "zone", "catalog", "state")) {
      try (Stream<Path> files = Files.walk(root.resolve(pkg))) {
        for (Path file : files.filter(f -> f.toString().endsWith(".java")).toList()) {
          assertThat(Files.readString(file))
              .as("%s must not reference the snapshot layer", file)
              .doesNotContain("sevenwondersduel.snapshot");
        }
      }
    }
  }
}
