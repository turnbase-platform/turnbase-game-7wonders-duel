# Frontend visual design

How the game looks and moves. Adapted from the old frontend's `DESIGN.md` and `ICONS.md`
(`grmp/games/seven-wonders-duel/frontend/`, read-only); every departure is listed in §9. The
frontend is purely visual: it renders the view and reacts to events by component id (plan 12). Rules
live in `rules/`; nothing here restates them.

## 1. Look

Ancient world, kept simple: flat vector shapes, a stone and bronze palette, one serif display face
for titles and numerals on a plain sans for text (system fonts only; the iframe CSP allows no remote
fonts). Light theme is warm limestone; dark theme is basalt. Both are CSS variables switched by
`prefers-color-scheme`. No textures, no photographic art.

| Token | Light | Dark | Use |
|---|---|---|---|
| `--ground` | limestone | basalt | board background |
| `--surface` | pale sand | dark slate | panels, cities |
| `--accent` | bronze | bright bronze | focus, takeable glow, active seat |
| card colours | brown #8B6914, gray #888888, blue #4A90D9, green #5AAD5A, yellow #DAA520, red #C73030, purple #7B4B94 | same, lightened for contrast | card banners |

Exact values are settled when the first screen is drawn (plan 13), not here.

## 2. Components are drawn from the catalog

The catalog gives every component its name, type, tags, back group, cost and badges. A card, a
Wonder and a token are each one React component that draws itself from its `CatalogEntry`; there are
no per-card files.

- **Card anatomy** (kept): effects banner (about 1/6 height, category colour, one icon per badge with
  the amount overlaid as text, not baked into the icon), cost bar top-left, chain badge top-right,
  art area, name bar at the bottom.
