# Agenda, Decisions and the Action Engine Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans (native, inline) to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Effects that can wait for a player's choice (B-EFF-3), a persisted agenda that survives between actions, and an `Engine` that accepts an `Answer`, resumes the paused effect and runs the agenda until it needs the next decision.

**Architecture:** `DuelState` gains an `Agenda`, a stack of `Step`s (top first): `EffectStep` (apply one effect) and `DecisionStep` (wait for a player). `Resolution` drains the agenda: it applies effect steps in order until the top is a decision or the agenda is empty. An effect that needs a choice stages a `DecisionStep` through `Resolution.decide`; the answer calls the same effect's `resume`, and the remaining effects of the source then continue in printed order (B-EFF-4). `Engine.apply` validates who may act and what they may answer, then returns the new state with the events. Everything is plain domain data and serialises through the snapshot layer.

**Tech Stack:** Java 21, JUnit 6, AssertJ; same build as plans 1–2.

**Spec:** `docs/design/core-mechanics.md` §4.5; rules `docs/rules/base.md` B-EFF-3, B-EFF-4, §10.1 (design point: pending decisions), B-AGE-0.

**Builds on:** plan 2 (`docs/implementation/02-effects-and-events.md`).

**Roadmap change (author's call, please check):** the original plan 3 (agenda, actions, phases) is split, because phases and the turn need the structure layouts, setup and the cost solver to mean anything. Plan 3 is this plan (agenda, decisions, engine). Plan 4: structure layouts, accessibility and reveal, setup, Wonder draft, Age start, turn pipeline with Discard, play-again and Age end (the starter choice is a decision). Plan 5: cost solver, Build and BuildWonder, military and science. Plan 6: views, quotes and GDK wiring.

## Global Constraints

- Package root `io.turnbase.games.sevenwondersduel`; new packages `agenda` and `engine`. Both are domain packages: they never import `snapshot`.
- Java 21 only; no I/O, clocks or randomness in this plan.
- `DuelState` stays immutable; `Agenda` is an immutable value; only `Resolution` replaces the state it holds.
- Format with `./mvnw -q -o spotless:apply` before every commit; run tests with `JAVA_HOME=$HOME/.sdkman/candidates/java/21.0.2-graalce`.
- Public types get Javadoc citing the rule they implement.
- Only the snapshot package is subject to the GDK serialization rules; the guard test there must keep passing with the new `StepData` and `OptionData` records.
- Out of scope: any `PlayerAction` other than `Answer` (plan 4 onward), decisions opened by triggers, a `DecisionOpened`/`DecisionAnswered` event (plan 6 decides what the UI needs), real choice effects (they arrive with card data; tests use fixtures).

## Review Focus

- A decision owned by the opponent is answered by the opponent, out of turn; the active player cannot answer it.
- Answering with an option that was not offered, answering when nothing is pending, and answering twice are all rejected, and a rejection leaves the input state untouched.
- State saved mid-decision (snapshot, JSON, back) resumes correctly: the remaining effects of the source still run after the answer.
- A pending step that names an effect the catalog no longer has, an index out of range, or an effect without `resume` fails with a clear error, not a null pointer.
- An effect with no feasible option opens no decision and the source's next effect still runs (B-EFF-3).
- Two decisions staged by one effect are answered in call order (so the effect can put the active player first, B-EFF-4).
- Draining while already draining, and a trigger asking for a decision, fail loudly.

---

### Task 1: Agenda model and its snapshot

**Files:**
- Create: `src/main/java/io/turnbase/games/sevenwondersduel/agenda/{Option,ComponentOption,PlayerOption,Step,EffectStep,DecisionStep,Agenda}.java`
- Create: `src/main/java/io/turnbase/games/sevenwondersduel/snapshot/{StepData,OptionData}.java`
- Modify: `state/DuelState.java` (new last component `Agenda agenda`, `withAgenda`), `SevenWondersDuelState.java` (new last component `List<StepData> agenda`), `snapshot/Snapshots.java`, and every caller of the `DuelState` / `SevenWondersDuelState` constructors (the compiler finds them; pass `Agenda.EMPTY` / `List.of()`).
- Test: `src/test/java/io/turnbase/games/sevenwondersduel/agenda/AgendaTest.java`, `snapshot/SnapshotsTest.java`

**Interfaces:**
- Produces:
  - `sealed interface Option permits ComponentOption, PlayerOption`; `record ComponentOption(ComponentId component)`; `record PlayerOption(PlayerId player)`.
  - `sealed interface Step permits EffectStep, DecisionStep`; `record EffectStep(EffectRef effect, PlayerId owner)`; `record DecisionStep(PlayerId owner, EffectRef effect, List<Option> options)` whose constructor copies the list and rejects an empty list and duplicate options (`IllegalArgumentException`).
  - `record Agenda(List<Step> steps)` (index 0 is the top, list copied) with `static final Agenda EMPTY`, `boolean isEmpty()`, `Step top()` (`IllegalStateException` when empty), `Agenda pop()`, `Agenda pushAll(List<Step> steps)` (the first element of `steps` becomes the new top, the rest of the agenda follows).
  - `DuelState.agenda()` and `DuelState.withAgenda(Agenda)`.
  - `record StepData(String kind, UUID owner, String source, int index, List<OptionData> options)` (kind `EFFECT` or `DECISION`; `options` empty for `EFFECT`) and `record OptionData(String kind, String value)` (kind `COMPONENT` or `PLAYER`; value is the component id or the player UUID text).

- [ ] **Step 1: Write the failing tests**

`AgendaTest`: `pushAllPutsFirstElementOnTop` (push `[a, b]` onto `[c]` gives top `a`, then `b`, then `c`); `popRemovesTopAndLeavesOriginalUntouched`; `topOfEmptyAgendaFails`; `decisionRejectsEmptyAndDuplicateOptions`; `agendaCopiesItsInput` (mutating the source list after construction changes nothing).

`SnapshotsTest`: extend `sample()` with an agenda of one `EffectStep` and one `DecisionStep` holding a `ComponentOption` and a `PlayerOption`; `domainSurvivesSnapshotRoundTrip` and `snapshotSurvivesJsonRoundTrip` must pass with it; add `unknownStepKindIsRejectedByName` (an `IllegalArgumentException` mentioning the kind). The existing guard test `snapshotTypesObeyGdkSerializationRules` must cover `StepData` and `OptionData` without change.

- [ ] **Step 2: Run them to see them fail** — `./mvnw -o test -Dtest='AgendaTest,SnapshotsTest'`. Expected: compilation failure (types missing).
- [ ] **Step 3: Implement** the types above. `Snapshots` writes steps in agenda order and reads them back with explicit kind switches (unknown kind names fail with the name in the message, like `phase`).
- [ ] **Step 4: Run the whole suite** — `./mvnw -o test`. Expected: all green (78 + 1 earlier tests plus the new ones).
- [ ] **Step 5: Commit** — `git add` the explicit paths; `feat(agenda): add the agenda model and its snapshot`.

---

### Task 2: Draining the agenda; decisions inside effects

**Files:**
- Modify: `effect/Effect.java` (new default `resume`), `effect/Resolution.java`
- Test: `src/test/java/io/turnbase/games/sevenwondersduel/effect/AgendaRunTest.java` (new); `InstantEffectsTest` must stay green unchanged.

**Interfaces:**
- Consumes: Task 1 types; `Resolution.within`, `EffectRef`, `Catalog.get`.
- Produces:
  - `Effect.resume(Resolution resolution, PlayerId owner, Option answer)`: default throws `IllegalStateException("effect takes no answer: …")`.
  - `Resolution.queue(List<Step> steps)`: puts the steps on the agenda, first element on top.
  - `Resolution.queueEffects(ComponentId source, PlayerId owner)`: queues one `EffectStep` per effect of `source`, printed order, index 0 on top.
  - `Resolution.decide(PlayerId decider, List<Option> options)`: valid only while an effect is applying or resuming. No options: does nothing (B-EFF-3). Otherwise stages `DecisionStep(decider, <the running effect's ref>, options)`; staged steps go on the agenda in call order when the running effect returns.
  - `Resolution.drain()`: applies `EffectStep`s from the top (each inside `within(ref, …)`, calling `catalog.get(ref.source()).effects().get(ref.index()).apply(this, owner)`) until the agenda is empty or its top is a `DecisionStep`. Throws `IllegalStateException` when called while already draining.
  - `Resolution.resume(Option answer)`: the top must be a `DecisionStep`; pops it, runs the named effect's `resume(this, step.owner(), answer)` inside `within(ref, …)`, puts staged steps on the agenda, then does not drain (the caller drains).
  - `resolveEffects(source, owner)` becomes `queueEffects` followed by `drain()`; its behaviour for effects without decisions is unchanged.
  - Class Javadoc of `Resolution` states: after an exception the instance is poisoned and must be discarded; the caller keeps the `DuelState` it started from (closes the plan-2 review note on partial failure).
  - A stale reference (source not in the catalog, index out of range) raises `IllegalStateException` naming the `EffectRef`.
  - `decide` and `queueEffects` called while a trigger reaction runs (`triggerDepth > 0`) throw `IllegalStateException("triggers cannot open decisions or queue effects")`.

- [ ] **Step 1: Write the failing tests** in `AgendaRunTest`, using a fixture effect `Choose` (in the test) whose `apply` calls `decide(owner, <ComponentOption for each component in zone "board">)` and whose `resume` moves the chosen component into `Fixtures.city(owner)`:
  - `effectsRunInPrintedOrderThroughTheAgenda`: card with effects `[GainCoins(1), GainCoins(2)]`, `resolveEffects` gives events `+1`, `+2` and an empty agenda.
  - `decisionPausesRemainingEffects`: card `[Choose, GainCoins(5)]` with two tokens on the board; after `resolveEffects` the coins are unchanged, the agenda is `[DecisionStep(owner, ref(card,0), [t1, t2]), EffectStep(ref(card,1), owner)]`.
  - `resumeAppliesAnswerThenRemainingEffectsFollow`: `resume(ComponentOption(t2))` then `drain()` moves `t2` to the owner's city and then applies `GainCoins(5)`; events carry source `ref(card,0)` then `ref(card,1)`.
  - `noFeasibleOptionOpensNoDecisionAndNextEffectRuns`: empty board, same card: agenda empty, coins +5 (B-EFF-3).
  - `stagedDecisionsAreAnsweredInCallOrder`: an effect that calls `decide(P2, …)` then `decide(P1, …)`: top of the agenda is P2's.
  - `effectWithoutResumeRejectsAnAnswer`, `staleReferenceFailsClearly` (agenda holds an `EffectStep` for an unknown component and for an index past the end; message contains the ref's source id), `drainWhileDrainingFails` (an effect whose `apply` calls `resolution.drain()`), `triggerCannotDecide` (a `CoinsChanged` trigger calling `decide` → `IllegalStateException`).

- [ ] **Step 2: Run them to see them fail** — `./mvnw -o test -Dtest=AgendaRunTest`. Expected: compilation failure (`queue`, `decide`, … missing).
- [ ] **Step 3: Implement** the methods above in `Resolution` (a `draining` flag, a `staged` list, and the staged steps flushed with `state.withAgenda(state.agenda().pushAll(staged))` after each `apply`/`resume`). `decide` finds the running effect through the top frame's `source`.
- [ ] **Step 4: Run the whole suite** — `./mvnw -o test`. Expected: all green, including the 79 earlier effect/event tests.
- [ ] **Step 5: Commit** — `feat(effect): drain the agenda and let effects wait for a decision (B-EFF-3, B-EFF-4)`.

---

### Task 3: `Engine` and `Answer`

**Files:**
- Create: `src/main/java/io/turnbase/games/sevenwondersduel/engine/{Engine,PlayerAction,Answer,Outcome,IllegalActionException}.java`
- Test: `src/test/java/io/turnbase/games/sevenwondersduel/engine/EngineTest.java`; reuse the `Fixtures` of the `effect` test package by moving the shared fixture class to a test-support package if needed (a ledgered ruling, not a copy).

**Interfaces:**
- Consumes: Task 2 `Resolution` API; `DuelState.agenda()`.
- Produces:
  - `sealed interface PlayerAction permits Answer`; `record Answer(Option choice)`.
  - `record Outcome(DuelState state, List<EventRecord> events)` (events copied).
  - `final class IllegalActionException extends RuntimeException` (message only).
  - `final class Engine` with `Engine(RuleSet rules)` and `Outcome apply(DuelState state, PlayerId actor, PlayerAction action)`:
    - `Answer`: rejected unless the agenda top is a `DecisionStep` ("no decision is pending"), `actor` is its owner ("not your decision"), and `choice` is one of its options ("not an offered option"). Then `Resolution.resume(choice)` and `drain()`; return the new state and the events.
    - The input state is never modified; a rejection throws before any `Resolution` exists.

- [ ] **Step 1: Write the failing tests** in `EngineTest` (the `Choose` fixture from Task 2 plus a second fixture effect that opens another decision on resume):
  - `ownerAnswersAndTheSourceContinues`: card `[Choose, GainCoins(5)]`, run `resolveEffects` via a `Resolution`, then `engine.apply(resolution.state(), P1, new Answer(...))` returns coins +5, the chosen token in the owner's city, an empty agenda, events with the right sources.
  - `opponentOwnedDecisionIsAnsweredByTheOpponent`: a decision owned by P2 while P1 is active: P2 may answer, P1 gets `IllegalActionException` ("not your decision").
  - `answerWithoutPendingDecisionIsRejected` and `answeringTwiceIsRejected`.
  - `answerOutsideTheOfferedOptionsIsRejected`: state unchanged (`isEqualTo` the input).
  - `answerThatOpensAnotherDecisionStopsAgain`: the outcome's agenda top is the new `DecisionStep`.
  - `pausedGameSurvivesTheSnapshot`: take the state with a pending decision, `Snapshots.toSnapshot` → `GdkSerialization` JSON → `fromSnapshot`, then answer; the result equals answering the original state.
- [ ] **Step 2: Run them to see them fail** — `./mvnw -o test -Dtest=EngineTest`. Expected: compilation failure.
- [ ] **Step 3: Implement** `Engine.apply`.
- [ ] **Step 4: Run `./mvnw -o verify`.** Expected: BUILD SUCCESS, all tests green, enforcer and spotless clean.
- [ ] **Step 5: Commit** — `feat(engine): add the Engine and the Answer action`; then add `03-agenda-and-decisions.md` to `docs/INDEX.md` (the implementation row) and commit it with the plan.

---

## Self-review

- **Spec coverage:** §4.5 one agenda with `EffectStep`/`DecisionStep`, owner may be the opponent, `Answer` resumes (Tasks 1–3); depth-first, front-pushed follow-ups (Task 2: `queue` and staged steps); B-EFF-3 (Task 2); B-EFF-4 printed order and decisions for both players, active first (Task 2, staged call order); §5a out-of-turn answers validated by the game (Task 3). `Phase`s, `PlayerAction`s other than `Answer`, and `ChooseStarter` are plan 4; the starter choice will need a `DecisionStep` that resumes a flow rather than an effect, which plan 4 adds as a second kind of resume target.
- **Rulings made while planning:** one feasible option still asks (the rule says feasible options are *offered*); triggers cannot open decisions (no base-game trigger needs it; the trigger frame's `EffectRef` indexes triggers, not effects, so a resumed decision would be ambiguous).
- **Type consistency:** `EffectRef(source, index)` identifies the effect in both `EffectStep` and `DecisionStep`; `Option` is the only answer type; `Outcome.events` is the plan-2 `EventRecord` list.
