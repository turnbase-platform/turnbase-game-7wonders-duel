# Age Cards Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans (native, inline). Steps use checkbox (`- [ ]`) syntax.

**Goal:** The 66 Age I–III cards (23 + 23 + 20) as declarative data in the base pack, with backs, tags, costs, chains, production, shields, science, points and commerce.

**Architecture:** (1) A `content` package: `Card` (a `Component` record) with a small fluent builder, and one table class per Age (`AgeOne`, `AgeTwo`, `AgeThree`) listing cards through that builder; `Chains`, `Colors` and `Backs` name the shared constants once. (2) One new mechanic, `Census` (how many of something an owner has) and `GainCoinsPer(Census, each)`, which covers Chamber of Commerce, Port, Armory, Lighthouse and Arena; a census counts the owner's live components by tag, so Wonders only need the `Tag.WONDER` tag. Guilds (7b) add a "larger of both cities" census without touching this one. (3) A separate `BaseContent` pack (`base-content`) returns the tables and `Backs.ALL`; `BasePack` stays zones, layouts and age mapping, so fixture-based tests never see the real catalogue.

**Tech Stack:** Java 21, JUnit 6, AssertJ.

**Spec:** `docs/rules/base.md` §3.3–3.5, §3.10, §4.5. **The previous implementation (`grmp/.../DeckFactory.java`) is the source of truth for names, costs and effects**; the docs were cross-checked against it and agree on every cost and colour.

**Builds on:** 2 (effects), 4b (`Setup` finds cards by `Tag.age(n)`), 5a–5e (cost, military, science, economy), 6a (`Points`), 6d (backs).

**Out of scope:** Guilds (7b), Wonders (7c), tokens (7d), a full-game test (7e).

## Global Constraints

- Names follow the old code, with its clear defects fixed (`base.md` §13): Age II blue W W G is **Courthouse**, Age III red 8$ is **Pretorium**, the duplicate Age III "Fortifications" is **Circus** (C C S S, ◂helmet), **Circus Maximus**, Arena ◂barrel, Lighthouse ◂vase, **Glass-Blower** keeps the docs spelling (the user chose it). Ids are the name in lower-case with dashes.
- Chain symbol ids are the old enum names lower-cased (`vase`, `barrel`, `theater`, `temple`, `sun`, `water`, `columns`, `moon`, `target`, `helmet`, `horseshoe`, `sword`, `fortress`, `lyre`, `gear`, `book`, `lamp`). Which card gives and which frees follows `base.md` §4.5.
- Back groups: `brown` (Age I), `blue` (Age II), `violet` (Age III), `dark-purple` (guilds). Every card has the tags `Tag.age(n)` and `Tag.color(c)`.
- Brown and grey production raises the opponent's price (B-TRADE-2); Forum and Caravansery do not.
- No per-card branches in core code.

## Review Focus

- Lighthouse counts itself (the card is in the city before its effects run); Arena counts only constructed Wonders, not unbuilt ones; Chamber of Commerce with no grey card pays 0 and does not fail.
- A card both gives and is freed by the same symbol only where the table says so (Statue ◂theater, gives columns); no symbol is given by two cards or freed by none.
- Ages hold 23, 23 and 20 cards; ids are unique; no card lacks a back, an Age tag or a colour tag.
- `Setup` with the real catalogue deals Age decks of the layout sizes (no card double-tagged into two Ages).

---

### Task 1: Census and GainCoinsPer

**Files:** create `effect/Census.java`, `effect/GainCoinsPer.java`; modify `effect/Ownership.java` (add `count`), `scoring/ScoreContext.java` (delegate). Tests: `effect/GainCoinsPerTest`.

- [ ] Tests first: `GainCoinsPer(Census.of(tag), 2)` pays 2 per live tagged component of the owner, 0 for none, ignores the opponent's and non-live ones, and counts a card moved into the city before its effects run.
- [ ] Implement `Census` (`int count(DuelState, RuleSet, PlayerId owner)`, `static Census of(Tag)`), `Ownership.count(state, rules, player, tag)`, and the effect.

### Task 2: Constants and the Card builder

**Files:** create `content/Card.java`, `content/Chains.java`, `content/Colors.java`, `content/Backs.java`; modify `science/ScienceSymbol.java` (constants `PLUMB_BOB`, `WHEEL`, `QUILL`, `MORTAR_AND_PESTLE`, `SUNDIAL`, `ASTROLABE`), `catalog/Tag.java` (`WONDER`). Tests: `content/CardTest`.

- [ ] Tests first: the builder yields a `Component` of type CARD with id slugged from the name, the Age and colour tags, the back, cost, effects and modifiers; `gives(...)` adds a `ChainSource`; `freedBy(...)` sets the cost's chain set; a blank name is rejected.
- [ ] Implement.

### Task 3: Age I

**Files:** create `content/AgeOne.java`. Tests: `content/AgeOneTest`.

- [ ] Tests first: 23 cards, all tagged Age 1 and backed `brown`; spot-check Lumber Yard (free, wood), Logging Camp (1$), Glassworks (grey, glass), Stable (W, gives horseshoe, 1 shield), Workshop (P, plumb bob, 1 VP), Stone Reserve (3$, price 1 for stone), Tavern (free, 4$, gives vase), Theater (3 VP, gives theater).
- [ ] Implement the table.

### Task 4: Age II

**Files:** create `content/AgeTwo.java`. Tests: `content/AgeTwoTest`.

- [ ] Tests first: 23 cards, Age 2, back `blue`; spot-check Sawmill (2$, W W), Forum (3$ C, choice glass/papyrus, no price effect), Caravansery, Customs House (4$, glass and papyrus at 1), Courthouse (W W G, 5 VP), Statue (C C, freed by theater, gives columns), Barracks (3$, freed by sword), Brewery (free, 6$, gives barrel), Library (freed by book, quill, 2 VP).
- [ ] Implement the table.

### Task 5: Age III

**Files:** create `content/AgeThree.java`. Tests: `content/AgeThreeTest`.

- [ ] Tests first: 20 cards, Age 3, back `violet`; spot-check Pretorium (8$, 3 shields), Circus (C C S S, freed by helmet), Fortifications (freed by fortress), Chamber of Commerce (P P, per grey 3$, 3 VP), Port, Armory, Lighthouse (freed by vase, per yellow), Arena (freed by barrel, per Wonder 2$), Senate (freed by temple), Palace (7 VP).
- [ ] Implement the table.

### Task 6: Base pack and docs

**Files:** create `content/BaseContent.java`, modify `SevenWondersDuelGame` default constructor, `docs/rules/base.md` (§3.4, §3.5, §13 names), `docs/INDEX.md`. Tests: `catalog/BasePackTest`, `content/AgeCardsTest`.

- [ ] Tests first: 66 distinct ids; every card has a back the pack declares, one Age tag and one colour tag; every symbol given by exactly one card is freed by at least one card and vice versa; Age deck sizes after `Setup`'s removal/guild addition equal the layout sizes; Lighthouse counts itself in a played state through `Construct`.
- [ ] Wire the pack, update `base.md` names (Courthouse/Pretorium/Circus Maximus/chain symbol names) and `docs/INDEX.md`; run `spotless:apply` and the full suite.
