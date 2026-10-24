# Build, Build Wonder and the Turn End After a Decision — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans (native, inline). Steps use checkbox (`- [ ]`) syntax.

**Goal:** The two remaining main actions of a turn — construct the Building and construct a Wonder (B-ACT-1, B-ACT-3, B-ACT-4, B-ACT-5) — paid with the plan 5a quote, with the turn ending only after every effect and decision they open has been settled (B-TURN-2, B-TURN-3).

**Architecture:** An action that can open decisions puts a `TurnEndStep` at the bottom of the agenda before its effects. `Resolution.drain` already stops at any step that is not an effect, so decisions and effects stack above it; `Turn.settle` drains, and when the top is the `TurnEndStep` it pops it and runs `Turn.end`. `Engine.answer` settles the same way, so a turn ends after the last answered decision with no turn-in-progress flag: the agenda is the continuation and survives a snapshot. `turn/Construct` holds the two paid actions; `WonderLimit` holds the 7-Wonder rule as a count.

**Tech Stack:** Java 21, JUnit 6, AssertJ; same build as plans 1–5a.

**Spec:** `docs/rules/base.md` §6 (B-TURN-2..6, B-ACT-1, B-ACT-3..5), §4 (via the 5a `CostSolver`).

**Builds on:** 4c (`Turn.end`, Discard), 5a (`CostSolver.quote`). Decided with the user: split of plan 5 into 5a–5d. Out of scope: military (5c), science and progress tokens (5d), the payee hook for Economy-style effects (a progress token, 5d), Easter Island and expansion Wonders (the limit is a count so they are additions), scoring.

## Global Constraints

- Same packages and formatting rules as 5a. New code in `turn/` and `engine/`; `WonderLimit` in `turn/`.
- Zones: Wonders sit in `wonders.unbuilt` then `wonders.built`, keyed by the owner's UUID text; the tucked Age card goes to `underWonder` keyed by the Wonder's id value; Destroyed Wonders go to `destroyed` (terminal) keyed by the former owner's UUID text.
- The price is `CostSolver.quote`: coins plus trade go to the bank (`changeCoins(actor, -total)`); a chained build pays nothing. Building a Wonder prices the Wonder, never the Age card (B-ACT-3).
- Rejections throw `IllegalActionException` before any `Resolution` exists. Legality and its order for both actions: the Discard order (phase, starter chosen, nothing pending, active player, card takeable), then action-specific checks.
- A turn ends exactly once, after the action's effects and decisions are done. Discard keeps its direct `Turn.end`.

## Review Focus

- A decision opened by a built card or Wonder (answered by either player) ends the turn when the last one is answered — not before, not twice — and a chain of decisions still ends it once.
- A build that opens a decision leaves the state restorable: a snapshot with the `TurnEndStep` at the bottom round-trips and the turn still ends after the answer.
- A build the actor cannot afford, a Wonder not in the actor's `wonders.unbuilt`, a card not takeable, and a Wonder construction at the limit are all rejected with the state unchanged and no coins lost.
- The 7th constructed Wonder destroys every remaining unbuilt Wonder at once, for both players, before its own effects resolve; nothing can be built after that, and a Destroyed Wonder never returns (B-ACT-4, B-ACT-5).
- A Wonder's effects resolve for its owner; the tucked Age card has no effect and is not in the city (B-ACT-3); a built card's effects are resolved after it is in the city, so its own triggers and modifiers already apply (B-EFF-1).
- A "play again" granted by a built card or Wonder is honoured after the reveal; one granted on the turn that empties the structure is lost (B-TURN-5, B-TURN-6).
- Paying never takes the treasury below zero and quoting does not change the state.

---

### Task 1: The turn end as an agenda step

**Files:** Create `agenda/TurnEndStep.java` (`record TurnEndStep() implements Step`); modify `agenda/Step.java` (permits), `snapshot/Snapshots.java` (kind `TURN_END`, both directions), `turn/Turn.java` (`settle`), `engine/Engine.java` (`answer` settles). Test: `turn/SettleTest`, `snapshot/SnapshotsTest`, `engine/EngineTest`.

**Interfaces:** `Turn.settle(Resolution)`: drains the agenda; if it is then non-empty with a `TurnEndStep` on top it pops the step (`advance`) and calls `end`; any other top (a decision) is left for the player. `Engine.answer` replaces its `drain` by `Turn.settle`. A decision answered outside a turn (no `TurnEndStep` below) still just drains.

- [ ] Failing tests: from a started Age one with agenda `[TurnEndStep]`, `settle` hands the move over; with `[DecisionStep, TurnEndStep]`, `settle` leaves both and a following answer through the engine ends the turn; with no `TurnEndStep` settle never changes the active player; snapshot round-trip of an agenda holding the step. Red → green → commit `feat(turn): the turn ends after the last answered decision (B-TURN-3)`.

### Task 2: Construct the Building

**Files:** Create `engine/Build.java` (`record Build(ComponentId card)`), `turn/Construct.java`; modify `PlayerAction`, `Engine` (holds a `CostSolver`; the shared take check is extracted from `discard`). Test: `turn/BuildTest`.

**Interfaces:** `Construct.building(Resolution, CostSolver, PlayerId actor, ComponentId card)`: quotes, pays, moves the card to the actor's city, pushes `TurnEndStep`, queues the card's effects, `Turn.settle`. `Engine` rejects with "cannot afford" when `!quote.payableWith(coins)`.

- [ ] Failing tests with fixture cards that carry cost, effects and modifiers: a free card is built and the turn passes; a priced card pays quote total (including trade at the opponent-raised price) and a chained one pays nothing; unaffordable rejected with state unchanged; effects resolve after placement (a card whose effect pays coins and a held trigger on it see the order); a card whose effect opens a decision ends the turn only after the answer (Review Focus 1–2); play again honoured, lost on the emptying turn. Red → green → commit `feat(turn): construct the Building (B-ACT-1)`.

### Task 3: Construct a Wonder

**Files:** Create `engine/BuildWonder.java` (`record BuildWonder(ComponentId card, ComponentId wonder)`), `turn/WonderLimit.java`; modify `Construct`, `PlayerAction`, `Engine`. Test: `turn/BuildWonderTest`, `turn/WonderLimitTest`.

**Interfaces:** `WonderLimit.MAX = 7`; `WonderLimit.built(DuelState) : int` (components in `wonders.built` of either player); `WonderLimit.allows(DuelState) : boolean` (`built < MAX`); `WonderLimit.enforce(Resolution)`: when `built >= MAX`, moves every unbuilt Wonder to `destroyed`. `Construct.wonder(Resolution, CostSolver, PlayerId, ComponentId card, ComponentId wonder)`: prices the Wonder, pays, moves the card under the Wonder and the Wonder to the actor's `wonders.built`, enforces the limit, pushes `TurnEndStep`, queues the Wonder's effects, settles.

- [ ] Failing tests: the Wonder's cost is charged and the card's cost ignored; the card ends in `underWonder` keyed by the Wonder and is not in the city; the Wonder's effects resolve for the owner; a Wonder not in the actor's unbuilt zone, an unaffordable one, and any construction with 7 built are rejected, state unchanged; the 7th destroys all unbuilt Wonders of both players before its effects; two Wonders built by the same player in successive turns. Red → green → commit `feat(turn): construct a Wonder and the seven-Wonder limit (B-ACT-3..5)`.
