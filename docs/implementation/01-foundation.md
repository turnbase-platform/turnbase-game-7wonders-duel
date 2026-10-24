# Foundation Implementation Plan (plan 1 of 4)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** The foundation of the game: seeded RNG cursor, typed ids, data-driven zones with a terminal `destroyed`, the component catalog with content packs, the domain game state, and a snapshot layer that maps it to a GDK-compatible state.

**Architecture:** The domain model is ordinary immutable OOP with typed ids; it is not shaped by the GDK's serialization rules. `SevenWondersDuelState` is a flat, serializer-safe snapshot and the only type that crosses the platform boundary; a `Snapshots` mapper converts both ways losslessly. Zones, components and rules are registered by `ContentPack`s and assembled into a `RuleSet`. Nothing here knows a card or a rule of 7 Wonders Duel except the base pack's zone list.

**Tech Stack:** Java 21, JUnit 6 + AssertJ, `turnbase-serialization` (test scope) for round trips, Maven (`./mvnw`).

**Spec:** `docs/design/core-mechanics.md` (§3, §4.1–4.3). Rules: `docs/rules/base.md` §10.4, §10.8.

**Roadmap:** this plan → (2) effects, modifiers, event bus → (3) agenda, actions, phases → (4) cost solver, views, events, quotes. Each is planned when the previous one is done.

## Global Constraints

- Package root `io.turnbase.games.sevenwondersduel`; sub-packages `id`, `rng`, `zone`, `catalog`, `state`, `snapshot`.
- Java 21 only; no I/O, no clocks, no `Math.random()`, no unseeded `Random` (`backend-gdk-api.md` §9).
- Domain types (`id`, `rng`, `zone`, `catalog`, `state`) are free OOP: typed ids, sealed interfaces and rich collections are allowed. They never import `snapshot`.
- Only `SevenWondersDuelState` and the other types in `snapshot` are subject to `backend-gdk-api.md` §5.1: records, `List`, `Map` with `String`/`UUID`/enum keys, `String`, `UUID`, enums, boxed numerics; **no interface-, abstract- or `Object`-typed field**; ordered data is a `List`.
- Mapping is lossless and deterministic: `fromSnapshot(toSnapshot(x)).equals(x)`, and unordered sets are written sorted.
- Depends on `turnbase-sdk` only (provided scope); `turnbase-serialization` is test scope.
- Format with `./mvnw spotless:apply` before every commit (google-java-format).
- Public types get Javadoc stating the rule they implement, citing rule IDs from `docs/rules/` where one applies.

## Review Focus

- Moving a component out of `destroyed` is rejected, from any code path (B-ACT-5; design §4.2).
- Placing the same component twice, moving an unknown component, or moving into an unknown zone fails loudly, never silently.
- Two packs declaring the same zone id or the same component id fail assembly with a message naming both packs.
- Shuffling an empty and a one-element list; an RNG cursor survives a JSON round trip and continues the same sequence.
- A state with empty collections, a player at 0 coins, and `playAgain` set on one player only survives domain → snapshot → JSON → snapshot → domain equal; a snapshot referencing an unknown phase or a malformed id fails with a message naming it.

---

### Task 1: Seeded RNG cursor

**Files:**
- Create: `src/main/java/io/turnbase/games/sevenwondersduel/rng/RngCursor.java`
- Create: `src/main/java/io/turnbase/games/sevenwondersduel/rng/Rng.java`
- Test: `src/test/java/io/turnbase/games/sevenwondersduel/rng/RngTest.java`

**Interfaces:**
- Produces:
  - `record RngCursor(long seed, long draws)` with `static RngCursor start(long seed)`.
  - `record Drawn<T>(T value, RngCursor next)` (nested in `Rng`).
  - `final class Rng` with `static Drawn<Integer> nextInt(RngCursor c, int bound)` and `static <T> Drawn<List<T>> shuffle(List<T> items, RngCursor c)` (returns a new list; input untouched).

