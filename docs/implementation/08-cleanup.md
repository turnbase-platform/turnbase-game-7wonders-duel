# Deferred-Minors Cleanup Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans (native, inline). Steps use checkbox (`- [ ]`) syntax.

**Goal:** Pay down the minors deferred from plans 7a–7e: four missing rule tests, the doc/code disagreements in `base.md`, and the duplicated fluent-record helpers. No new mechanics.

**Architecture:** Tests pin existing behaviour (any failure is a production defect, fixed test-first in its own commit and ledgered). Docs are aligned to the code unless the code is wrong. `Card`, `Wonder` and `Token` share one `Fluent.append` helper.

**Tech Stack:** Java 21, JUnit 6, AssertJ.

**Spec:** `docs/rules/base.md` §3.8–3.9, §10, §13, B-SCI-1..3, B-MIL-3.

**Out of scope:** `placeMilitaryTokens` side assignment and `ChainBuilt` carrying a card (design changes); the chained-free-card Urbanism question (an `[OPEN-…]` rules item — surface, do not guess); frontend and bundle work.

## Global Constraints

- No behaviour change in `src/main` except where a new test exposes a defect.
- Rule IDs cited in test names or comments; no invented values.

## Review Focus

- A refactor of the fluent records changes no catalogue entry (`BaseContent` equality before/after).
- Each new test fails when the behaviour it pins is removed.

---

### Task 1: Missing rule tests

**Files:** tests under `src/test/.../turn`, `science`, `setup`, `cost`.

- [ ] Urbanism: a Mausoleum rebuild of a chain-able card pays no +4.
- [ ] Law through `ScienceCheck`: taking Law while holding a scales-less pair opens a token pick; taking it with the other five symbols wins (B-SCI-3).
- [ ] Great Library after a real `Setup` over `BaseContent` offers only Box tokens, never the board's.
- [ ] Architecture, Masonry, Economy, Strategy each change a real quote/shield as specified in 7d Global Constraints.
- [ ] `spotless:apply`, `verify`, commit.

### Task 2: Docs aligned

**Files:** `docs/rules/base.md`.

- [ ] Theology: §10 lists it as a modifier but it is a `WonderBonusTurn` trigger — align the text to the code.
- [ ] §13 "Lighthouse ◂Amphora", §3.2 science-symbol names, stray blank line; reference to nonexistent `B-NAMES` noted in the ledger only (commit message is immutable).
- [ ] Commit.

### Task 3: One `append` helper

**Files:** create `content/Fluent.java`; modify `Card`, `Wonder`, `Token`.

- [ ] Test first: `BaseContent` components equal a snapshot taken before the refactor (ids, costs, effects, modifiers, triggers).
- [ ] Replace the three private `append` helpers with `Fluent.append`; `verify`; commit.
