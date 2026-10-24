# Setup, Wonder Draft and Age Start Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans (native, inline). Steps use checkbox (`- [ ]`) syntax.

**Goal:** A game that can be started from a seed: setup (B-SET-3, 4, 6; B-WND-1), the Wonder draft (B-WND-2..5) as player actions, and the start of Age I with the starter choice (B-AGE-0, B-AGE-2).

**Architecture:** Content is found by typed `Tag`s on components, never by id. `DuelState` gains `age`, `first` and `deciding`; the draft's turn order and legal picks are derived from `first` and zone counts, so a draft needs no extra state. Draft picks and the starter choice are `PlayerAction`s (decided with the user: not agenda steps). One seeded cursor feeds every shuffle.

**Tech Stack:** Java 21, JUnit 6, AssertJ; same build as plans 1–4a.

**Spec:** `docs/rules/base.md` §5.1 (B-SET-3/4/6), §5.3 (B-WND-1..5), §6 (B-AGE-0, B-AGE-2); `docs/design/core-mechanics.md`.

**Builds on:** 4a. Out of scope: military pawn and tokens (B-SET-1/2, plan 5), Age II/III start and `deciding` after Age I (4c), Wonder-draft variants (B-WND-6).

## Global Constraints

- Same package root and formatting rules as 4a. Domain code never imports `snapshot`.
- Deck zone keys: `"1"`, `"2"`, `"3"` per Age, `"wonders"` for the Wonder pool. Wonders are drafted into `wonders.unbuilt` keyed by player UUID text.
- No invented card data: tests use fixture catalogs sized to the rule (23/23/20 age cards, 7 guilds, 12 wonders, 10 progress tokens built from fixture components).

## Review Focus

- Draft order: first, second, second, first; then second, first, first, second — and only the player to move may pick, only from `draft`.
- A pick of a Wonder not offered, a pick after the draft, a pick before setup, and a second pick by the same player out of turn all throw `IllegalActionException` and leave the state unchanged.
- A catalog whose counts do not fit the rules (not 10 progress tokens, not 12 wonders, fewer than 3 guilds, an Age deck that cannot reach 20) fails setup by name.
- Same seed gives identical setup; different seeds differ somewhere; every component is placed exactly once.
- `ChooseStarter` by the non-deciding player, when nothing is pending, or naming a non-seated player is rejected.
- A snapshot taken mid-draft and mid-starter-choice round-trips.

---

### Task 1: Tags and the new state fields

**Files:** Create `catalog/Tag.java`; modify `catalog/Component.java` (`default Set<Tag> tags()` = empty), `state/DuelState.java` (+`int age`, `PlayerId first`, `PlayerId deciding` nullable, with `withAge`, `withDeciding`), `SevenWondersDuelState` (+`int age`, `UUID first`, `UUID deciding` nullable), `snapshot/Snapshots.java`; update every constructor caller in tests and `Fixtures.state`. Test: `DuelStateTest`, `SnapshotsTest`.

**Interfaces:** Produces `record Tag(String value)` with `of(String)`, `static Tag age(int n)` (`"age:n"`), constants `GUILD`, `PROGRESS`. `DuelState` validation: `first` seated, `deciding` null or seated, `age` ≥ 0. `Fixtures.state` keeps its signature (age 1, first = active, deciding null).

- [ ] Write failing tests: invalid `first`/`deciding`/negative age rejected; snapshot round trip carries all three (incl. null deciding).
- [ ] Run, expect compile failure. Implement. Run `./mvnw -o verify`. Commit `feat(state): tags, age, first player and deciding player`.

### Task 2: Age → layout mapping

**Files:** modify `catalog/ContentPack.java` (`default Map<Integer, LayoutId> ageLayouts()` empty), `RuleSet` (`LayoutId`/`Layout layoutForAge(int)`; two packs mapping one Age rejected like other duplicates), `BasePack` (1→`age1`, 2→`age2`, 3→`age3`). Test: `RuleSetTest`.

Ruling carried from 4a: layouts stay keyed by `LayoutId`; the Age mapping is data. An expansion that wants a different Age I layout is a design call for that expansion (the duplicate is rejected loudly, not silently resolved).

- [ ] Tests: `baseMapsEachAgeToItsLayout`, `unmappedAgeIsRejectedByName`, `duplicateAgeMappingIsRejected`. Red → green → commit `feat(catalog): map Ages to layouts`.

### Task 3: Setup

**Files:** Create `setup/Setup.java`; Test `setup/SetupTest.java` (fixture catalog helper `SetupFixtures` in the test tree).

**Interfaces:** `static DuelState Setup.newGame(RuleSet rules, long seed, PlayerId a, PlayerId b)`. Result: phase `WONDER_DRAFT`, `age` 0, `first` = seeded pick (`Rng.nextInt(…, 2)`), `active` = `first`, `deciding` null, 7 coins each, 5 progress tokens in `board`, 5 in `box`; per Age n deck key `"n"` in `deck` = Age n cards minus 3 to `box` (plus 3 guilds for Age III, 4 guilds to `box`); 12 wonders shuffled in `deck` key `"wonders"`, first 4 moved to `draft` (B-WND-2). Selection by tag/type; counts checked by name.

- [ ] Tests for every Review Focus bullet on setup (counts, determinism, each component placed once, failure messages). Red → green → commit `feat(setup): seeded setup (B-SET-3/4/6, B-WND-1)`.

### Task 4: Wonder draft

**Files:** Create `engine/DraftWonder.java` (`record DraftWonder(ComponentId wonder)`), `draft/Draft.java` (derivation); modify `PlayerAction`, `Engine`. Test: `draft/DraftTest`, `engine/EngineTest`.

**Interfaces:** `Draft.toMove(DuelState) : Optional<PlayerId>` (empty when not drafting); `Engine` validates phase `WONDER_DRAFT`, mover, offered wonder, then moves it to `wonders.unbuilt`/mover, reveals the next 4 after pick 4 (B-WND-3), and after pick 8 sends the unrevealed 4 to `box` (B-WND-4), sets `age` 1, deals Age I (Task 5 hook) and starts the Age. `active` follows `toMove`.

- [ ] Tests: full 8-pick script yields 4 wonders each; wrong mover/not offered/after draft rejected, state untouched. Red → green → commit `feat(draft): the Wonder draft (B-WND-2..5)`.

### Task 5: Age start and starter choice

**Files:** Create `engine/ChooseStarter.java` (`record ChooseStarter(PlayerId starter)`), `age/AgeStart.java`; modify `PlayerAction`, `Engine`. Test: `age/AgeStartTest`, `engine/EngineTest`.

**Interfaces:** `AgeStart.begin(Resolution, int age)`: deal `deck` key `age` into `layoutForAge(age)` via `Structure.deal`, `revealUncovered`, phase `AGE`, `age` set, `deciding` = `first` for Age I. `ChooseStarter` legal only in phase `AGE` with `deciding` set and actor = `deciding`; sets `active`, clears `deciding`.

- [ ] Tests: after the draft, structure is dealt and uncovered face-down cards revealed; starter choice rules from Review Focus; mid-state snapshot round trip. Red → green → commit `feat(age): Age start and starter choice (B-AGE-0, B-AGE-2)`.
