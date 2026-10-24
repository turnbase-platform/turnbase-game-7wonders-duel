# Cleanup Pass Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans (native, inline). Steps use checkbox (`- [ ]`) syntax.

**Goal:** Close the deferred items that later work would build on: card-back groups in the view (B-BACK-1), a typed `Tag.color`, one shared set of zone ids, and the small 6c leftovers.

**Architecture:** (1) `id/BackId` names a back group; `ContentPack.backs()` declares them (default none) and `Component.back()` is an immutable `Optional<BackId>` of the card; `RuleSet.assemble` rejects a back no pack declares and a duplicate declaration. Dealing a card face-down into the structure (`Setup`) fails if it has no back. (2) `HiddenView(zone, key, back, count)` groups hidden components by place **and** back. (3) `Tag.color(String)` replaces the per-colour constants as the way to name a colour. (4) `zone/Zones` holds the base zone ids once. (5) `onInit` requires two teams of one; the snapshot round trip is tested with real `teams` and a `FORFEIT` victory; `WireActions` documents that unknown JSON fields are ignored by the platform mapper.

**Tech Stack:** Java 21, JUnit 6, AssertJ.

**Spec:** `docs/rules/base.md` §3.10 (B-BACK-1, B-BACK-2). Decided with the user: option 1, the back is pack-declared, immutable card data like colour and name.

**Builds on:** 3 (`RuleSet`), 4b (`Setup`), 6b (`Views`), 6c (`SevenWondersDuelGame`, `WireActions`).

**Out of scope:** real card data (`BasePack.components()` stays empty); the remaining 6b test gaps, `canSee` cost, `affordable`.

## Global Constraints

- Back, colour and name are fixed parts of a card; nothing in core names a back group except through a pack.
- No invented values: tests use fixture backs, the base pack declares none until its catalogue exists.

## Review Focus

- A card with no back, or a back no pack declares, is rejected with the offending id named; a face-down card's back is in the view for both seats and the spectator, and nothing else about it is.
- Two hidden cards at one place with different backs are two `HiddenView`s, never merged.
- Replacing the zone constants changes no id string; replacing the colour constants changes no `Tag.value()`.
- `onInit` with one team of two, or three players, throws; the state round trips with filled `teams`.

---

### Task 1: Back groups

**Files:** create `id/BackId.java`; modify `catalog/ContentPack.java`, `catalog/Component.java`, `catalog/RuleSet.java`, `setup/Setup.java`, `view/HiddenView.java`, `view/Views.java`, the test fixtures. Tests: `catalog/RuleSetTest`, `setup/SetupTest`, `view/VisibilityTest`/`ViewsTest`.

- [ ] Tests first: undeclared and duplicate backs rejected at assembly; `Setup` rejects a back-less card; hidden cards at one place with two backs give two `HiddenView`s carrying the back; a spectator sees the same backs.
- [ ] Implement; update fixtures to declare a fixture back.

### Task 2: Tag.color

**Files:** modify `catalog/Tag.java` and its users (`DiscardValue`, `Scoring`, tests). Test: `TagTest`.

- [ ] Test `Tag.color("yellow").value()` is `color:yellow`; replace the `YELLOW`/`BLUE`/`RED` constants by `Tag.color(..)` at the use sites.

### Task 3: Shared zones

**Files:** create `zone/Zones.java`; modify the classes holding private `ZoneId.of("...")` constants (Structure, Setup, Draft, Military, MilitaryTrack, Places, DiscardValue, ScienceCheck). Test: `zone/ZonesTest` pins every id string.

- [ ] Pin the strings first, then point each class at `Zones`; the suite is the regression net.

### Task 4: 6c leftovers

**Files:** modify `SevenWondersDuelGame.onInit`, `wire/WireActions` Javadoc. Tests: `GameInitTest`, `SnapshotsTest`.

- [ ] Test: `onInit` with one team of two is rejected; a snapshot with filled `teams` and a `FORFEIT` victory survives the JSON round trip.
- [ ] Implement the two-teams check; document the unknown-field behaviour on `WireActions.decode`.
