# Tokens Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans (native, inline). Steps use checkbox (`- [ ]`) syntax.

**Goal:** The 10 progress tokens and 4 military tokens as declarative data, the two rules they need that the engine lacks (Urbanism's chain bonus, Theology's Wonder bonus turn), and military-token placement in `Setup`.

**Architecture:** (1) `ScienceSymbol.SCALES` (Law). (2) A `ChainBuilt` event, announced by `Construct.building` when the quote was waived by a chain, and a `ChainBonus(coins)` trigger that pays the owner for their own chain builds (Urbanism). (3) A `WonderBonusTurn` trigger: when a Wonder enters the owner's built Wonders, the owner gets play again (Theology; the flag cannot double up). (4) `Token` is a record like `Card` and `Wonder` (type `TOKEN`; tags `Tag.PROGRESS`, or a military zone tag). `ProgressTokens` and `MilitaryTokens` are tables; the rest reuses `Reduction`, `TradeRevenue`, `ShieldBonus`, `Symbol` + `ScienceCheck`, `Points`. (5) `Setup` places military tokens by zone tag: the i-th token of a zone on the i-th player's side.

**Tech Stack:** Java 21, JUnit 6, AssertJ.

**Spec:** `docs/rules/base.md` §3.8, §3.9, B-MIL-3, B-SCI-1..3. Old `ProgressToken` and engine token positions agree (Loot 2 at space 3, Loot 5 at space 6, each side); Urbanism's chain bonus is rulebook-only.

**Builds on:** 7c (`Resolution.shuffle`, Wonders, `Spots`), 5b `Construct`, 5c `Military`, 5d `ScienceCheck`, 5e `Reduction`/`TradeRevenue`/`ShieldBonus`.

**Out of scope:** the full seeded game (7e).

## Global Constraints

- Architecture/Masonry: 2 fewer symbols on Wonders / blue cards, never retroactive (queried at quote time).
- Economy: the opponent's trade payments go to the token's owner; printed coin costs do not.
- Strategy: +1 shield from the owner's future red cards only; Wonders unboosted.
- Law: scales symbol and the science check; counts toward the six-symbol win.
- Mathematics: 3 VP per progress token owned, itself included. Agriculture: +6$ and 4 VP. Philosophy: 7 VP.
- Urbanism: +6$ now; +4$ per own Building built free by a chain (not by Mausoleum, not the opponent's builds).
- Theology: every later Wonder the owner builds grants play again.
- Military tokens: Loot 2 in zone 2 (spaces 3–5), Loot 5 in zone 3 (6–8), on each side; the effect is `LoseCoins` for the player the pawn is pushed against, via the existing `Military`.
- Progress tokens are tagged `Tag.PROGRESS`; Setup needs ≥ 5.

## Review Focus

- Urbanism: chain build pays 4 once; a normal build, a Mausoleum rebuild and the opponent's chain build pay nothing.
- Theology: a Wonder with its own play again does not double; a Wonder built before the token does not retro-grant.
- Law as the pair-triggering token: taking Law when you hold one scales card, and when you hold the other five symbols (supremacy).
- Mathematics with Mathematics alone = 3 VP; Scoring puts tokens on the progress line.
- Setup over `BaseContent`: 5 progress on the board, 5 in the Box; military tokens on correct sides and zones; seed-stable.
- The Great Library offers only Box tokens, never the board's (real catalogue).

---

### Task 1: SCALES, ChainBuilt/ChainBonus, WonderBonusTurn

**Files:** modify `science/ScienceSymbol.java`, `effect/Resolution.java` (announce), `turn/Construct.java`; create `event/ChainBuilt.java`, `cost/ChainBonus.java`, `effect/WonderBonusTurn.java`. Tests: alongside; `ScienceTest` uses `ScienceSymbol.SCALES`.

- [ ] Tests first: Review Focus Urbanism and Theology cases; `Construct.building` with a chained quote emits `ChainBuilt`.
- [ ] Implement; `spotless:apply`, `verify`, commit.

### Task 2: Token, tables, BaseContent

**Files:** create `content/Token.java`, `content/ProgressTokens.java`, `content/MilitaryTokens.java`; modify `content/BaseContent.java`, `military/MilitaryTrack.java` (zone tag). Tests: `content/ProgressTokensTest`, `content/MilitaryTokensTest`.

- [ ] Tests first: ten distinct progress tokens, all `Tag.PROGRESS`, type TOKEN; each rule of §3.8 played through `Resolution`/`CostSolver`/`Scoring`; four military tokens with zones and `LoseCoins`.
- [ ] Implement; verify, commit.

### Task 3: Setup places military tokens; real-catalogue Setup

**Files:** modify `setup/Setup.java`. Tests: `setup/SetupTest` plus a `BaseContent` Setup test.

- [ ] Tests first: Review Focus Setup cases, a Pawn hitting a placed Loot token costs the right player coins.
- [ ] Implement; verify, commit; register this plan in `docs/INDEX.md`.
