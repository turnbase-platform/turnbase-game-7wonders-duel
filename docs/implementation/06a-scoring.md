# Scoring Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans (native, inline). Steps use checkbox (`- [ ]`) syntax.

**Goal:** When the last card of Age III is taken the game is scored and ends: civilian score, tie-break on blue VP, shared victory (B-END-1..3, B-MIL-5).

**Architecture:** Scores are derived, never stored. (1) A component's VP is a static `Points` modifier — a fixed number or a function of the player's city (`ScoreContext`) — so guilds, Mathematics and expansion scoring are data, not core edits. (2) `Scoring.score(state, rules, player)` returns a `Scorecard`: named lines (`military`, `blue`, `buildings`, `wonders`, `progress`, `coins`), each a sum; the line of a component's points follows its tag/type (`Scoring.lineOf`). (3) `Scoring.conclude(resolution)` compares the two scorecards (total, then the `blue` line), and `declareVictory`s the winner with `Reason.POINTS`, or a shared victory (`winner == null`, allowed for `POINTS` only). `AgeEnd.finish` calls it after the last Age instead of parking in `Phase.SCORING`, which is removed.

**Tech Stack:** Java 21, JUnit 6, AssertJ; same build as plans 1–5e.

**Spec:** `docs/rules/base.md` §11 (B-END-1..3), B-MIL-5, §3.6 (guild city choice), §3.8 (Mathematics, Philosophy, Agriculture).

**Builds on:** 5c (`MilitaryTrack`, `declareVictory`), 5d (`Tag.PROGRESS`), 4c (`AgeEnd`). Decided with the user: points are a modifier on the component; a shared win is `Victory(null, POINTS)`.

**Out of scope:** the card/Wonder/token VP data (catalog data later); guild "city with most" composition helpers beyond `ScoreContext.count`; the per-viewer score display (plan 6b) and `GameOutcome` (plan 6c); Agora/Pantheon score lines (additions through `Points` and `lineOf`).

## Global Constraints

- New code in `scoring/` (`Points`, `ScoreContext`, `Scorecard`, `Scoring`), `state/Victory` (reason `POINTS`), `military/MilitaryTrack` (VP table), `age/AgeEnd`. Same formatting and package rules.
- Lines, in order: `military`, `blue` (cards tagged `Tag.BLUE`), `buildings` (other cards), `wonders`, `progress` (tokens), `coins` (`⌊treasury / 3⌋`). Only live components count (built Wonders, cards and tokens in the city).
- Military VP (B-MIL-5): the player the pawn is advanced toward the opponent scores by the zone of its space: zone 1 → 2, zone 2 → 5, zone 3 → 10, centre → 0; the capital is a victory and never scored.
- Winner: higher total; equal totals → higher `blue` line; still equal → shared victory.
- `Victory`: a null winner is legal only with `Reason.POINTS`; the snapshot maps it (`victoryWinner` null with `victoryReason` set).
- `Tag.BLUE = "color:blue"` follows `Tag.YELLOW`/`RED`.

## Review Focus

- A player behind on total but ahead on nothing else loses; equal totals go to blue VP; equal blue is a shared victory, not an error.
- Points from components outside the city (unbuilt Wonders, cards under a Wonder, discarded, destroyed) do not count; a stolen/moved component scores for its current owner.
- Military VP goes to the player the pawn leans toward, 0 at the centre, and not to the other player.
- Treasury VP rounds down; a computed `Points` sees the owner's current city only.
- The game ends exactly once when Age III's structure empties: further actions are rejected, a play-again flag on that turn is moot.
- A snapshot of a shared win round-trips.

---

### Task 1: A shared win is a Victory

**Files:** modify `state/Victory.java` (`Reason.POINTS`; winner required except for `POINTS`), `snapshot/Snapshots.java` (map a null winner; reject a null winner without `POINTS`), `catalog/Tag.java` (`BLUE`). Test: `state/VictoryTest`, `snapshot/SnapshotsTest`.

- [ ] Failing tests: `new Victory(null, POINTS)` is legal; `new Victory(null, MILITARY)` and a null reason are rejected; a shared win round-trips through the snapshot with a null winner and `POINTS`. Red → green → commit `feat(game): a game can end in a shared victory (B-END-3)`.

### Task 2: Component points

**Files:** create `scoring/Points.java` (`interface Points extends Modifier { int points(ScoreContext ctx); static Points fixed(int n) }`), `scoring/ScoreContext.java` (`record ScoreContext(DuelState state, RuleSet rules, PlayerId owner)`; `int count(PlayerId player, Tag tag)` counts live components with the tag in that player's city), `scoring/Scorecard.java` (`record Scorecard(PlayerId player, Map<String,Integer> lines)`; `line(String)` 0 if absent; `total()`), `scoring/Scoring.java` (`score(DuelState, RuleSet, PlayerId) : Scorecard` with the component lines; `lineOf(Component) : String`). Test: `scoring/ScoringTest`.

**Interfaces:** `Scoring.lineOf`: type `WONDER` → `wonders`, `TOKEN` → `progress`, a `CARD` tagged `Tag.BLUE` → `blue`, other `CARD` → `buildings`.

- [ ] Failing tests with fixture parts: fixed points land in the line of their component; computed points (3 per `Tag.PROGRESS` component in the city, Mathematics-style) see the owner's city; points of components not in the city (deck, discard, under a Wonder, unbuilt Wonder) are ignored; two modifiers on one component add. Red → green → commit `feat(scoring): components score through Points modifiers (B-END-2)`.

### Task 3: Military and treasury lines

**Files:** modify `military/MilitaryTrack.java` (`victoryPoints(int zone) : int`: 2, 5, 10 for zones 1–3, else 0), `scoring/Scoring.java` (`military` and `coins` lines). Test: `scoring/ScoringTest`, `military/MilitaryTrackTest`.

- [ ] Failing tests: VP per zone including 0 at the centre and zone 4 never scored; the player the pawn leans toward gets the line and the other gets 0; `coins` is ⌊coins/3⌋ (8 → 2, 9 → 3, 2 → 0). Red → green → commit `feat(scoring): military and treasury victory points (B-MIL-5, B-END-2)`.

### Task 4: The game is scored when the last Age ends

**Files:** modify `scoring/Scoring.java` (`conclude(Resolution)`), `age/AgeEnd.java` (call it instead of `withPhase(SCORING)`), `state/Phase.java` (remove `SCORING`), `age/AgeEndTest` (the scoring test now expects `OVER`). Test: `scoring/ConcludeTest` through `Engine` with the last card of Age III.

- [ ] Failing tests: higher total wins with `POINTS`; equal totals → more `blue`; equal both → shared victory; the final state is `OVER`, actions are rejected; the agenda is empty. Red → green → `verify` → commit `feat(scoring): the game is scored after Age III (B-END-1..3)`.
