# Economy and Strategy Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans (native, inline). Steps use checkbox (`- [ ]`) syntax.

**Goal:** The two progress tokens that hook into existing mechanics work as data: Economy pays the bank's share of a purchase to its owner, and Strategy adds a shield to the owner's future red cards (B-TRADE-4, B-PRG table, B-EFF-4).

**Architecture:** (1) A purchase is paid in two parts: the printed coins go to the bank, and the trade cost goes through a new primitive `Resolution.payTrade(payer, amount)` that charges the payer and records `TradePaid(payer, amount)`. (2) Economy is a `Trigger<TradePaid>` component part, `TradeRevenue`: when the payer is not its owner, the owner gains the amount. Nothing in `Construct` knows about Economy. (3) Strategy is a static `ShieldBonus(Tag, int)` modifier; `Shields.apply` adds the owner's bonuses whose tag the shield's source carries (found by `resolution.source()`), so it is not retroactive (only cards built later run their shields) and does not touch Wonders (no red tag).

**Tech Stack:** Java 21, JUnit 6, AssertJ; same build as plans 1–5d.

**Spec:** `docs/rules/base.md` §3.8 (Economy, Strategy, Wonder shields note), B-TRADE-4, B-EFF-4, §10.2–10.3.

**Builds on:** 5a (`Quote.tradeCost` is already separate from `Quote.coins`), 5b (`Construct`), 5c (`Shields`), 5d (`Resolution.source()`). Decided with the user: Economy pays only the coins spent on **bought resources**, never the printed coin cost, and applies to Wonders too; Strategy is a shield modifier queried by `Shields`.

**Out of scope:** placing the progress tokens' data in `BasePack` (catalog data waits); Architecture, Masonry, Theology (cost/wonder modifiers); scoring tokens; Urbanism.

## Global Constraints

- `TradePaid` is recorded only when the amount is positive. Payment still fails (`changeCoins`) if the payer lacks the coins; the caller has already checked the quote.
- Economy gains exactly what the payer paid — after the payer's own discounts — and only when the payer is the owner's opponent.
- `Tag.RED = "color:red"` follows `Tag.YELLOW`; the `Tag.color(name)` cleanup stays a deferred minor.
- The bonus applies per `Shields` effect run for a source tagged with the bonus tag; the owner is the shield's owner.

## Review Focus

- A purchase with printed coins and bought resources gives the Economy holder only the trade part; a purchase with no bought resources gives nothing, and a chained build gives nothing.
- The Economy holder building pays the bank, not themselves.
- Economy works for Wonders as well as cards.
- Strategy: a red card built before the token gives no bonus; after it, +1; a non-red card and a Wonder get none; the opponent's red cards get none.
- The extra shield can reach the capital and win; it moves the pawn one space at a time like any other shield.

---

### Task 1: Paying for trade is an event

**Files:** Create `event/TradePaid.java` (`record TradePaid(PlayerId payer, int amount) implements GameEvent`); modify `event/GameEvent.java` (permits), `effect/Resolution.java` (`payTrade`), `turn/Construct.java` (pay `quote.coins()` with `changeCoins`, then `payTrade(actor, quote.tradeCost())`, in both `building` and `wonder`). Test: `effect/ResolutionTest`, `turn/BuildTest`.

**Interfaces:** `Resolution.payTrade(PlayerId payer, int amount)`: charges the payer and records `TradePaid`; no-op for zero.

- [ ] Failing tests: `payTrade` lowers the treasury and records one `TradePaid`, nothing for zero; a build with a bought resource records `TradePaid(actor, tradeCost)` and the coins part separately as `CoinsChanged`; a build with only printed coins records no `TradePaid`; same for a Wonder. Red → green → commit `feat(trade): the price of bought resources is a payment event (B-TRADE-4)`.

### Task 2: Economy

**Files:** Create `cost/TradeRevenue.java` (`Trigger<TradePaid>`; `react`: if `!event.payer().equals(owner)`, `resolution.changeCoins(owner, event.amount())`). Test: `turn/EconomyTest`.

- [ ] Failing tests through `Engine` with a fixture token carrying `TradeRevenue` in the opponent's city: the holder gains exactly the trade cost, the payer's total outlay is unchanged; printed coins alone give nothing; a chained build gives nothing; the holder building their own purchase gains nothing (net cost is only what they paid, to the bank); a Wonder purchase pays the holder; the token on the payer's own side does not fire. Red → green → commit `feat(trade): Economy gives the trade payment to its owner (B-TRADE-4)`.

### Task 3: Strategy

**Files:** Modify `catalog/Tag.java` (`RED`); create `military/ShieldBonus.java` (`record ShieldBonus(Tag tag, int extra) implements Modifier`); modify `effect/Shields.java` (`apply`: add the owner's `ShieldBonus` modifiers whose tag the source carries). Test: `turn/StrategyTest`, `military/ShieldBonusTest`.

**Interfaces:** `Shields.apply` total = `count + Σ extra` over `Ownership.modifiers(state, rules, owner, ShieldBonus.class)` with `source().tags().contains(tag)`; no source → no bonus.

- [ ] Failing tests through `Engine`: a red card with 1 shield and Strategy in the city moves the pawn 2; without the token 1; a red card built before the token arrives gave 1 and the token adds nothing later; a non-red shield card and a Wonder get no bonus; the opponent's token gives nothing to the actor; enough bonus shields from the centre reach the capital and win. Red → green → commit `feat(military): Strategy adds a shield to future red cards (B-PRG)`.