- [ ] **Step 1: Write the failing tests** in `RngTest`:
  - `sameCursorGivesSameDraw`: `nextInt(start(42), 100)` twice → equal values and equal `next`.
  - `drawAdvancesCursor`: `next.draws()` is `draws + 1`; chaining 3 draws gives `draws == 3` and the 3 values equal drawing them from one `new java.util.Random(42)` sequence via `nextInt(100)`.
  - `shuffleIsAPermutationAndDeterministic`: shuffle of `List.of(1..20)` sorted equals the input; same cursor → same order; input list unchanged.
  - `shuffleEdgeCases`: empty and one-element lists return equal lists and leave the cursor unchanged for size ≤ 1.
  - `cursorContinuesAfterRebuildFromSeedAndDraws`: advance `RngCursor.start(7)` twice, rebuild `new RngCursor(c.seed(), c.draws())`; the next draw equals the next draw from the original (this is what the snapshot stores).
- [ ] **Step 2: Run** `./mvnw -q test -Dtest=RngTest` — expect compilation failure (types missing).
- [ ] **Step 3: Implement** both types. `nextInt` builds `new Random(seed)`, discards `draws` calls to `nextInt()`, then returns `nextInt(bound)` with `draws + 1`. `shuffle` is Fisher–Yates from the end using `nextInt(i + 1)`, threading the cursor.
- [ ] **Step 4: Run** `./mvnw -q test -Dtest=RngTest` — expect PASS.
- [ ] **Step 5: Commit**
  ```bash
  ./mvnw -q spotless:apply
  git add src/main/java/io/turnbase/games/sevenwondersduel/rng src/test/java/io/turnbase/games/sevenwondersduel/rng
  git commit -m "feat(rng): seeded cursor with deterministic draws and shuffle" -- src/main/java/io/turnbase/games/sevenwondersduel/rng src/test/java/io/turnbase/games/sevenwondersduel/rng
  ```

### Task 2: Typed ids, zone model and registry

**Files:**
- Create: `src/main/java/io/turnbase/games/sevenwondersduel/id/{ComponentId,ZoneId,PlayerId,PackId}.java`
- Create: `src/main/java/io/turnbase/games/sevenwondersduel/zone/{Visibility,ZoneKind,Location,Placement,ZoneRegistry}.java`
- Test: `src/test/java/io/turnbase/games/sevenwondersduel/zone/ZoneRegistryTest.java`, `.../id/IdsTest.java`

**Interfaces:**
- Produces:
  - `record ComponentId(String value)`, `record ZoneId(String value)`, `record PackId(String value)` — each rejects null/blank in its compact constructor; `record PlayerId(UUID value)` rejects null. Each has a static `of(...)`.
  - `enum Visibility { PUBLIC, OWNER, NONE }` — who may see a component's identity in the zone.
  - `record ZoneKind(ZoneId id, Visibility visibility, boolean ordered, boolean terminal)`.
  - `record Location(ZoneId zone, String key)` — e.g. `(city, "<player uuid>")`, `(structure.up, "3:4")`. `static Location of(ZoneId zone)` uses key `""`.
  - `record Placement(ComponentId component, Location at)`.
  - `final class ZoneRegistry`: `static ZoneRegistry of(List<ZoneKind> kinds)`; `ZoneKind kind(ZoneId id)` (throws `IllegalArgumentException` naming the id if unknown); `boolean knows(ZoneId id)`. Duplicate zone ids throw `IllegalArgumentException` naming the id.

- [ ] **Step 1: Write the failing tests.** `IdsTest`: blank/null rejected, equal values are equal. `ZoneRegistryTest`: lookup returns the registered kind; unknown id throws with the id in the message; duplicate id throws; `knows` true/false.
- [ ] **Step 2: Run** `./mvnw -q test -Dtest=ZoneRegistryTest` — expect compilation failure.
- [ ] **Step 3: Implement** the types; the registry holds an unmodifiable `Map<String, ZoneKind>`.
- [ ] **Step 4: Run** the test — expect PASS.
- [ ] **Step 5: Commit** (`feat(zone): data-driven zone kinds and registry`; add only the `zone` main and test directories, same pathspec pattern as Task 1).

### Task 3: Placement operations

**Files:**
- Create: `src/main/java/io/turnbase/games/sevenwondersduel/zone/Placements.java`
- Test: `src/test/java/io/turnbase/games/sevenwondersduel/zone/PlacementsTest.java`

