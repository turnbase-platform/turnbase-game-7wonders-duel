# Turn Pipeline, Discard and Age End Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans (native, inline). Steps use checkbox (`- [ ]`) syntax.

**Goal:** A full turn loop for the one action that needs no cost solver, Discard: take an accessible card, end the turn (reveal, play again, alternate), and when the structure empties end the Age and start the next one with the right deciding player (B-TURN-1..6, B-ACT-2, B-AGE-1..3).

**Architecture:** `turn/Turn` owns "what happens after an action": reveal uncovered cards, then pick the next active player from the play-again flags, or end the Age when the structure is empty. `age/AgeEnd` clears the flags (a play again earned on the last turn is lost), then starts the next Age through `AgeStart.begin` or moves to `SCORING` after the last Age. The deciding player comes from the military pawn, which becomes real state here: `Pawn(side, space)`; plan 5 only moves it.

**Tech Stack:** Java 21, JUnit 6, AssertJ; same build as plans 1–4b.

**Spec:** `docs/rules/base.md` §6 (B-TURN-1..6, B-ACT-2), §7 (B-AGE-1..3), §8 (B-MIL-1 track, B-SET-1 start).

**Builds on:** 4b. Decided with the user: pawn state is added now (B-AGE-3 is implemented for real); yellow cards are identified by a `Tag`. Out of scope: Build and BuildWonder (plan 5), pawn movement, tokens and capital win (plan 5), final scoring (plan 5/6).

## Global Constraints

- Same packages and formatting rules as 4b. New packages `turn`; `age` already exists. Domain code never imports `snapshot`.
- `Pawn.space` is 0 (centre) to 9 (capital); `side` is the player the pawn is on the side of, null exactly at the centre. The track has no other data in 4c.
- A turn ends only when the agenda is empty; Discard opens no decision. Plan 5 must run `Turn.end` after a decision's drain (carried, not solved here).
- Discard pays `2$ + 1$ per card in the actor's city tagged Tag.YELLOW` (B-ACT-2); the constants live in one place.

## Review Focus

- Only the active player may take a card, only an accessible (takeable, face-up, uncovered) one, only in phase `AGE`, with no starter choice and no decision pending; anything else throws `IllegalActionException` and leaves the state unchanged.
- The reveal step runs after every take, so the next player sees newly uncovered cards (B-TURN-3).
- Play again: the active player's flag is consumed at the end of their turn and they move again; the opponent's flag (Shwedagon) is left until that player's own turn ends; a flag set on the turn that empties the structure is lost, for both players (B-TURN-5).
- Ending Age I or II starts the next Age with the deciding player from the pawn: the player the pawn is on the side of, or the player who took the last card when it is at the centre; the starter choice then follows as in Age I.
- Ending the last Age moves to `SCORING` with no deal and nobody to move; no action is legal there.
- A snapshot round-trips with the pawn at the centre and off it; an off-centre pawn with a missing side, or a side at the centre, is rejected.

---

### Task 1: The pawn as state

**Files:** Create `state/Pawn.java`; modify `DuelState` (+`Pawn pawn`, `withPawn`), `SevenWondersDuelState` (+`UUID pawnSide`, `int pawnSpace`), `Snapshots`, `Setup` (pawn at the centre, B-SET-1); update constructor callers in tests. Test: `state/PawnTest`, `DuelStateTest`, `SnapshotsTest`, `SetupTest`.

**Interfaces:** `record Pawn(PlayerId side, int space)`; constructor rejects space outside 0..9 and a side exactly when space is not 0 violated either way; `static final Pawn CENTRE`. `DuelState` validates that a non-null side is seated.

- [ ] Failing tests: centre and off-centre construct; bad combinations rejected by name; snapshot round trip with both; setup starts at the centre. Red (compile) → green → commit `feat(state): the military pawn (B-SET-1, B-MIL-1)`.

### Task 2: Discard and the turn end

**Files:** Create `engine/Discard.java` (`record Discard(ComponentId card)`), `turn/Turn.java`, `turn/DiscardValue.java`; modify `Tag` (`YELLOW`), `PlayerAction`, `Engine`. Test: `turn/TurnTest`, `turn/DiscardValueTest`.

**Interfaces:** `DiscardValue.of(RuleSet, DuelState, PlayerId) : int`; `Turn.discard(Resolution, PlayerId, ComponentId)`; `Turn.end(Resolution)` reveals uncovered cards, then hands over to the other player (flags in Task 3, Age end in Task 4).

- [ ] Failing tests for every Review Focus bullet on legality and the reveal; discard value 2 with no yellow cards, 2+n with n. Red → green → commit `feat(turn): Discard and the end of a turn (B-TURN-2, B-TURN-3, B-ACT-2)`.

### Task 3: Play again

**Files:** modify `turn/Turn.java`. Test: `turn/PlayAgainTest`.

- [ ] Failing tests: own flag → same player moves again and the flag is cleared (event recorded); opponent's flag untouched until their turn ends, then they move again; no flags → alternate. Red → green → commit `feat(turn): play again flags (B-TURN-4, B-TURN-6)`.

### Task 4: Age end

**Files:** Create `age/AgeEnd.java`, `age/Deciding.java`; modify `Turn.end`. Test: `age/AgeEndTest`.

**Interfaces:** `Deciding.forNextAge(DuelState state) : PlayerId` (pawn side, else `state.active()`, the player who took the last card); `AgeEnd.finish(Resolution)`: clear both flags, then `AgeStart.begin(r, nextAge, deciding)` for the next Age in `rules.ages()`, or `Phase.SCORING` after the last.

- [ ] Failing tests for the Age-end Review Focus bullets (a script that discards all 20 cards of Age I, with and without an off-centre pawn, a flag earned on the last turn, the last Age to SCORING). Red → green → commit `feat(age): Age end and the next Age (B-AGE-1..3, B-TURN-5)`.
