# Military Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans (native, inline). Steps use checkbox (`- [ ]`) syntax.

**Goal:** Shields move the military pawn one space at a time, military tokens fire when their zone is entered, and a pawn landing on a capital ends the game with a military victory (B-MIL-1..4, B-END-1).

**Architecture:** Three small parts. (1) The end of a game is a value: a nullable `Victory(winner, reason)` on `DuelState`, set by `Resolution.declareVictory`, which also sets `Phase.OVER` and clears the agenda; `Engine` rejects every action once the game is over. (2) `Resolution.setPawn` is the one primitive that moves the pawn (event `PawnMoved`), like `changeCoins` for treasuries. (3) `military/Military.advance(resolution, mover, shields)` holds the rules: step by step toward the opponent's capital, collect the military tokens of each zone entered, queue their effects as ordinary steps (the token is a `TOKEN` component whose effect is `LoseCoins`) and move them to `destroyed`, and declare victory when the capital is reached. `Shields(n)` is the instant effect that calls it.

**Tech Stack:** Java 21, JUnit 6, AssertJ; same build as plans 1–5b.

**Spec:** `docs/rules/base.md` §8 (B-MIL-1..4), §3.9 (tokens), §10.5 (path model), B-END-1.

**Builds on:** 4a/4b (`Pawn`, zones), 3 (`queue`, agenda), 5b (`Build` to drive shields end to end). Decided with the user: tokens are components in a track zone; the end is a `Victory` field plus `Phase.OVER`.

**Out of scope:** the Strategy token's +1 shield on red cards and Economy-style hooks (progress tokens, 5d); end-of-game military VP (B-MIL-5, scoring plan); placing the four tokens at setup (B-SET-2) — the base pack ships no components, and a military token needs a tag no data yet defines, so setup waits for the catalog data (tests place tokens by hand); Pantheon/Agora/Extra Wonders movement variants (own-side movement, Minerva, Poliorcetics) — `Military.advance` takes the mover and a count so they are additions.

## Global Constraints

