# Foundation Implementation Plan (plan 1 of 4)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** The value-type foundation of the game: seeded RNG cursor, data-driven zones with a terminal `destroyed`, the component catalog with content packs, and the serializable game state.

**Architecture:** State is one immutable record of `String`/`UUID` ids; zones, components and rules are registered by `ContentPack`s and assembled into a `RuleSet`. Pure functions move components between zones against the registry. Nothing here knows a card or a rule of 7 Wonders Duel except the base pack's zone list.

**Tech Stack:** Java 21, JUnit 6 + AssertJ, `turnbase-serialization` (test scope) for round trips, Maven (`./mvnw`).

**Spec:** `docs/superpowers/specs/2026-10-24-core-mechanics-design.md` (§3, §4.1–4.3). Rules: `docs/rules/base.md` §10.4, §10.8.

**Roadmap:** this plan → (2) effects, modifiers, event bus → (3) agenda, actions, phases → (4) cost solver, views, events, quotes. Each is planned when the previous one is done.

## Global Constraints

- Package root `io.turnbase.games.sevenwondersduel`; sub-packages `rng`, `zone`, `catalog`, `state`.
- Java 21 only; no I/O, no clocks, no `Math.random()`, no unseeded `Random` (`backend-gdk-api.md` §9).
- State types: records, `List`, `Set`, `Map`, `String`, `UUID`, enums, boxed numerics. **No interface-, abstract- or `Object`-typed field.** `Map` keys are `String`, `UUID`, enums or boxed integrals only. Ordered data is a `List`.
- Ids inside state are `String` (components, zones, packs) and `UUID` (players).
- Depends on `turnbase-sdk` only (provided scope); `turnbase-serialization` is test scope.
- Format with `./mvnw spotless:apply` before every commit (google-java-format).
- Public types get Javadoc stating the rule they implement, citing rule IDs from `docs/rules/` where one applies.

## Review Focus

- Moving a component out of `destroyed` is rejected, from any code path (B-ACT-5; design §4.2).
- Placing the same component twice, moving an unknown component, or moving into an unknown zone fails loudly, never silently.
- Two packs declaring the same zone id or the same component id fail assembly with a message naming both packs.
- Shuffling an empty and a one-element list; an RNG cursor survives a JSON round trip and continues the same sequence.
- A state with empty collections, a player at 0 coins, and `playAgain` set on one player only round-trips equal.

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
  - `cursorSurvivesJsonRoundTrip`: serialize `RngCursor.start(7)` advanced twice with `new GdkSerialization().writeState(c, Long.MAX_VALUE)` / `readState(json, RngCursor.class)`; equal, and the next draw from the restored cursor equals the next draw from the original.
- [ ] **Step 2: Run** `./mvnw -q test -Dtest=RngTest` — expect compilation failure (types missing).
- [ ] **Step 3: Implement** both types. `nextInt` builds `new Random(seed)`, discards `draws` calls to `nextInt()`, then returns `nextInt(bound)` with `draws + 1`. `shuffle` is Fisher–Yates from the end using `nextInt(i + 1)`, threading the cursor.
- [ ] **Step 4: Run** `./mvnw -q test -Dtest=RngTest` — expect PASS.
- [ ] **Step 5: Commit**
  ```bash
  ./mvnw -q spotless:apply
  git add src/main/java/io/turnbase/games/sevenwondersduel/rng src/test/java/io/turnbase/games/sevenwondersduel/rng
  git commit -m "feat(rng): seeded cursor with deterministic draws and shuffle" -- src/main/java/io/turnbase/games/sevenwondersduel/rng src/test/java/io/turnbase/games/sevenwondersduel/rng
  ```

### Task 2: Zone model and registry

**Files:**
- Create: `src/main/java/io/turnbase/games/sevenwondersduel/zone/{Visibility,ZoneKind,Location,Placement,ZoneRegistry}.java`
- Test: `src/test/java/io/turnbase/games/sevenwondersduel/zone/ZoneRegistryTest.java`

**Interfaces:**
- Produces:
  - `enum Visibility { PUBLIC, OWNER, NONE }` — who may see a component's identity in the zone.
  - `record ZoneKind(String id, Visibility visibility, boolean ordered, boolean terminal)`.
  - `record Location(String zone, String key)` — e.g. `("city", "<player uuid>")`, `("structure.up", "3:4")`. `static Location of(String zone)` uses key `""`.
  - `record Placement(String componentId, Location at)`.
  - `final class ZoneRegistry`: `static ZoneRegistry of(List<ZoneKind> kinds)`; `ZoneKind kind(String zoneId)` (throws `IllegalArgumentException` naming the id if unknown); `boolean knows(String zoneId)`. Duplicate zone ids throw `IllegalArgumentException` naming the id.

- [ ] **Step 1: Write the failing tests** in `ZoneRegistryTest`: lookup returns the registered kind; unknown id throws with the id in the message; duplicate id throws; `knows` true/false.
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
  - `List<Placement> place(List<Placement> all, String id, Location at, ZoneRegistry zones)` — adds a component not yet placed; throws `IllegalStateException` if already placed, `IllegalArgumentException` if the zone is unknown.
  - `List<Placement> move(List<Placement> all, String id, Location to, ZoneRegistry zones)` — throws `IllegalArgumentException` if the component is not placed or the target zone is unknown, `IllegalStateException` if the source zone is `terminal`. The moved component goes to the end of the list (order within an ordered zone = list order).
  - `Location locationOf(List<Placement> all, String id)` (throws if absent).
  - `List<String> idsIn(List<Placement> all, String zone)` and `List<String> idsIn(List<Placement> all, Location at)`, in list order.

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
  - `interface Component { String id(); String name(); ComponentType type(); }`.
  - `final class Catalog`: `Component get(String id)` (throws `IllegalArgumentException` naming the id), `List<Component> ofType(ComponentType t)` (registration order).
  - `interface ContentPack { String id(); List<ZoneKind> zoneKinds(); List<Component> components(); }`.
  - `final class RuleSet`: `static RuleSet assemble(List<ContentPack> packs)`; accessors `ZoneRegistry zones()`, `Catalog catalog()`, `List<String> packIds()`. A duplicate zone id or component id across packs throws `IllegalArgumentException` whose message names both packs and the id.

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

