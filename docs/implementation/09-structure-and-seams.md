# Structure and Seams Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans (native, inline). Steps use checkbox (`- [ ]`) syntax.

**Goal:** Close the three items plan 8 left out (chain events, military token sides, the chained free card) and the two extendability seams the codebase scan found (one zone vocabulary, pack-declared setup).

**Architecture:** Each task is behaviour-preserving except where stated. Tests first; every task ends with `spotless:apply` + `verify` and a commit.

**Spec:** `docs/rules/base.md` §3.8–3.9, §5.1 (B-SET-*), Urbanism row (§3.8).

**Out of scope:** indexing `Placements` — measured on 2026-10-25: a full game step costs ~90 µs including bot move enumeration, `Placements.idsIn` ~0.6 µs over 99 placements; not a bottleneck, ledgered as a ruling. Frontend and bundle work.

## Global Constraints

- No behaviour change in a game's outcome for any seed: `FullGameTest` (incl. the pinned seed-5 game) stays green untouched.
- Rule IDs cited in tests and comments; no invented values.

## Review Focus

- A chained card that is also free still pays Urbanism (the better outcome for the player).
- Setup of the base game is identical before and after parameterisation (same placements for the same seed).
- A military token's side no longer depends on catalogue order.

---

### Task 1: Chained free card (ruling by the owner, 2026-10-25)

Urbanism pays when the chain is used. A card that is free anyway but has a chain the player holds counts as chained — the player gets the better outcome. The code already behaves so (`CostSolver.chained` runs before the cost); pin it and record it in `base.md`.

- [ ] Test: `aFreeCardWithAHeldChainIsStillChained` (Quote.chained, `ChainBuilt` emitted).
- [ ] `base.md` Urbanism row: state the ruling.

### Task 2: `ChainBuilt` carries the card

- [ ] Test: the event for a chained build equals `new ChainBuilt(actor, card)`.
- [ ] `ChainBuilt(PlayerId builder, ComponentId card)`; `Resolution.chainBuilt(builder, card)`; `Construct.building` passes it.

### Task 3: Military token side is explicit

- [ ] Test: Setup places each zone's tokens by their seat tag, whatever the catalogue order; a token without a seat tag, or two on one seat and zone, is rejected by name.
- [ ] `MilitaryTrack.seatTag(int)`; `MilitaryTokens` tags seat 0/1; `Setup.placeMilitaryTokens` uses it.

### Task 4: One zone vocabulary

- [ ] `turn/Places` merges into `zone/Spots` (public `city`, `built`, `unbuilt`, `underWonder`, `DISCARD`, `BOX`); callers updated; tests use `Zones`/`Spots` instead of literals where they spell a base zone.

### Task 5: Setup parameters are declared by the pack

- [ ] Test: `RuleSet.setup()` of `BasePack` equals today's constants; two packs declaring setup are rejected by name; `Setup.newGame` output for fixed seeds is unchanged.
- [ ] `SetupParams` record (startingCoins, progressOnBoard, removedPerAge, guildsInPlay; the draft stays a procedure, B-WND-3); `ContentPack.setup()` default `Optional.empty()`; `RuleSet.setup()`; `Setup` reads it.
