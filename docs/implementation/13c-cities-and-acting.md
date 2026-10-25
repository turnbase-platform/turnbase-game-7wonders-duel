# Plan 13c — Cities and acting

**Why.** With the board in place (13b) the player still cannot build, discard or construct a Wonder from
it, and the cities are not drawn. This plan adds the city stacks and the Build / Wonder / Discard flow
of `docs/design/frontend-visual-design.md` §4–§5. Moves themselves exist (`core/moves.ts`, plan 12).

## What is already settled

- `offers(session)` already derives `build`, `discard` and (not yet driven) `buildWonder` from quotes.
  A quote is the price for one card or Wonder for each player; a card with a quote may be taken.
- Zones in the `city` family hold each player's built cards; `wonders.unbuilt` / `wonders.built` hold
  Wonders. Card colour comes from the tag `color:<name>`.
- Stacks: one per colour in a fixed order, banners visible, empty colours take no space, no labels.
  Wonders below the stacks as flat cards; built = owner outline plus the paid-for edge.

## Decisions

1. **City model is pure.** `core/city.ts`: `cityOf(view, catalog, player): {coins, stacks, wonders,
   tokens, produces}` with stacks in the fixed colour order and cards in build order. `produces` is
   derived from the player's cards (a plain tally, not the rules engine: it is the display line, and
   the exact payable amount always comes from the quote).
2. **One action surface.** `ui/Actions.tsx`: a popover anchored to the selected card on wide/medium, a
   bottom sheet on portrait/landscape (same content, switched by `Layout`). It shows the Large card
   and the three actions with prices from the quote; unaffordable Build is disabled with its reason.
3. **Wonder choice is a second step.** `buildWonder` needs card *and* Wonder; choosing "Wonder" lists
   the unbuilt Wonders with their quoted price, then sends the action. `moves.ts` gains the
   `buildWonder` offers (card × affordable Wonder).
4. **Quote prices read plainly**, from `QuoteView`/`CostView`: coins to the bank, trade coins, resources
   covered. If a field the screen needs is missing from the wire, that is a spec-defect against the
   view (plan 6/11), not a client guess.
5. **Keyboard**: card selection and the three actions are real buttons; Escape closes; focus returns to
   the card.

## Tasks

1. `cityOf`, test-first: colour order, build order, empty colours absent, wonders built/unbuilt,
   spectator view (hidden counts only).
2. `moves.ts`: `buildWonder` offers, test-first (affordable only, needs a selected card).
3. `<City>` (stacks, wonders, line with coins/produces/tokens) and the Opponent | You tabs in the side
   panel (You on your turn); Hover/tap a stack card shows the Large card.
4. `<Actions>` popover/sheet with the Wonder second step; wired to `sendAction`; rejection banner stays.
5. Playwright pass: build a card, discard one, construct a Wonder (the flow plan 12 could not drive),
   at the four sizes; findings in this plan.
6. Checks clean; bundle size noted.

## Done when

A full Age I can be played from the new board with Build, Discard and Build-Wonder, both cities are
visible by tab, and a Wonder construction shows in the city with the owner's outline.

## Not in this plan

Motion (14), the military track, science and token prompts, end screen (15).