**Interfaces:**
- Consumes: Task 2 types.
- Produces `final class Placements` (static, pure; the list is the state's `List<Placement>`):
  - `List<Placement> place(List<Placement> all, ComponentId id, Location at, ZoneRegistry zones)` — adds a component not yet placed; throws `IllegalStateException` if already placed, `IllegalArgumentException` if the zone is unknown.
  - `List<Placement> move(List<Placement> all, ComponentId id, Location to, ZoneRegistry zones)` — throws `IllegalArgumentException` if the component is not placed or the target zone is unknown, `IllegalStateException` if the source zone is `terminal`. The moved component goes to the end of the list (order within an ordered zone = list order).
  - `Location locationOf(List<Placement> all, ComponentId id)` (throws if absent).
  - `List<ComponentId> idsIn(List<Placement> all, ZoneId zone)` and `List<ComponentId> idsIn(List<Placement> all, Location at)`, in list order.

- [ ] **Step 1: Write the failing tests** in `PlacementsTest` using a registry with zones `deck`, `city` (non-terminal) and `destroyed` (terminal):
  - `placeAddsAndRejectsDuplicates`
  - `moveChangesLocationAndKeepsOtherPlacements`
  - `moveAppendsToEndOfOrderedZone`: two ids in `deck`, move the first away and back → it is now last.
  - `moveOutOfTerminalZoneIsRejected`: place in `destroyed`, `move` to `city` → `IllegalStateException`; list unchanged.
  - `moveUnknownComponentAndUnknownZoneFail`
  - `idsInFiltersByZoneAndByLocation`: `idsIn(all, "city")` spans keys; `idsIn(all, new Location("city","p1"))` does not.
  - `operationsDoNotMutateTheInput`.
- [ ] **Step 2: Run** `./mvnw -q test -Dtest=PlacementsTest` — expect compilation failure.
- [ ] **Step 3: Implement** with immutable list copies (`List.copyOf`).
- [ ] **Step 4: Run** the test — expect PASS.
- [ ] **Step 5: Commit** (`feat(zone): pure place/move with terminal-zone enforcement`).

### Task 4: Catalog, content packs and rule set

**Files:**
- Create: `src/main/java/io/turnbase/games/sevenwondersduel/catalog/{ComponentType,Component,Catalog,ContentPack,RuleSet}.java`
- Test: `src/test/java/io/turnbase/games/sevenwondersduel/catalog/RuleSetTest.java`

**Interfaces:**
- Consumes: `ZoneKind`, `ZoneRegistry`.
- Produces:
  - `enum ComponentType { CARD, WONDER, TOKEN }` (later plans add more).
  - `interface Component { ComponentId id(); String name(); ComponentType type(); }`.
  - `final class Catalog`: `Component get(ComponentId id)` (throws `IllegalArgumentException` naming the id), `List<Component> ofType(ComponentType t)` (registration order).
  - `interface ContentPack { PackId id(); List<ZoneKind> zoneKinds(); List<Component> components(); }`.
  - `final class RuleSet`: `static RuleSet assemble(List<ContentPack> packs)`; accessors `ZoneRegistry zones()`, `Catalog catalog()`, `List<PackId> packIds()`. A duplicate zone id or component id across packs throws `IllegalArgumentException` whose message names both packs and the id.

- [ ] **Step 1: Write the failing tests** in `RuleSetTest` with two small test-local `ContentPack` records:
  - `assembleMergesZonesAndComponents`
  - `catalogLooksUpById` and `catalogFiltersByTypeInRegistrationOrder`
  - `duplicateZoneAcrossPacksNamesBothPacksAndId`
  - `duplicateComponentAcrossPacksNamesBothPacksAndId`
  - `unknownComponentIdThrowsWithId`
- [ ] **Step 2: Run** `./mvnw -q test -Dtest=RuleSetTest` — expect compilation failure.
- [ ] **Step 3: Implement.** Merge in pack order; track the owning pack per zone/component id to build the error message.
- [ ] **Step 4: Run** the test — expect PASS.
- [ ] **Step 5: Commit** (`feat(catalog): component catalog, content packs and rule set assembly`).

### Task 5: Domain game state

**Files:**
- Create: `src/main/java/io/turnbase/games/sevenwondersduel/state/{Phase,PlayerState,DuelState}.java`
- Test: `src/test/java/io/turnbase/games/sevenwondersduel/state/DuelStateTest.java`

**Interfaces:**
- Consumes: `PlayerId`, `ComponentId`, `PackId`, `Placement`, `RngCursor`.
- Produces:
  - `enum Phase { SETUP, WONDER_DRAFT, AGE, SCORING, OVER }`.
  - `record PlayerState(int coins, boolean playAgain)` with `withCoins(int)` and `withPlayAgain(boolean)`; negative coins rejected.
  - `record DuelState(Phase phase, PlayerId active, List<Placement> placements, Map<PlayerId, PlayerState> players, Map<PlayerId, Set<ComponentId>> knowledge, RngCursor rng, List<PackId> packs)`. Compact constructor copies collections immutably and rejects `players` not of size 2 and an `active` that is not a key of `players`. Accessors/withers: `player(PlayerId)` (throws if absent), `opponentOf(PlayerId)`, `withPhase`, `withActive`, `withPlacements`, `withPlayer(PlayerId, PlayerState)`, `withKnowledge(PlayerId, Set<ComponentId>)`, `withRng`.
- Later plans add `board`, `military` and `agenda` with their own tasks.

- [ ] **Step 1: Write the failing tests** in `DuelStateTest`:
  - `opponentOfReturnsTheOtherSeat` and `opponentOfUnknownPlayerThrows`
  - `playAgainIsPerPlayer`: setting it on one player leaves the opponent's flag false.
  - `withersDoNotMutateTheOriginal`
  - `constructorRejectsActiveOutsideTheTwoSeats` and `negativeCoinsRejected`
  - `collectionsAreDefensivelyCopied`: mutating the list passed in does not change the state.
- [ ] **Step 2: Run** `./mvnw -q test -Dtest=DuelStateTest` — expect compilation failure.
- [ ] **Step 3: Implement** the records.
- [ ] **Step 4: Run** the test — expect PASS.
- [ ] **Step 5: Commit** (`feat(state): domain game state with per-player flags`; this task's files only).

### Task 6: GDK snapshot layer

**Files:**
- Modify: `src/main/java/io/turnbase/games/sevenwondersduel/SevenWondersDuelState.java` (currently an empty record)
- Create: `src/main/java/io/turnbase/games/sevenwondersduel/snapshot/{PlacementData,PlayerData,Snapshots}.java`
- Test: `src/test/java/io/turnbase/games/sevenwondersduel/snapshot/SnapshotsTest.java`

**Interfaces:**
- Consumes: `DuelState` and everything it holds.
- Produces (all GDK-safe per `backend-gdk-api.md` §5.1):
  - `record PlacementData(String component, String zone, String key)`.
  - `record PlayerData(int coins, boolean playAgain)`.
  - `record SevenWondersDuelState(String phase, UUID active, List<PlacementData> placements, Map<UUID, PlayerData> players, Map<UUID, List<String>> knowledge, long rngSeed, long rngDraws, List<String> packs) implements GameState` — knowledge lists are written sorted.
  - `final class Snapshots`: `static SevenWondersDuelState toSnapshot(DuelState)` and `static DuelState fromSnapshot(SevenWondersDuelState)`. `fromSnapshot` throws `IllegalArgumentException` naming the offending value for an unknown phase or a blank id.

- [ ] **Step 1: Write the failing tests** in `SnapshotsTest`:
  - `domainSurvivesSnapshotRoundTrip`: a `DuelState` with two players (7 and 0 coins), `playAgain` on one only, two placements, a knowledge set, an advanced RNG cursor and packs `[base]`; `fromSnapshot(toSnapshot(x))` equals `x`.
  - `snapshotSurvivesJsonRoundTrip`: `new GdkSerialization().writeState(snapshot, Long.MAX_VALUE)` then `readState(json, SevenWondersDuelState.class)` equals the snapshot, and mapping back equals the domain state.
  - `emptyCollectionsRoundTrip`
  - `knowledgeIsWrittenSorted`: the same set in different insertion orders yields equal snapshots.
  - `unknownPhaseAndBlankIdAreRejectedByName`
  - `snapshotTypesObeyGdkSerializationRules`: reflect over the record components of every record in the `snapshot` package and `SevenWondersDuelState`; fail on interface/abstract/`Object` component types (checking `List`/`Map` type arguments recursively) and on `Map` key types other than `String`/`UUID`/enum/boxed integral. This guard is the only place the GDK rules are enforced.
  - `domainPackagesDoNotImportSnapshot`: scan the sources (or use ArchUnit-free reflection over class dependencies) so `id`, `rng`, `zone`, `catalog` and `state` never reference `snapshot`.
- [ ] **Step 2: Run** `./mvnw -q test -Dtest=SnapshotsTest` — expect compilation failure.
- [ ] **Step 3: Implement** the records and mapper.
- [ ] **Step 4: Run** the test — expect PASS.
- [ ] **Step 5: Commit** (`feat(snapshot): GDK-compatible state and lossless mapper`; this task's files only).

### Task 7: Base pack zones and an end-to-end foundation test

**Files:**
- Create: `src/main/java/io/turnbase/games/sevenwondersduel/catalog/BasePack.java`
- Test: `src/test/java/io/turnbase/games/sevenwondersduel/catalog/BasePackTest.java`

**Interfaces:**
- Consumes: Tasks 2–6.
- Produces `final class BasePack implements ContentPack` with id `base`, no components yet, and these `ZoneKind`s (id, visibility, ordered, terminal):

  | id | visibility | ordered | terminal | note |
  |---|---|---|---|---|
  | `deck` | NONE | yes | no | key = Age |
  | `structure.up` | PUBLIC | no | no | key = `row:x` |
  | `structure.down` | NONE | no | no | face-down; reveal = move to `structure.up` (B-STR-2) |
  | `city` | PUBLIC | no | no | key = player UUID |
  | `underWonder` | NONE | no | no | key = Wonder id (B-ACT-3) |
  | `discard` | PUBLIC | yes | no | |
  | `draft` | PUBLIC | no | no | Wonders on offer |
  | `wonders.unbuilt` | PUBLIC | no | no | key = player UUID |
  | `wonders.built` | PUBLIC | no | no | key = player UUID |
  | `board` | PUBLIC | no | no | progress tokens on the board |
  | `box` | NONE | no | no | removed at setup; draw pools are queries (design §4.2) |
  | `destroyed` | PUBLIC | no | **yes** | permanent removal in play |

- [ ] **Step 1: Write the failing tests** in `BasePackTest`:
  - `ruleSetAssemblesAndKnowsEveryZone`: all 12 ids resolve.
  - `onlyDestroyedIsTerminal`.
  - `boxedComponentCanBeDrawnButDestroyedCannot`: a fixture token in `box` moves to `board`; a fixture Wonder in `destroyed` cannot move to `wonders.unbuilt` (B-ACT-5).
  - `faceDownCardRevealsByMove`: `structure.down` → `structure.up` keeps the key.
  - `endToEndThroughSnapshot`: build a `DuelState` over the base registry with `Placements.place`/`move`, map it through `Snapshots` and JSON, and assert `Placements.idsIn` gives the same answers on the restored state.
- [ ] **Step 2: Run** `./mvnw -q test -Dtest=BasePackTest` — expect compilation failure.
- [ ] **Step 3: Implement** `BasePack` from the table; no card data.
- [ ] **Step 4: Run the whole suite** `./mvnw -q verify` — expect BUILD SUCCESS (tests, spotless, enforcer, coverage gate).
- [ ] **Step 5: Commit** (`feat(catalog): base pack zone kinds`).

---

## Self-review

- **Spec coverage:** §3 domain/snapshot split (Tasks 5–6), §4.1 state (Task 5; board/military/agenda deferred to plans 2–3), §4.2 zones with `box`/`destroyed` and terminal enforcement (Tasks 2, 3, 7), §4.3 catalog and packs (Task 4), RNG cursor (Task 1). §4.4–4.8 belong to plans 2–4.
- **Type consistency:** typed ids from Task 2 are used unchanged in Tasks 3–7; `RuleSet.zones()` returns the registry Task 3 consumes; `Snapshots` is the only code that sees both `DuelState` and `SevenWondersDuelState`.
- **Decision made here:** face-down structure cards are a separate zone `structure.down`, so visibility stays a per-zone property and "reveal" is an ordinary move.
