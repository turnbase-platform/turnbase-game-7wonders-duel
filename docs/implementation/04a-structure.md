# Structure Layouts, Accessibility and Reveal Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans (native, inline) to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** The card structure of an Age as data: layouts for the three base Ages, dealing a deck into a layout, deriving which cards are covered or takeable, and revealing face-down cards that became uncovered (B-STR-1, B-STR-2).

**Architecture:** A `Slot(row, x)` is a position; its string form is the `Location` key in `structure.up` / `structure.down`. A `Layout` is rows of x positions, each row face up or down, registered by a `ContentPack` and looked up through `RuleSet`. Covering is one relation: a slot is covered by the occupied slots of the next row down whose `x` differs by exactly 1 (`base.md` §5.2), so accessibility needs only the placements, never the layout. Revealing moves a card from `structure.down` to `structure.up` at the same key through `Resolution.move`.

**Tech Stack:** Java 21, JUnit 6, AssertJ; same build as plans 1–3.

**Spec:** `docs/rules/base.md` §5.2 (B-STR-1, B-STR-2), `docs/design/core-mechanics.md` §4.2–4.3.

**Builds on:** plan 3. This is 4a of three: 4b is setup, the Wonder draft and the Age start; 4c is the turn pipeline (Discard, play-again, Age end). Decided with the user: flow choices (draft pick, starter) are derived from state and are `PlayerAction`s, not agenda steps.

## Global Constraints

- Package root `io.turnbase.games.sevenwondersduel`; new package `structure` (domain, never imports `snapshot`). Layout data lives in `BasePack`; the mechanics never name an Age.
- Java 21 only; no I/O, clocks or randomness. Format with `./mvnw -q -o spotless:apply` before each commit; tests run with `JAVA_HOME=$HOME/.sdkman/candidates/java/21.0.2-graalce`.
- Rows are numbered top → bottom from 0; the bottom row is accessible first. Layout data is exactly the table in `base.md` §5.2; do not infer coordinates for expansion layouts.
- Out of scope: shuffling and choosing the deck (4b), taking a card (4c), Agora layouts.

## Review Focus

- Age III's gap row: x=1 and x=5 are each covered by a pair of the row below and cover a pair of the row above; removing only one card of a pair keeps the card above covered.
- A card is uncovered only when both neighbours below are gone, not one; edge slots with a single neighbour below need that one only.
- Revealing happens for every uncovered face-down card, in a fixed order (row, then x), and never for a covered one; a structure with nothing to reveal changes nothing and emits nothing.
- Dealing a deck whose size differs from the layout, or a card not in the deck, fails by name; the input placements are untouched.
- A malformed layout (row with unsorted or duplicate x, empty row) is rejected on construction.

---

### Task 1: Slots, layouts and the base Age layouts

**Files:**
- Create: `structure/{Slot,Layout}.java`, `id/LayoutId.java`
- Modify: `catalog/ContentPack.java` (`default List<Layout> layouts()` returning `List.of()`), `catalog/RuleSet.java` (merge layouts, `layout(LayoutId)`; duplicate id rejected like zones), `catalog/BasePack.java` (layouts `age1`, `age2`, `age3`)
- Test: `structure/LayoutTest.java`, `catalog/RuleSetTest.java` (extend)

**Interfaces:**
- Produces:
  - `record LayoutId(String value)` with `static of(String)`, same shape as `ZoneId`.
  - `record Slot(int row, int x)` with `String key()` (`"row:x"`), `static Slot parse(String key)` (`IllegalArgumentException` naming a bad key), `Location location(ZoneId zone)`.
  - `record Layout(LayoutId id, List<Row> rows)` with nested `record Row(boolean faceUp, List<Integer> xs)`; the constructor rejects an empty layout, an empty row and xs that are not strictly increasing. `List<Slot> slots()` top → bottom, left → right; `boolean faceUp(Slot)`; `int size()`.
  - `RuleSet.layout(LayoutId)` (`IllegalArgumentException` naming an unknown id); `ContentPack.layouts()`.

- [ ] **Step 1: Write the failing tests**

