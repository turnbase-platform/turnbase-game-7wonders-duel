# Effects, Modifiers and Event Bus Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans (native, inline) to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Instant effects that change state through one `Resolution`, a causation-tree event stream, ownership-derived modifier lookup, and triggers dispatched on an event bus (active player first).

**Architecture:** `Resolution` is the engine's working object for one player action: it holds the current `DuelState`, applies state changes through a handful of primitive operations (`move`, `changeCoins`, `setPlayAgain`), and records every change as a `GameEvent` with `id`, `parentId` and `source`, forming the causation tree (design §4.4, §4.8). Each recorded event is offered to the triggers of components that are *live* for a player, active player first (B-EFF-4). Modifiers are only *collected* here (ownership query); their narrow interfaces arrive with the cost solver in plan 4.

**Tech Stack:** Java 21, JUnit 6, AssertJ; same build as plan 1.

**Spec:** `docs/design/core-mechanics.md` §4.1, §4.4, §4.8 (events); rules `docs/rules/base.md` B-EFF-1…4, §10.3.

**Builds on:** plan 1 (`docs/implementation/01-foundation.md`), all merged on game-repo `master`.

## Global Constraints

- Package root `io.turnbase.games.sevenwondersduel`; new packages `event` and `effect`. Both are domain packages: they never import `snapshot`.
- Java 21 only; no I/O, clocks or randomness in this plan (nothing here draws from the RNG).
- Events and effects are plain domain types (sealed interfaces and records are fine); they are not part of the GDK state and are not serialized in this plan.
- `DuelState` stays immutable; only `Resolution` mutates (it replaces its held state value).
- Format with `./mvnw -q -o spotless:apply` before every commit; run tests with `JAVA_HOME=$HOME/.sdkman/candidates/java/21.0.2-graalce`.
- Public types get Javadoc citing the rule they implement.
- Out of scope (later plans): the agenda and decisions (plan 3), military/progress/science state and effects, the narrow modifier interfaces and cost solver (plan 4), card data.

## Review Focus

- A trigger whose effect re-triggers itself (loop): the engine must stop with a clear error, not overflow the stack.
- A trigger's source leaving play mid-dispatch (moved out of a live zone by an earlier trigger): it must not react after that.
- B-EFF-1: an instant effect's result survives its source later moving to `destroyed` (nothing is undone).
- B-EFF-2: losing or taking more coins than the treasury holds moves exactly the treasury; taker gains exactly what the victim lost.
- Event ids are unique and `parentId` always names an earlier event (or none); the `events()` list handed out cannot be mutated.
- Trigger ordering when the owner is the active player vs the opponent; a component live for neither player (e.g. in `discard`) never reacts.

---

### Task 1: Events, causation and `Resolution` primitives

**Files:**
- Create: `src/main/java/io/turnbase/games/sevenwondersduel/event/{GameEvent,ComponentMoved,CoinsChanged,PlayAgainChanged,EffectRef,EventRecord}.java`
- Create: `src/main/java/io/turnbase/games/sevenwondersduel/effect/Resolution.java`
- Test: `src/test/java/io/turnbase/games/sevenwondersduel/effect/ResolutionTest.java`

