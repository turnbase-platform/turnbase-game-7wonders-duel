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

## Outcome (2026-10-25)

Built as planned: `core/badges.ts`, `core/motif.ts`, `ui/icons.tsx` (55 icons), `ui/Card.tsx`
(`Card`, `CardBack`), `ui/style/{tokens,card}.ts`, and the gallery at `dev/gallery.html`
(`npm run dev`, then `/dev/gallery.html`). 98 frontend tests, bundle 205 KB (was 199).

**Rulings**

- No icon is copied from the old sprite (decision 4 said to start from it): it is a 125 KB
  multi-colour clip-art set and does not fit the flat look. All 55 are drawn here, one colour,
  `currentColor`, solid for coins, resources and the shield, line art for symbols and tokens.
- Wonders are 4:3, not 3:2 (design §2 amended): at 3:2 the five-resource cost row left no art area.
- The legacy first-screen `.card` chip class is now `.chip`, so the real card owns `.card`.
- Tests render with `renderToStaticMarkup`, not React Testing Library: it is not a dependency and
  the components are pure of state.

**Findings**

- `pointsPer` can count `coins` (Moneylenders' Guild, one point per three coins, `per = 3`); the
  first cut had no glyph for it. `present()` now shows a coin with the lot size.
- Gallery pass, light and dark, all three sizes: legible; small cards are banner-only by design
  (name as tooltip). Wonder art area is modest at Standard; look again once the board uses it (13b).
- Not yet done: the paid-card edge on built Wonders is a plain tab; 13c supplies the card that paid.