`LayoutTest`: `everyBaseLayoutHasTwentySlots`; `rowShapesMatchTheRulebook` (Age I rows 2U 3D 4U 5D 6U, Age II 6U 5D 4U 3D 2U, Age III 2U 3D 4U 2D 4U 3D 2U, read from `faceUp` and row sizes); `ageThreeGapRowSitsAtOneAndFive`; `slotKeyRoundTrips` and `badSlotKeyIsRejectedByName`; `unsortedOrEmptyRowIsRejected`. `RuleSetTest`: `layoutIsLookedUpById`, `unknownLayoutIsRejectedByName`, `duplicateLayoutAcrossPacksIsRejected`.

- [ ] **Step 2: Run them, expect compile failure.** `./mvnw -o test -Dtest='LayoutTest,RuleSetTest'`
- [ ] **Step 3: Implement the types and the three layouts** with the x tables of `base.md` §5.2; row 0 is the top row of the table.
- [ ] **Step 4: Run them, expect pass; run `./mvnw -o verify`.**
- [ ] **Step 5: Commit** `feat(structure): add slots, layouts and the base Age layouts (B-STR-1)`.

### Task 2: Deal, accessibility and reveal

**Files:**
- Create: `structure/Structure.java`
- Test: `structure/StructureTest.java`

**Interfaces:**
- Consumes: Task 1 types; `Placements`, `Resolution.move`, `ZoneRegistry`.
- Produces (static, over the placement list):
  - `List<Placement> deal(List<Placement> all, Layout layout, List<ComponentId> cards, ZoneRegistry zones)`: card `i` (in `layout.slots()` order) moves from wherever it is to `structure.up` or `structure.down` at its slot key; `IllegalArgumentException` when `cards.size() != layout.size()`.
  - `List<Slot> uncovered(List<Placement> all)`: occupied slots (either zone) with no occupied slot at `(row+1, x±1)`, ordered by row then x.
  - `List<ComponentId> takeable(List<Placement> all)`: the face-up cards on uncovered slots, same order.
  - `void revealUncovered(Resolution resolution)`: moves every face-down card on an uncovered slot to `structure.up` at the same key, in `uncovered` order, computed once before moving.

- [ ] **Step 1: Write the failing tests** (fixture cards `c0..c19` in `deck`, rules from `BasePack` plus a fixture pack for the components)

`StructureTest`: `dealPutsFaceUpAndFaceDownCardsInTheirZones`; `dealRejectsWrongDeckSize` (input untouched); `onlyTheBottomRowIsUncoveredAfterDeal` for each of the three layouts (6, 2, 2 slots); `cardIsUncoveredOnlyWhenBothBelowAreGone` (Age I: remove one bottom neighbour, still covered; remove both, uncovered); `edgeSlotNeedsOnlyItsSingleNeighbour`; `ageThreeGapCardsFollowTheirPairs`; `takeableExcludesFaceDownCards`; `revealTurnsUncoveredFaceDownCardsUpInRowOrder` (event order, same keys, card ids); `revealWithNothingToDoEmitsNothing`; `revealLeavesCoveredFaceDownCardsAlone`.

- [ ] **Step 2: Run, expect compile failure.**
- [ ] **Step 3: Implement `Structure`.** Occupancy is read from the keys of placements in the two structure zones; parse keys with `Slot.parse`.
- [ ] **Step 4: Run the tests, then `./mvnw -o verify`.**
- [ ] **Step 5: Commit** `feat(structure): deal, accessibility and reveal (B-STR-1, B-STR-2)`.

---

## Self-Review

- **Spec coverage:** B-STR-1 (layouts, deal) Tasks 1–2; B-STR-2 (reveal) Task 2; the one-relation accessibility recommendation of §5.2 Task 2; layouts as pack data (§4.3) Task 1. Deck shuffling, "end of turn" timing of the reveal and taking a card are 4b/4c.
- **Rulings made while planning:** accessibility is derived from placements alone (slot keys carry the geometry), so no layout lookup or stored flag is needed; layouts are indexed by `LayoutId`, not by Age number, so an expansion layout is an addition.
- **Type consistency:** `Slot.key()` is the `Location.key` of both structure zones; `Structure` returns `Slot`/`ComponentId`, never `Location`.