- New code in `military/` (rules), `effect/` (`Shields`), `state/` (`Victory`), `event/` (`PawnMoved`). Same formatting and package rules as before.
- Pawn convention (existing): `Pawn.side` is the player whose capital end of the track the pawn is on, null at the centre. A shield by player M moves the pawn one space toward the **opponent's** capital: from M's side it moves toward the centre, from the centre it enters the opponent's side at 1, on the opponent's side it moves out.
- Zones of the track: spaces 1–2 → zone 1, 3–5 → zone 2, 6–8 → zone 3, 9 → capital (B-MIL-1). The centre is no zone. Entering means the zone differs from the one the pawn was in before the step.
- Token location: zone `military`, key `<side player uuid>:<zone number>`, a public non-live zone. A token's effect is applied with the **opponent of the side's player** as owner (so `LoseCoins` makes the side's player lose), then the token goes to `destroyed` keyed by the side (B-MIL-3, §3.9).
- Victory: decided once, on the landing space (B-MIL-4); shields left over when the capital is reached are dropped (clamp). `declareVictory` clears the agenda: nothing pending survives the end of the game.
- Snapshot: `SevenWondersDuelState` gains `victoryWinner` (UUID, null) and `victoryReason` (String, null); round-trips.

## Review Focus

- Movement of n shields is n single steps; each zone entered (including re-entering after a back-and-forth) fires its token, a consumed token never fires again, and tokens passed in one move fire in the order entered.
- A move that crosses the centre (the opponent had the advantage) ends on the mover's advantage and fires nothing for zones on the opponent's side being left.
- Reaching the capital ends the game for the mover, extra shields are dropped, pending steps (the turn end, queued token effects, decisions) are discarded, and nothing can be done afterwards: Answer, Build, BuildWonder, Discard, Draft are all rejected.
- A token whose loss exceeds the victim's treasury takes only what they have (existing `LoseCoins`); a token on the pawn's own starting zone is never fired by merely standing there.
- A snapshot of a finished game round-trips with its winner and reason; a snapshot of a running one has null for both.
- Shields from a built card resolve after the card is in the city and before the turn ends; a card with a shield and a play-again still honours the play-again.

---

### Task 1: The game can end (Victory, OVER)

**Files:** Create `state/Victory.java` (`record Victory(PlayerId winner, Reason reason)`, `enum Reason { MILITARY }` — later plans add `SCIENCE`, `SCORE`; winner null is reserved for a shared victory by score and is not used yet, so the constructor requires it now); modify `state/DuelState.java` (component `victory`, last, nullable; `withVictory`; every constructor call site), `effect/Resolution.java` (`declareVictory(PlayerId winner, Reason reason)`: `advance` sets phase `OVER`, the victory and an empty agenda; `drain` stops when the phase is `OVER`), `turn/Turn.java` (`end` returns when the game is over), `snapshot/Snapshots.java` + `SevenWondersDuelState` (two fields), `engine/Engine.java` (`apply` rejects with "the game is over" when the phase is `OVER`, before any other check). Test: `state/DuelStateTest`, `effect/ResolutionTest`, `snapshot/SnapshotsTest`, `engine/EngineTest`.

**Interfaces:** `Resolution.declareVictory(PlayerId, Victory.Reason)`; `DuelState.victory() : Victory` (null while running); `DuelState.withVictory(Victory)`.

- [ ] Failing tests: `declareVictory` sets OVER, the victory and clears a non-empty agenda; a drain with effects still queued does nothing after it; `Turn.end` after it changes nothing; every `PlayerAction` kind is rejected with "the game is over"; a victory in a state outside OVER (or OVER without victory is allowed — scoring will use it) — only `victory != null ⇒ phase == OVER` is enforced by `DuelState`; snapshot round trip with and without a victory. Red → green → commit `feat(game): a game can end in victory (B-END-1)`.

### Task 2: The pawn primitive

**Files:** Create `event/PawnMoved.java` (`record PawnMoved(Pawn from, Pawn to) implements GameEvent`); modify `event/GameEvent.java` (permits), `state/Pawn.java` (`Pawn towards(PlayerId target, PlayerId other)` — no: see Interfaces), `effect/Resolution.java` (`setPawn`). Test: `state/PawnTest`, `effect/ResolutionTest`.

**Interfaces:** `Pawn.stepToward(PlayerId capitalOwner, PlayerId mover) : Pawn` — one space toward `capitalOwner`'s capital; `mover` is the other player: from the centre it enters `capitalOwner`'s side at 1; on `capitalOwner`'s side it goes out one (clamped at `CAPITAL`); on the other side it goes in one (reaching the centre clears the side). `Resolution.setPawn(Pawn)`: replaces the pawn, records `PawnMoved`, no-op (no event) when unchanged.

- [ ] Failing tests: all four step cases including through the centre and clamping at 9; `setPawn` records one `PawnMoved` with both positions and fires nothing when equal. Red → green → commit `feat(military): the pawn moves one space at a time (B-MIL-2)`.

### Task 3: The military path

**Files:** Create `military/Military.java`, `military/MilitaryTrack.java` (zone numbering and token keys: `zoneOf(int space) : int` 0 for the centre/none, `key(PlayerId side, int zone) : String`, `Location tokenSpot(PlayerId side, int zone)`); modify `catalog/BasePack.java` (zone `military`, public, non-live, not terminal). Test: `military/MilitaryTest`, `military/MilitaryTrackTest`, `catalog/BasePackTest` (zone exists).

**Interfaces:** `Military.advance(Resolution, PlayerId mover, int shields)`: for each shield, `setPawn(pawn.stepToward(opponent, mover))`; when the new space is a zone different from the previous space's zone and `military/<side>:<zone>` holds a token, add that token to the list and move it to `destroyed` keyed by the side; at the capital, stop stepping and `declareVictory(mover, MILITARY)` (which discards the queued effects); otherwise, after the loop, `queue` one `EffectStep` per effect of the collected tokens in the order entered (`queueEffects` for each token, in reverse so the first entered is on top), owner = opponent of the side's player.

- [ ] Failing tests with fixture tokens (`LoseCoins`): one shield from the centre moves to side 1 and fires nothing; three shields enter zone 2 and fire its token once (victim loses the coins, capped by the treasury); a second advance through the same zone fires nothing; retreat to the centre and advance again to the zone after the token was consumed fires nothing, while a token placed afresh fires (re-entering); a move through zones 1→2→3 fires both tokens, first-entered resolves first; a move that starts on the mover's own disadvantage (pawn on mover's side, space 2, 3 shields) ends on the opponent's side at 1; nine shields from the centre win, 12 shields win with the pawn at 9; victory discards the already-collected token effects; two movers in turn undo each other. Red → green → commit `feat(military): the military path, tokens and capital supremacy (B-MIL-2..4)`.

### Task 4: Shields from cards and Wonders

**Files:** Create `effect/Shields.java` (`record Shields(int count) implements Effect`; positive count); the `Component` data stays declarative. Test: `turn/ShieldsTest` (end to end through `Engine`).

**Interfaces:** `Shields.apply(resolution, owner)` calls `Military.advance(resolution, owner, count)`.

- [ ] Failing tests through `Engine.apply`: a built red fixture card with `Shields(2)` moves the pawn two spaces toward the opponent and the turn passes; the same on a Wonder; a card with `Shields(1)` and `GrantPlayAgain` moves the pawn and keeps the move; a built card moving the pawn onto a token fires the loot before the turn ends; a card with enough shields to reach the capital ends the game (phase OVER, victory military for the actor, active player unchanged, no turn end); every further action is rejected; the pawn after a game-ending move is at 9. Red → green → commit `feat(effect): shields move the pawn (B-MIL-2)`; then `docs: mark plan 5c done`.
