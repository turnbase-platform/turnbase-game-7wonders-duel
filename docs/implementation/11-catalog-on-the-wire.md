# Plan 11 — The catalog on the wire

**Why.** The view names components by id only. A client that is purely visual (owner, 2026-10-25:
"FE reacts to events and uses component ids to animate") still needs to know what each id *is*:
name, colour, Age, cost, what it gives. The data is static and derived from the rule set, so it is
**not** persisted in `SevenWondersDuelState`; it travels once, in the resync message, and the FE
keeps it for the session. It leaks nothing: it lists every card of the packs, not which are dealt.

## Mechanics describe themselves

Effects, modifiers and triggers are Java behaviour, so the FE cannot read them. Each mechanic
declares `badges()` (`Badged`, a default method returning none): a small presentation vocabulary,
`Badge(kind, amount, of, per, larger)`. A new mechanic in an expansion adds its own badge; no
central switch changes. Opaque lambdas (`Points`, `Census`, `Reduction`'s predicate, `PriceModifier`)
get named forms (`FixedPoints`, `FixedPrice`, `Selector`, `Census.Counted`) so they can describe
themselves.

| Mechanic | kind | amount / of |
|---|---|---|
| `Shields` | `shields` | count |
| `ShieldBonus` | `shieldBonus` | extra, of tag |
| `GainCoins` / `TakeCoins` / `LoseCoins` | `coins` / `takeCoins` / `loseCoins` | amount |
| `GainCoinsPer` / `PointsPer` | `coinsPer` / `pointsPer` | each, census (`of`, `per`, `larger`) |
| `Points.fixed` | `points` | points |
| `Production` | `produces` / `producesOneOf` | count, resources |
| `PriceModifier.fixedAt` | `fixedPrice` | price, resources |
| `Reduction` | `reduction` | symbols, selector labels |
| `Symbol` | `science` | symbol id |
| `ChainSource` / `ChainBonus` | `chain` / `chainBonus` | symbols / coins |
| `DiscardOpponentCard` | `discardOpponent` | tag |
| `DrawProgressToken` | `drawProgress` | drawn |
| `BuildFromDiscard`, `GrantPlayAgain`, `WonderBonusTurn`, `TradeRevenue` | `buildFromDiscard`, `playAgain`, `playAgainOnWonder`, `tradeRevenue` | |
| `ScienceCheck` | none (the `science` badge shows it) | |

## Catalog

`CatalogView(entries)`, one `CatalogEntry(id, name, type, tags, back, cost, badges)` per component
of the rule set, in registration order; `CostView(coins, resources, freedBy)`. `ServerMessage`
gains `catalog` (null except on resync).

## Tasks

1. `Badge`, `Badged`, and every base mechanic described, tested one by one.
2. `CatalogViews.of(rules)`; a test that every mechanic of the base content describes itself
   (so a new mechanic cannot ship invisible) and that census badges name what they count.
3. `ServerMessage.catalog`, sent by `onResync`.