### Task 5: Game state value

**Files:**
- Modify: `src/main/java/io/turnbase/games/sevenwondersduel/SevenWondersDuelState.java` (currently an empty record)
- Create: `src/main/java/io/turnbase/games/sevenwondersduel/state/{Phase,PlayerState}.java`
- Test: `src/test/java/io/turnbase/games/sevenwondersduel/SevenWondersDuelStateTest.java`

**Interfaces:**
- Consumes: `Placement`, `RngCursor`.
- Produces:
  - `enum Phase { SETUP, WONDER_DRAFT, AGE, SCORING, OVER }`.
  - `record PlayerState(int coins, boolean playAgain)` with `withCoins(int)` and `withPlayAgain(boolean)`.
  - `record SevenWondersDuelState(Phase phase, UUID active, List<Placement> placements, Map<UUID, PlayerState> players, Map<UUID, Set<String>> knowledge, RngCursor rng, List<String> packs) implements GameState`, with `player(UUID)` (throws if absent), `opponentOf(UUID)` (two-player game: the other key; throws otherwise), and `withPhase`, `withActive`, `withPlacements`, `withPlayer(UUID, PlayerState)`, `withKnowledge(UUID, Set<String>)`, `withRng`. Withers return new instances; the compact constructor copies collections immutably.
- Later plans add `board`, `military` and `agenda` fields with their own tasks.

- [ ] **Step 1: Write the failing tests** in `SevenWondersDuelStateTest`:
  - `roundTripsThroughJson`: a state with two players (7 and 0 coins), `playAgain` true on one only, two placements, a knowledge set, an advanced RNG cursor and `packs = ["base"]`; `writeState`/`readState` yields an equal value.
  - `emptyCollectionsRoundTrip`
  - `opponentOfReturnsTheOtherSeat`
  - `playAgainIsPerPlayer`: `withPlayer(a, a.withPlayAgain(true))` leaves the opponent's flag false.
  - `withersDoNotMutate`
  - `stateHasNoInterfaceTypedFieldsOrNonKeyableMapKeys`: reflect over the record components of every record under `state`, `zone`, `rng` and the state class; fail on interface/abstract/`Object` component types (excluding `List`, `Set`, `Map` and their type arguments, which are checked recursively) and on `Map` key types other than `String`/`UUID`/enum/boxed integral. This is the `backend-gdk-api.md` §5.1 guard.
- [ ] **Step 2: Run** `./mvnw -q test -Dtest=SevenWondersDuelStateTest` — expect compilation failure.
- [ ] **Step 3: Implement** the records.
- [ ] **Step 4: Run** the test — expect PASS.
- [ ] **Step 5: Commit** (`feat(state): serializable game state with per-player flags`). Pathspec: only the files of this task.

### Task 6: Base pack zones and an end-to-end foundation test

**Files:**
- Create: `src/main/java/io/turnbase/games/sevenwondersduel/catalog/BasePack.java`
- Test: `src/test/java/io/turnbase/games/sevenwondersduel/catalog/BasePackTest.java`

**Interfaces:**
- Consumes: Tasks 2–5.
- Produces `final class BasePack implements ContentPack` with id `"base"`, no components yet, and these `ZoneKind`s (`id`, visibility, ordered, terminal):

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
  - `boxedComponentCanBeDrawnButDestroyedCannot`: place a fixture token in `box`, `move` to `board` succeeds; place a Wonder in `destroyed`, `move` to `wonders.unbuilt` throws (B-ACT-5).
  - `faceDownCardRevealsByMove`: `structure.down` → `structure.up` keeps the key.
  - `endToEndStateSurvivesSerialization`: build a `SevenWondersDuelState` whose placements come from `Placements.place`/`move` over the base registry, round-trip it through JSON, and assert `Placements.idsIn` gives the same answers on the restored value.
- [ ] **Step 2: Run** `./mvnw -q test -Dtest=BasePackTest` — expect compilation failure.
- [ ] **Step 3: Implement** `BasePack` from the table above; no card data.
- [ ] **Step 4: Run the whole suite** `./mvnw -q verify` — expect BUILD SUCCESS (tests, spotless, enforcer, coverage gate).
- [ ] **Step 5: Commit** (`feat(catalog): base pack zone kinds`).

---

## Self-review

- **Spec coverage:** §4.1 state (Task 5; board/military/agenda deferred to plans 2–3), §4.2 zones with `box`/`destroyed` and terminal enforcement (Tasks 2, 3, 6), §4.3 catalog and packs (Task 4), §3 serialization constraints (Task 5 guard), RNG cursor (Task 1). §4.4–4.8 belong to plans 2–4.
- **Type consistency:** `Placement`/`Location`/`ZoneRegistry` are defined in Task 2 and used unchanged in Tasks 3, 5, 6; `RuleSet.zones()` returns the registry Task 3 consumes.
- **Decision made here:** face-down structure cards are modelled as a separate zone `structure.down`, so visibility stays a per-zone property and "reveal" is an ordinary move.