- **Art area**: a generated motif, not an empty block. A large glyph (the card's dominant badge) on a
  gradient of its category colour. Real art can later replace the motif without a layout change.
- **Sizes**: Large 120×160 (popover, viewers), Standard 75×100 (pyramid, cities), Small 50×67
  (compact lists, phone). Sizes are CSS custom properties so a breakpoint rescales a whole zone.
- **Backs** come from `back`: one face per group (Age I, II, III, Guild, Wonder, Token).
- **Icons**: one inline SVG `<symbol>` sprite in `main.js`. Needed: `vp`, `shield`, `coin`, the five
  resources, ten progress tokens (all exist in the old sprite), plus **17 chain symbols and 7 science
  symbols to draw** (the old sprite used emoji). Each icon copied from the old sprite names its source
  in a comment. Icons have a `<title>`.

## 3. Screens and panels

One board, built from panels. Wide screens show all panels; narrow screens show one at a time.

| Panel | Contents |
|---|---|
| **Structure** | the Age pyramid (or the Wonder draft in its phase), the five board tokens |
| **Cities** | both cities: cards by colour, Wonders (built, unbuilt), owned tokens, coins, resources produced |
| **Track** | the military track with pawn and penalty tokens, and the science progress of each seat |
| **Log** | the event log (plan 12), collapsible |
| **Viewers** | discard pile and destroyed cards, opened from a button; full-screen on phone |

Phases map to panel content: starter choice and the draft replace the pyramid area; Age transitions
and game over are overlays. A pending decision (destroy a card, take a token, pick from the discard)
opens a prompt over the relevant panel and never hides the panel it refers to.

### 3.1 Wide (tablet and desktop, ≥ 900px)

```
┌ header: Age · turn · coins ────────────────────────────┐
│ opponent city                                          │
├────────────────────────┬───────────────────────────────┤
│ structure (pyramid)    │ track · tokens · log          │
├────────────────────────┴───────────────────────────────┤
│ your city                                              │
└────────────────────────────────────────────────────────┘
```

Both cities are visible at once (the old toggle between tableaux is dropped). A spectator sees the
same board with both seats as "opponent" and hidden cards as counts.

### 3.2 Narrow (phone, < 900px)

One panel at a time behind a bottom tab bar: **Structure · Cities · Track · Log**. The header (Age,
turn, both coin counts, a one-line military position) stays visible on every tab so the state of the
game is never off screen. Rules for the switching:

- The tab for what the player must act on is highlighted; it is selected automatically when it becomes
  their turn, and **never switched under their finger** mid-interaction.
- Cities shows one city at a time with a seat toggle (yours first); the opponent's city is one tap away.
- An action popover becomes a bottom sheet.
- Cards render Small in lists, Standard in the pyramid, Large only in the sheet.

## 4. Acting

Selecting a quoted card (a quote's presence means it is takeable, plan 12) anchors a popover to it on
wide screens, a bottom sheet on phone. It shows the Large card and up to three actions: **Build**
(disabled with a reason when not affordable), **Wonder**, **Discard**. Each action shows its price
from the quote: coins to the bank, coins to the opponent for trade, resources covered by what the city
makes, chain-free builds. Choosing Wonder asks which unbuilt Wonder next. Escape or tapping outside
cancels. Every control is a real button and reachable by keyboard.

## 5. Choreography

Cues, not an animation queue. A **cue** is derived from an event and names a component id (or a
zone-level thing like the pawn); the view has already changed, and each cue animates from where the
component *was* to where it *is* (FLIP: record the DOM rectangle before the view applies, play after).

| Event | Cue |
|---|---|
| `moved` | the component travels from its old rectangle to its new one (build, discard, wonder, draft, deal) |
| `moved` structure.down → structure.up | flip: back turns to face, "was uncovered" |
| `coins` | the amount floats off the coin counter and ticks it |
| `trade` | coins float from the payer to the other seat |
| `pawn` | the pawn slides along the track; a crossed penalty token pulses |
| `chain` | the chain badge on the built card lights up |
| `playAgainGranted` / `Cleared` | the seat's turn marker pulses / settles |

Rules: cues play in event order and parent-child nesting (children with their cause); a new message
mid-cue finishes the cue instantly rather than queueing behind it; any click skips; `prefers-reduced-
motion` turns every cue into an instant state change. Total time for one move is under about 1.2 s.
Durations are tokens, not constants scattered in code. Age transition and victory are overlays
driven by the phase, not events. No sound.

## 6. The pyramid

Static grid: odd rows offset by half a cell, rows overlap about 30% vertically, a removed card leaves
its slot empty and nothing reflows. Covered cards are dimmed, takeable cards glow and are the only
interactive ones. Placement comes from the `key` of each `structure.*` component (to be confirmed
against the Age layouts in plan 13); face-down cards use the Age's back and are counts-only for a
viewer that may not identify them. On phone the pyramid scales with the Standard size token and
scrolls horizontally only if the Age III layout does not fit.

## 7. Military, science, tokens, end

- **Track** (plan 15): always on the board, not a collapsed chip. Spaces −9…+9, penalty tokens at the
  rule-defined spaces, pawn from `pawnSide`/`pawnSpace`. Colour follows the seat colours.
- **Science**: six symbols per seat, owned ones lit, pairs marked, Law shown as the token it is.
- **Progress tokens**: five on the board, owned ones in the city; the three-token draw is a pending
  prompt.
- **End screen**: winner, `victoryReason`, final scores. A *breakdown* needs scoring data on the wire
  (not there yet); it is a backend item raised in plan 15 before the screen depends on it. Rematch
  and leave are the platform's, not the game's.

## 8. Seats and spectators

Seat colours are fixed per seat (seat 1 bronze, seat 2 teal), never per viewer, so a spectator and
both players see the same colour for the same city. "Mine" is a marker, not a colour. A spectator
never sees a hidden component's name (plan 12 verified the data side; the UI must not invent one).

## 9. Departures from the old design

| Old | New | Why |
|---|---|---|
| emoji placeholders | drawn SVG icons | looks finished; 24 icons to draw |
| `AnimationQueue` with a typed list | cues from events, FLIP, skippable | the backend already sends events by component id |
| action modal, no cost detail | anchored popover / sheet with the quote's price | quotes exist; modals are heavy |
| toggle between tableaux | both cities visible (wide), seat toggle (phone) | a duel is watched across both |
| military in a collapsible header chip | permanent track | it is the game's tension |
| empty art area | generated motif | readable without commissioned art |
| winner and score only | reason, plus breakdown when the wire has it | needs a backend item |
| host rematch / leave | platform's job | out of the game's scope |
| mouse only | keyboard-operable buttons, reduced motion | cheap |
| no phone layout | tab-switched panels (§3.2) | owner's request |
| first player vote buttons | `chooseStarter` offers (already built) | the rules changed the flow |

## 10. Order of work

13 — tokens, card/wonder/token components, icons, the pyramid and draft, cities, the action popover
(wide and phone). 14 — cues from events. 15 — track, science, tokens, pending prompts, end screen.
16 — the real-stack pass. Each plan opens with the screens it changes shown as a mockup the owner
reacts to before code is written.
