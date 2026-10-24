# Science Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans (native, inline). Steps use checkbox (`- [ ]`) syntax.

**Goal:** Scientific symbols are counted from what a player owns; the first pair of a symbol lets the player take a progress token from the board (with its instant effects); six different symbols end the game (B-SCI-1..3, B-END-1).

**Architecture:** (1) A symbol is a static `Symbol(ScienceSymbol)` modifier, so the count is derived from live components like production and chains (B-EFF-1); nothing is stored. (2) `ScienceCheck` is the instant effect a source (green card, the Law token) carries: for each `Symbol` the source has, it first checks supremacy (six distinct symbols, `declareVictory(SCIENCE)`), then the pair rule (the symbol's count is exactly 2, so only the first pair of each symbol counts) by asking the owner to pick a progress token on the `board`; the pick moves to the owner's city and its effects are queued (Agriculture, Urbanism, and Law's own `ScienceCheck`, which can chain). (3) `Resolution.source()` tells a running effect which component it belongs to.

**Tech Stack:** Java 21, JUnit 6, AssertJ; same build as plans 1–5c.

**Spec:** `docs/rules/base.md` §9 (B-SCI-1..3), §3.8 (tokens), §10.6, B-EFF-3 (nothing to pick means nothing happens).

**Builds on:** 3 (decisions, `ComponentOption`), 5b (`Build` to drive it end to end), 5c (`declareVictory`, `Victory.Reason`). Decided with the user: the work is split, science here (5d) and Economy/Strategy next (5e).

**Out of scope:** the progress tokens' own rules other than Law and the instant ones the engine already supports (Economy, Strategy, Masonry, Architecture, Theology, Urbanism hook are 5e or later; VP tokens belong to scoring); placing five random tokens on the board at setup (B-SET-3) — tests place them by hand, as in 5c; expansion symbol sources (Ishtar, Nisaba, US Capitol).

## Global Constraints

- New code in `science/` (rules), `effect/` (`ScienceCheck`, `Resolution.source`), `state/` (`Victory.Reason.SCIENCE`).
- Symbols are counted over the owner's **live** components (`Ownership.modifiers`). A progress token is a `TOKEN` component tagged `Tag.PROGRESS`; those on the board are in zone `board`.
- Pair: the check runs after the source is in the city, so "the first pair" is a count of exactly 2 for that symbol. Three or more grants nothing (B-SCI-1). A source with a symbol the owner already holds twice before it (count 3) grants nothing.
- Supremacy is checked before the pair: six distinct symbols end the game at once and the pair token is not offered.
- No progress token on the board: nothing happens (B-EFF-3). Exactly one: still offered (a decision with one option), as everywhere else in the engine.
- The pick is made by the owner of the source.

## Review Focus

- A third symbol of a kind grants nothing; a pair that completes while the board is empty grants nothing and the game goes on.
- Taking Law from the board gives a symbol that can itself complete a pair or the sixth symbol, in the same turn.
- The sixth distinct symbol wins even when it also completes a pair; no token is offered and the turn does not end.
- A picked token's instant effects resolve for the picker (Agriculture-like coins) before the turn ends.
- A snapshot of a game won by science round-trips.

---

### Task 1: Symbols and their count (`science/`)

**Files:** Create `science/ScienceSymbol.java` (record of non-blank `id`), `science/Symbol.java` (`record Symbol(ScienceSymbol symbol) implements Modifier`), `science/Science.java` (`Map<ScienceSymbol,Integer> counts(DuelState, RuleSet, PlayerId)`, `int distinct(...)`). Test: `science/ScienceTest`.

- [ ] Failing tests: counts over the city only (a symbol in the discard or in the opponent's city does not count); two cards with the same symbol count 2; `distinct` ignores repeats; a blank symbol id is rejected. Red → green → commit `feat(science): symbols are counted from what a player owns (B-SCI-2)`.

### Task 2: `Resolution.source()` and the SCIENCE victory

**Files:** Modify `effect/Resolution.java` (`Optional<ComponentId> source()`: the component whose effect is running, empty outside one), `state/Victory.java` (`Reason.SCIENCE`). Test: `ResolutionTest`, `SnapshotsTest`.

- [ ] Failing tests: `source()` is the card inside `apply` and `resume`, empty outside; a SCIENCE victory round-trips through the snapshot. Red → green → commit `feat(effect): an effect can ask which component it belongs to`.

### Task 3: `ScienceCheck` (pair and supremacy)

**Files:** Create `effect/ScienceCheck.java` (`record ScienceCheck() implements Effect`), implemented with `Science`. Test: `science/ScienceCheckTest` (direct, through `Resolution`).

- [ ] Failing tests: first pair opens a decision whose options are exactly the progress tokens on the board; the pick goes to the owner's city and its effects are queued (a fixture token with `GainCoins(6)` pays out after `drain`); a third symbol offers nothing; an empty board offers nothing; six distinct symbols declare a SCIENCE victory for the owner and offer nothing even when the sixth also completes a pair; a source with two symbols checks both (pair on the second); a token with `ScienceCheck` and a `Symbol` taken as the pick can chain into a second decision. Red → green → commit `feat(science): the first pair takes a progress token, six symbols win (B-SCI-1..3)`.

### Task 4: End to end through `Engine`

**Files:** Test `turn/ScienceTest` (cards from `TurnFixtures.Part`).

- [ ] Failing tests through `Engine.apply`/`Answer`: building a green fixture card that completes a pair leaves a pending decision for the builder, the turn not yet ended; answering moves the token into the city, applies its coins and then passes the turn; building the sixth symbol ends the game (phase OVER, victory SCIENCE, active unchanged); picking Law whose symbol completes another pair opens a second decision before the turn ends. Red → green → commit `test(science): science end to end`; then `docs: mark plan 5d done`.
