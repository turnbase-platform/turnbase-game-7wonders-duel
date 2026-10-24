# Wonders Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans (native, inline). Steps use checkbox (`- [ ]`) syntax.

**Goal:** The 12 Wonders as declarative data, plus the three decision effects they need.

**Architecture:** (1) `Resolution` gains two primitives, `shuffle(List)` (advances the seeded cursor) and `revealTo(player, component)` (one player learns an identity); `zone/Spots` names the public places effects move things to (city, discard, box). (2) Three effects, each offering only feasible options (B-EFF-3): `DiscardOpponentCard(colour)`, `BuildFromDiscard`, `DrawProgressToken(drawn)`. (3) `Wonder` is a record like `Card` (type `WONDER`, `Tag.WONDER`, no back); `Wonders` is the table; `BaseContent` adds them.

**Tech Stack:** Java 21, JUnit 6, AssertJ.

**Spec:** `docs/rules/base.md` §3.7, B-EFF-2..4. Costs and effects cross-checked against the old `DeckFactory.getAllWonders()`: all twelve agree.

**Builds on:** 7a/7b (`Card`, `BaseContent`, `Census`), 5b (`Construct`), 5e (`Shields`, `ShieldBonus`).

**Out of scope:** progress and military tokens (7d); the full game (7e). 7c tests use fixture tokens tagged `Tag.PROGRESS`.

## Global Constraints

- Wonder shields are not boosted by Strategy (`Tag.WONDER` is not the red tag); Wonder production does not raise the opponent's price.
- Appian Way: +3$ for the owner, the opponent loses `min(3, treasury)` (B-EFF-2), play again.
- Circus Maximus / Statue of Zeus: the owner picks one opponent card of the colour (grey / brown) to discard, then 1 shield. No such card: nothing is discarded, the shield still comes.
- Mausoleum: the owner picks any discarded card and builds it free; its effects run. Empty discard: nothing.
- Great Library: three random tokens from the Box (all if fewer), only the owner learns them; the owner takes one, the others stay in the Box.
- Great Lighthouse and Piraeus use `Production.choice(false, ...)`.
- No per-Wonder branches in core code; no randomness except through `Resolution.shuffle`.

## Review Focus

- Every `ComponentType.WONDER` carries `Tag.WONDER`; Arena and the Builders Guild count real Wonders.
- Great Library with 0, 1, 2 and 5 tokens in the Box; the opponent's view of the pending decision hides the options.
- Mausoleum picking a card whose effects open a decision (a green card's science pair) — resumes cleanly.
- Mausoleum card is built in the owner's city with no payment and no second turn end.
- Statue of Zeus when the opponent has no brown card: shield only; with Strategy owned the Wonder shield is unboosted.
- Same seed → same three tokens; the drawn tokens do not change the Box contents.

---

### Task 1: Resolution primitives and Spots

**Files:** modify `effect/Resolution.java`; create `zone/Spots.java`. Tests: `effect/ResolutionDrawTest`.

- [ ] Tests first: `shuffle` is deterministic per seed and advances `state().rng()`; `revealTo(P1, c)` adds to P1's knowledge only.
- [ ] Implement; `spotless:apply`, `verify`, commit.

### Task 2: DiscardOpponentCard and BuildFromDiscard

**Files:** create `effect/DiscardOpponentCard.java`, `effect/BuildFromDiscard.java`. Tests alongside.

- [ ] Tests first: options are exactly the opponent's cards of the colour (not the owner's); empty → no decision; resume discards; BuildFromDiscard lists the discard pile, builds free into the city, runs the card's effects, empty → nothing.
- [ ] Implement; verify, commit.

### Task 3: DrawProgressToken

**Files:** create `effect/DrawProgressToken.java`. Tests alongside.

- [ ] Tests first: Review Focus cases for 0/1/2/5 tokens; only the owner knows the drawn ones; resume moves the pick to the owner's city and runs its effects; same seed same draw.
- [ ] Implement; verify, commit.

### Task 4: Wonders table and pack

**Files:** create `content/Wonder.java`, `content/Wonders.java`; modify `content/BaseContent.java`. Tests: `content/WondersTest`.

- [x] Tests first: 12 distinct Wonders, all `Tag.WONDER`, type WONDER, no back; every cost/effect/points row of `base.md` §3.7; effects played through `Resolution`. (Setup over `BaseContent` needs the progress tokens: moved to 7d.)
- [ ] Implement; verify, commit; register this plan in `docs/INDEX.md`.
