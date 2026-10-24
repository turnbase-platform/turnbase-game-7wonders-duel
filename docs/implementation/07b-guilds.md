# Guilds Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans (native, inline). Steps use checkbox (`- [ ]`) syntax.

**Goal:** The 7 purple guilds as declarative data, with the "city with the most" census and a points-per-census modifier.

**Architecture:** (1) `Census` gains three compositions: `sum(Census...)`, `largerOfBoth(Census)` (the maximum over the owner's and the opponent's city, B-GUILD-1) and `every(n, Census)` (whole groups of `n`), plus `coins()`, the owner's treasury. (2) `Points.per(Census, each)` is the end-of-game twin of `GainCoinsPer`: `each` points per unit counted at scoring. (3) `Guilds` is a table like the Age tables; `Card.guild(name)` makes a purple, `Tag.GUILD` card with the dark-purple back and **no Age tag** (Setup deals guilds by `Tag.GUILD`). `BaseContent` adds the guilds.

**Tech Stack:** Java 21, JUnit 6, AssertJ.

**Spec:** `docs/rules/base.md` §3.6. Costs and effects were cross-checked against the old `DeckFactory.getAllGuildCards()`: all seven agree with the docs.

**Builds on:** 7a (`Census`, `GainCoinsPer`, `Card`, `BaseContent`), 6a (`Points`).

**Out of scope:** Wonders (7c), tokens (7d), the full game (7e).

## Global Constraints

- The coin payment (on construction) and the points (at game end) each take their own maximum; they may name different cities (`base.md` §3.6).
- Shipowners counts brown **and** grey in **one** city: the maximum of the sum, not the sum of two maxima.
- Moneylenders: 1 VP per whole 3$ in the richest city; the maximum is taken over treasuries.
- Builders: no coins; 2 VP per Wonder (`Tag.WONDER`, built) in the city with most.
- Guilds carry `Tag.GUILD` and `Colors.PURPLE`, back `Backs.DARK_PURPLE`, no Age tag.
- No per-card branches in core code.

## Review Focus

- Shipowners: owner has 3 brown + 0 grey, opponent 1 brown + 3 grey → coins/points use opponent's 4, not 3 + 3.
- A guild built when both cities have zero of the colour pays nothing and does not fail.
- The opponent's cards count (they are in a live zone keyed by the opponent); the owner's guild itself is purple and counts for nothing.
- Moneylenders: 8$ → 2 VP; 2$ → 0; the richer treasury wins even when it is the opponent's.
- Guilds add no card to `tagged(Tag.age(3))`; Setup still deals 20 − 3 + 3.

---

### Task 1: Census compositions and Points.per

**Files:** modify `effect/Census.java`; create `scoring/PointsPer.java`; modify `scoring/Points.java` (`per`). Tests: `effect/CensusTest`, `scoring/PointsPerTest`.

- [ ] Tests first: `largerOfBoth` returns the larger of owner's and opponent's count, in both directions and on a tie; `sum` adds; `every(3, coins())` gives 2 for 8$; `coins()` reads the owner's treasury; `Points.per(census, 2)` returns `2 * count` at scoring.
- [ ] Implement as records (equal values compare equal), `Census.sum`, `Census.largerOfBoth`, `Census.every`, `Census.coins`, `Points.per`.
- [ ] `spotless:apply`, `verify`, commit.

### Task 2: Guilds table and pack

**Files:** modify `content/Card.java` (`guild(name)`); create `content/Guilds.java`; modify `content/BaseContent.java`; modify `content/AgeCardsTest` (count Age cards by Age tag, not by type). Tests: `content/GuildsTest`.

- [ ] Tests first: 7 distinct guilds, each purple, `Tag.GUILD`, dark-purple back, no Age tag; every cost from `base.md` §3.6; the Review Focus cases played through `Resolution.resolveEffects` and `Scoring`.
- [ ] Implement; the table lists guilds through the builder (`Census.largerOfBoth(Census.of(Colors.YELLOW))` etc.).
- [ ] Check `AgeCardsTest` Setup arithmetic still holds; `spotless:apply`, `verify`, commit.

### Task 3: Docs

- [ ] Register in `docs/INDEX.md`; `base.md` §3.6 needs no change.