**Interfaces:**
- Consumes: `DuelState`, `PlayerState`, `Placements`, `RuleSet`, `Location`, `ComponentId`, `PlayerId`.
- Produces:
  - `sealed interface GameEvent permits ComponentMoved, CoinsChanged, PlayAgainChanged`.
  - `record ComponentMoved(ComponentId component, Location from, Location to) implements GameEvent`.
  - `record CoinsChanged(PlayerId player, int delta) implements GameEvent` (delta is never 0 when emitted).
  - `record PlayAgainChanged(PlayerId player, boolean granted) implements GameEvent`.
  - `record EffectRef(ComponentId source, int index)`; `EffectRef.ROOT`-less: a root cause (the player's own action) is `null` source, modelled as `Optional<EffectRef>` in `EventRecord`.
  - `record EventRecord(int id, int parentId, Optional<EffectRef> source, GameEvent event)`; `parentId` is `-1` for a root event.
  - `final class Resolution`: `Resolution(DuelState state, RuleSet rules)`; `DuelState state()`; `RuleSet rules()`; `List<EventRecord> events()` (unmodifiable view, emission order); `void move(ComponentId, Location to)`; `void changeCoins(PlayerId, int delta)` (delta 0 emits nothing; a result below zero throws `IllegalStateException` naming the player); `void setPlayAgain(PlayerId, boolean)` (emits only on change); `void within(EffectRef source, Runnable body)` — events emitted inside `body` carry `source` and, unless already nested, `parentId` of the currently handled event.

- [ ] **Step 1: Write the failing tests** in `ResolutionTest` (use `RuleSet.assemble(List.of(new BasePack()))` and a two-player `DuelState` helper):
  - `moveUpdatesStateAndEmitsComponentMovedWithFromAndTo`
  - `moveOutOfTerminalZoneThrowsAndEmitsNothing`
  - `changeCoinsUpdatesStateAndEmitsDelta`; `zeroDeltaEmitsNothing`; `coinsBelowZeroThrowsNamingPlayerAndLeavesStateUnchanged`
  - `setPlayAgainEmitsOnlyOnChange`
  - `eventsGetSequentialIdsStartingAtZeroAndRootParentIsMinusOne`
  - `eventsInsideWithinCarryTheSource`
  - `eventsListIsUnmodifiable`
- [ ] **Step 2: Run** `./mvnw -q -o test -Dtest=ResolutionTest` — expect compilation failure.
- [ ] **Step 3: Implement** the event types and `Resolution`. `Resolution` keeps `DuelState state`, an `ArrayList<EventRecord>`, and a small frame stack `(Optional<EffectRef> source, int parentId)`; `emit(GameEvent)` is private for now (Task 4 hooks the bus into it).
- [ ] **Step 4: Run** the test — expect PASS, then the whole suite.
- [ ] **Step 5: Commit** (`feat(effect): resolution primitives and causation-tracked events`).

### Task 2: Live zones, component behaviour and ownership queries

**Files:**
- Modify: `zone/ZoneKind.java` (add component `boolean live`; keep the existing 4-argument constructor, defaulting `live` to `false`), `catalog/Component.java`, `catalog/BasePack.java`
- Create: `src/main/java/io/turnbase/games/sevenwondersduel/effect/{Effect,Modifier,Trigger,Ownership}.java`
- Test: `src/test/java/io/turnbase/games/sevenwondersduel/effect/OwnershipTest.java`, extend `zone/ZoneRegistryTest` only if needed

**Interfaces:**
- Consumes: Task 1, `Placements`, `RuleSet`.
- Produces:
  - `ZoneKind.live`: "components here are in play for the player named by the location key; their modifiers and triggers are active". Base: `city` and `wonders.built` are live; all others are not. The key of a live zone is the player's UUID string (`PlayerId.value().toString()`).
  - `interface Effect { void apply(Resolution resolution, PlayerId owner); }`.
  - `interface Modifier {}` (marker; plan 4 adds the narrow sub-interfaces).
  - `interface Trigger<E extends GameEvent> { Class<E> eventType(); void react(E event, PlayerId owner, Resolution resolution); }`.
  - `Component` gains `default List<Effect> effects()`, `default List<Modifier> modifiers()`, `default List<Trigger<?>> triggers()`, all empty by default.
  - `final class Ownership`: `static List<ComponentId> liveComponents(DuelState, ZoneRegistry, PlayerId)` (placement order, across every live zone whose key is that player); `static <M extends Modifier> List<M> modifiers(DuelState, RuleSet, PlayerId, Class<M>)` (components' `modifiers()` filtered by type, in component placement order).

- [ ] **Step 1: Write the failing tests** in `OwnershipTest` with test-local `Component` records and a local `ContentPack` (zones `city` live, `discard` not live):
  - `liveComponentsAreThoseInLiveZonesKeyedByThePlayer`
  - `componentInDiscardOrInOpponentsCityIsNotLive`
  - `modifiersAreCollectedByTypeFromLiveComponentsOnly`
  - `modifiersFollowTheComponentWhenItMoves` (move a modifier card from my city to the opponent's: my list loses it, theirs gains it — B-EFF-1's "static effects follow the source")
  - `baseZonesMarkCityAndBuiltWondersLive` (against `BasePack`: exactly `city` and `wonders.built` are live)
- [ ] **Step 2: Run** `./mvnw -q -o test -Dtest=OwnershipTest` — expect compilation failure.
- [ ] **Step 3: Implement** the changes above; `BasePack` uses a `zone(...)` helper overload with a `live` argument.
- [ ] **Step 4: Run** the test, then the whole suite — expect PASS (existing tests keep compiling through the 4-argument constructor).
- [ ] **Step 5: Commit** (`feat(effect): live zones, component behaviour and ownership queries`).

### Task 3: Instant effects and in-order resolution

**Files:**
- Create: `src/main/java/io/turnbase/games/sevenwondersduel/effect/{GainCoins,LoseCoins,TakeCoins,GrantPlayAgain}.java`
- Modify: `effect/Resolution.java` (add `resolveEffects`)
- Test: `src/test/java/io/turnbase/games/sevenwondersduel/effect/InstantEffectsTest.java`

**Interfaces:**
- Consumes: Tasks 1–2.
- Produces:
  - `record GainCoins(int amount) implements Effect` — owner gains `amount` (B-EFF-2 "gain").
  - `record LoseCoins(int amount) implements Effect` — the **opponent** of the owner loses `min(amount, treasury)` to the bank (Loot-style).
  - `record TakeCoins(int amount) implements Effect` — owner takes `min(amount, opponent treasury)` from the opponent; the owner gains exactly what the opponent lost, as two `CoinsChanged` events, victim first.
  - `record GrantPlayAgain() implements Effect` — sets the **owner's** `playAgain` flag (B-TURN-6). A separate target-the-opponent variant is added with the Shwedagon Pagoda data, not now.
  - `Resolution.resolveEffects(ComponentId source, PlayerId owner)`: applies `catalog.get(source).effects()` in printed order, each inside `within(new EffectRef(source, index), …)`.
  - Amounts must be positive; constructors throw `IllegalArgumentException` otherwise.

- [ ] **Step 1: Write the failing tests** in `InstantEffectsTest`:
  - `gainCoinsAddsToOwner`
  - `loseCoinsTakesAtMostTheOpponentsTreasuryToTheBank` (opponent has 2, effect 5 → opponent 0, owner unchanged, one event of −2)
  - `takeCoinsMovesExactlyWhatTheVictimHad` (victim 3, take 5 → owner +3, victim 0; events victim −3 then owner +3)
  - `takeCoinsFromEmptyTreasuryDoesNothingAndEmitsNothing`
  - `effectsResolveInPrintedOrderWithIndexedSources` (card with `[GainCoins(2), GainCoins(3)]`: events carry `EffectRef(card,0)` then `(card,1)`)
  - `instantResultsSurviveTheSourceBeingDestroyed` (B-EFF-1: resolve, move the card to `destroyed`, coins unchanged)
  - `nonPositiveAmountsAreRejected`
- [ ] **Step 2: Run** `./mvnw -q -o test -Dtest=InstantEffectsTest` — expect compilation failure.
- [ ] **Step 3: Implement.** Effects only use `Resolution` primitives; no effect touches `DuelState` directly.
- [ ] **Step 4: Run** the test, then the whole suite — expect PASS.
- [ ] **Step 5: Commit** (`feat(effect): instant coin and play-again effects with ordered resolution`).

### Task 4: Event bus — triggers, ordering and causation

**Files:**
- Modify: `effect/Resolution.java` (dispatch inside `emit`)
- Test: `src/test/java/io/turnbase/games/sevenwondersduel/effect/TriggerBusTest.java`

**Interfaces:**
- Consumes: Tasks 1–3.
- Produces (behaviour of `Resolution.emit`): after recording an event `E` (id `n`), the bus offers `E` to triggers whose `eventType()` is assignable from `E`'s class. Candidates are the live components of the **active player** first, then the **opponent** (B-EFF-4), each in placement order, each component's triggers in declaration order. The candidate list is computed when `E` is emitted; before each reaction the component is re-checked to still be live for the same player, so a source that has left play does not react. A reaction runs inside `within(new EffectRef(component, triggerIndex), …)` with `parentId = n`, so what it emits is a child of `E`. Dispatch is depth-first. Nesting deeper than 32 levels throws `IllegalStateException("trigger chain too deep …")`.

- [ ] **Step 1: Write the failing tests** in `TriggerBusTest` (test-local components with `Trigger`s that record into a list and/or call `resolution.changeCoins`):
  - `liveTriggerReactsToMatchingEventOnly` (a `CoinsChanged` trigger ignores a `ComponentMoved`)
  - `triggerInNonLiveZoneNeverReacts`
  - `activePlayersTriggersRunBeforeOpponents` — run twice with each player active
  - `reactionEventsAreChildrenOfTheTriggeringEventWithTriggerSource` (check `parentId` and `source`)
  - `chainedTriggersFormATreeDepthFirst` (A's reaction emits an event that triggers B before A's second trigger runs)
  - `triggerLeavingPlayMidDispatchDoesNotReact` (first trigger moves the second component to `discard`; the second never fires)
  - `selfRetriggeringTriggerStopsWithClearError`
  - `endToEndUrbanismStyleCausationTree`: card `Gift` with `[GainCoins(2)]` moved into the city; a live `Tax` card has a `CoinsChanged` trigger that takes 1 coin from the opponent; assert the full `(id, parentId, source, event)` list.
- [ ] **Step 2: Run** `./mvnw -q -o test -Dtest=TriggerBusTest` — expect FAIL (no dispatch yet).
- [ ] **Step 3: Implement** dispatch in `Resolution.emit`; keep `events()` unmodifiable.
- [ ] **Step 4: Run** the whole suite and `./mvnw -q -o verify` — expect BUILD SUCCESS.
- [ ] **Step 5: Commit** (`feat(effect): event bus with active-player-first triggers and causation tree`).

---

## Self-review

- **Spec coverage:** §4.4 instant, modifier (collection only — narrow interfaces deliberately deferred to plan 4 with the solver that defines their signatures), trigger rows; §4.8 event `id`/`parentId`/`source`; B-EFF-1 (Task 3), B-EFF-2 (Task 3), B-EFF-4 ordering (Task 4), B-EFF-3 needs choices and therefore the agenda (plan 3).
- **Type consistency:** `Resolution.within`/`EffectRef` introduced in Task 1 are the only causation API used by Tasks 3–4; `Effect.apply(Resolution, PlayerId)` is the one effect signature; `Ownership.liveComponents` (Task 2) is what Task 4's bus uses.
- **Decisions made here (author calls, please check):**
  - Triggers fire **immediately and depth-first** on emit, which matches design §4.5 ("follow-ups pushed to the front: deterministic, depth-first, printed order"); plan 3's agenda keeps the same order for effects that push steps.
  - `ZoneKind.live` is a flag on the zone kind, not a hard-coded list, so a pack can declare its own in-play zones.
  - `Component` gets default-empty `effects()/modifiers()/triggers()` so existing fixtures and `BasePack` need no change.
  - The 32-level nesting cap is a guard against runaway pack data, not a rule.
