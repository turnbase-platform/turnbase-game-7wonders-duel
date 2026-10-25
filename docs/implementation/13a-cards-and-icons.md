# Plan 13a — Theme, icons and the card component

**Why.** Everything on the board is a card, a Wonder or a token drawn from a `CatalogEntry`
(`docs/design/frontend-visual-design.md` §1–§2). Plans 13b and 13c lay them out; this plan makes them
exist and be looked at in isolation first. Spec: the design doc; nothing else is decided here.

## What is already settled

- The catalog has 99 entries (cards, Wonders, tokens). A card's colour is the tag `color:<name>`
  (`grey`, not `gray`), its Age `age:<n>`, its back group `back`. Badges are `{kind, amount, of, per,
  larger}`; the fixtures use 20 kinds: `produces`, `producesOneOf`, `shields`, `shieldBonus`, `points`,
  `pointsPer`, `coins`, `coinsPer`, `loseCoins`, `science`, `chain`, `chainBonus`, `tradeRevenue`,
  `reduction`, `fixedPrice`, `playAgain`, `playAgainOnWonder`, `drawProgress`, `buildFromDiscard`,
  `discardOpponent`.
- CSS is injected from JS (iframe CSP) and icons are inlined into `main.js` (plan 12).

## Decisions

1. **Badges become presentation in one pure function.** `core/badges.ts`: `present(badge): Glyph[]`
   (icon id, amount text or null, `title`). It is exhaustive over the kinds above and returns a generic
   glyph with the kind as its title for an unknown one, so a new expansion kind degrades instead of
   throwing. This is where all per-kind knowledge lives; components never switch on `kind`.
2. **The art motif is derived, not stored.** `core/motif.ts`: `motifOf(entry): IconId` picks the
   dominant badge by a fixed priority (the glyph a reader would name the card by). Pure, tested.
3. **Styling stays plain CSS in strings, split by concern** (`ui/style/tokens.ts`, `card.ts`, …),
   concatenated by `injectStyles()`. Tokens are CSS variables on `:root` (light) overridden under
   `@media (prefers-color-scheme: dark)`; sizes are variables too (`--card-w`).
4. **Icons: one sprite.** `ui/icons.tsx` holds the `<symbol>` set and an `<Icon id>` component. Start by
   copying from the old sprite what exists (`vp`, `shield`, `coin`, five resources, ten progress
   tokens), each with a source comment; draw the missing ones: the science symbols (7) and chain
   symbols (17), plus whatever `present()` needs that neither has (play-again, trade, discard,
   draw-token, build-from-discard, loot). Every icon has a `<title>`.
5. **A gallery in the dev harness** (`dev/gallery.html`): every catalog entry as a Small, Standard and
   Large card, on light and dark, both sizes of Wonder. This is how plan 13a is looked at, and it stays
   as the place to eyeball a new expansion's cards.

## Tasks

1. **`present()` and `motifOf()`**, test-first: one test per kind (icon, amount text), the unknown-kind
   fallback, and a test over the whole fixture catalog that every badge yields at least one glyph.
2. **Tokens and styles**: `tokens.ts` (palette, radius 3px, sizes, light/dark), `injectStyles()`
   composes the parts. Test: the injected sheet defines every token the other parts use.
3. **Icons**: the sprite and `<Icon>`; a test that every id `present()` and `motifOf()` can return
   exists in the sprite.
4. **`<Card entry size>`**, **`<CardBack group size>`**: banner (glyphs, amount overlaid), cost overlay,
   art motif, name bar, chain badge; Wonder variant (3:2, sandstone, `built`/`owner` outline, faded when
   unbuilt, the paid-for edge); token variant. Tests (React Testing Library): name, banner glyph count,
   cost shown only when non-zero, wonder variant class, back by group.
5. **Gallery** and a Playwright look at it on both themes; findings in this plan.
6. `lint`, `typecheck`, `format:check`, `test` clean; bundle size noted.

## Done when

The gallery shows all 99 entries legibly at Small, Standard and Large in both themes, no icon is an
emoji, and `present()` is exhaustive over the fixture catalog.

## Not in this plan

Placement of cards on the board (13b), cities and the action popover (13c), motion (14).
