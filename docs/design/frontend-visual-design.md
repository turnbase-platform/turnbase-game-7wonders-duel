# Frontend visual design

How the game looks and moves. Adapted from the old frontend's `DESIGN.md` and `ICONS.md`
(`grmp/games/seven-wonders-duel/frontend/`, read-only); every departure is listed in §10. The
frontend is purely visual: it renders the view and reacts to events by component id (plan 12). Rules
live in `rules/`; nothing here restates them. The layouts below were agreed against browser mockups
on 2026-10-25; the mockups themselves are scratch and not kept.

## 1. Look

Ancient world, kept simple: flat vector shapes, stone and bronze, small corner radius (about 3px
everywhere, never pill-shaped), one serif face for titles and numerals over plain sans for text
(system fonts only; the iframe CSP allows no remote fonts). No gilding, no textures, no ornament.
Light theme is warm limestone; dark theme is basalt. Both are CSS variables switched by
`prefers-color-scheme`.

**Seat colours are per viewer.** You are always **cyan** and your opponent is always **bronze**, so
nobody has to remember who plays which colour. A spectator sees seat 1 as cyan (left) and seat 2 as
bronze (right). The same pair colours capitals, city tabs, outlines on built wonders and log names.

| Token | Value (light) | Use |
|---|---|---|
| `--ground` / `--surface` | limestone `#ece4d2` / sand `#f6f0e2` | board, panels |
| `--ink` | `#3b3226` | text, the pawn-zone outline |
| `--me` / `--opp` | cyan `#1f9aa8` / bronze `#9a5b2a` | the two seats |
| card colours | brown `#8B6914`, gray `#888888`, blue `#4A90D9`, green `#5AAD5A`, yellow `#DAA520`, red `#C73030`, purple `#7B4B94` | card banners, military pawn (red) |
| `--wonder` | sandstone `#a89468` | Wonder cards |

Exact values and the dark palette are settled when plan 13 draws the first screen.

## 2. Components are drawn from the catalog

The catalog gives every component its name, type, tags, back group, cost and badges. Cards, Wonders
and tokens are drawn by one React component each from their `CatalogEntry`; there are no per-card
files.

**One card anatomy for buildings and Wonders**: effects banner on top (an icon per badge, the amount
as overlaid text, never baked into the icon), cost overlay under the banner, art area, name bar at the
bottom. A chain badge sits at the top right when the card has one.

- **Buildings**: Standard 75×100 (pyramid, tablet), Small ~38 wide (stacks, phone), Large 120×160
  (popover, sheet).
- **Wonders**: the same anatomy at 4:3, larger, in the sandstone colour. Built: full colour, an
  outline in the owner's colour, and a small edge of the card that paid for it at the bottom right.
  Unbuilt: faded. Large version in the popover and sheet.
- **Art area**: a generated motif, not an empty block: the card's dominant badge as a large glyph on a
  radial highlight over the category colour. Real art can replace it later without a layout change.
- **Backs** come from `back`: one face per group (Age I, II, III, Guild, Wonder, Token).
- **Icons**: one inline SVG `<symbol>` sprite in `main.js`. Needed: `vp`, `shield`, `coin`, the five
  resources, ten progress tokens (all exist in the old sprite), plus **17 chain symbols and 7 science
  symbols to draw** (the old sprite used emoji). Each icon copied from the old sprite names its source
  in a comment. Icons have a `<title>`.

## 3. Layout

One board built from panels. Narrow screens show fewer at a time.

```
┌ header: Age · turn · both coin counts · science ──────────────────┐
├ strip: MILITARY TRACK (the centrepiece)         · progress tokens ┤
├────────────────────────────────────┬──────────────────────────────┤
│ structure (pyramid / wonder draft) │ right panel:                 │
│                                    │  [Opponent | You] city       │
│                                    │  Log                         │
└────────────────────────────────────┴──────────────────────────────┘
```

| Panel | Contents |
|---|---|
| **Track strip** | the military track (§6) and the board's progress tokens, above the structure |
| **Structure** | the Age pyramid, or the Wonder draft in its phase; centred, scaled to fit its container |
| **City panel** | one tab per city (Opponent, You; You selected on your turn), cities as colour stacks (§4) |
| **Log** | the event log, below the city panel |
| **Viewers** | discard pile and destroyed cards, opened from a button |

Phases fill the structure area: starter choice and the draft replace the pyramid; Age transitions and
game over are overlays. A pending decision (destroy a card, take a token, pick from the discard) opens
a prompt over the relevant panel and never hides the panel it refers to.

### Breakpoints

- **Wide (≥ 900px):** as drawn; the city panel is a column of ~230–300px beside the structure.
- **Medium (≈ 600–900px):** the right panel drops *under* the structure and becomes tabs
  Opponent / You / Log; the track strip stays on top.
- **Phone portrait:** one panel at a time behind a bottom tab bar, **Structure · Cities · Log**. The
  header and the compact track strip stay visible on every tab, so the state of the game is never off
  screen. The tab for what the player must act on is highlighted, and tabs are **never switched under
  a finger**.
- **Phone landscape:** structure left, the tabbed panel (Opp. / You / Log) right, a thin header and a
  thin track strip above to save height.
- **Pyramid scaling:** the scale is computed from the container width (a wrapper sized to the scaled
  pyramid, so it stays centred and never overflows), not a fixed CSS value.

A spectator sees the same board; both city tabs are "seat 1 / seat 2" and hidden cards are counts.

## 4. Cities

Per city, in the panel: coins, resources produced and owned progress tokens on one line; below it
**one stack per card colour**, then the Wonders. There are no colour labels, because a stack is the
colour. Cards in a stack are offset so each one's **effects banner stays visible**; a colour with no
cards takes no space. The order of the seven colours is fixed so stacks do not jump as cards are
built. Hover or tap a stack card for the Large card.

## 5. Acting

Selecting a quoted card (a quote's presence means it is takeable, plan 12) anchors a popover to it on
wide screens, a bottom sheet on phone. It shows the Large card and up to three actions: **Build**
(disabled with a reason when not affordable), **Wonder**, **Discard**. Each shows its price from the
quote: coins to the bank, coins to the opponent for trade, resources covered by what the city
produces, chain-free builds. Choosing Wonder asks which unbuilt Wonder next. Escape or tapping outside
cancels. Every control is a real button, reachable by keyboard.

## 6. The military track

The most prominent component on the board: a framed strip above the structure, square tiles, wider
than anything else in it. Orientation is fixed for every viewer: **your capital is on the left, the
opponent's on the right**, so the pawn is mirrored for the two players.

- **Tiles**: 19 squares, positions −9…+9 from your side. The small absolute number stays on each
  tile; the capitals show a shield. Capitals are solid in the owner's colour (cyan left, bronze right),
  with "defeat" under yours and "win" under the opponent's.
- **Gradient**: one background spread across all 19 tiles (tile *i* shows slice *i* of a 19×-wide
  gradient, so only tiles carry it): strong red at your end (imminent defeat), a light stone centre,
  gold at the opponent's end (imminent victory). Red appears nowhere else on the track except the pawn.
- **Pawn**: military red with a white ring and a dark outline so it reads on the red end.
- **Zone fields**, one row under the tiles, one field per rule zone (B-MIL-1):
  - outer zones (3–5 and 6–8, each side) hold a **loot token** drawn on top of the zone's end-of-game
    VP (B-MIL-5): "−2 🪙" and "−5 🪙";
  - when the pawn enters the zone and the token is applied, the token goes and the VP value shows,
    in a dashed field (dashed means exactly "the token was taken");
  - the middle zones {1,2} on each side show their VP always; the centre field is empty;
  - all VP fields are muted (thin border, grey text); the field the pawn is currently in is highlighted
    with a dark outline.
- **Distance on demand**: hovering, focusing or tapping any tile shows a tooltip with the shields the
  pawn needs to get there ("+3 shields", plus toward the opponent's capital, minus toward yours). No
  permanent badges: they were tried and were too noisy.
- The track is built from the view alone (`pawnSide`, `pawnSpace`, and which tokens are still in play
  in `visible`); the centre is `pawnSide = null`.

## 7. The pyramid

Static grid read from the view: every `structure.up` and `structure.down` entry carries a key
`row:x` (`Slot.key()`, x in half-card units), so the layout is drawn entirely from the view, hidden
cards included (a face-down card is a counted back at its slot). **Row 0 is the top row and the
accessible row is the bottom one** (the old frontend had this reversed). A card at `x` sits at
`x × half-card-width`; rows overlap about 30% vertically; a removed card leaves its slot empty and
nothing reflows. Covered cards are dimmed, takeable cards (those with a quote) glow and are the only
interactive ones.

## 8. Science, progress tokens, end

- **Science**: six symbols per seat, owned ones lit, pairs marked, Law shown as the token it is.
- **Progress tokens**: five on the board strip, owned ones in the city line; the three-token draw is a
  pending prompt.
- **End screen**: winner, `victoryReason`, final scores. A *breakdown* needs scoring data on the wire
  (not there yet); it is a backend item raised in plan 15 before the screen depends on it. Rematch and
  leave are the platform's, not the game's.

## 9. Choreography

Cues, not an animation queue. A **cue** is derived from an event and names a component id (or a
zone-level thing like the pawn); the view has already changed, and each cue animates from where the
component *was* to where it *is* (FLIP: record the DOM rectangle before the view applies, play after).

| Event | Cue |
|---|---|
| `moved` | the component travels from its old rectangle to its new one (build, discard, wonder, draft, deal) |
| `moved` structure.down → structure.up | flip: back turns to face, "was uncovered" |
| `coins` | the amount floats off the coin counter and ticks it |
| `trade` | coins float from the payer to the other seat |
| `pawn` | the pawn slides tile by tile; a loot token that is applied lifts away and reveals its VP |
| `chain` | the chain badge on the built card lights up |
| `playAgainGranted` / `Cleared` | the seat's turn marker pulses / settles |

Cues play in event order and parent-child nesting (children with their cause). A new message
mid-cue finishes the cue instantly rather than queueing behind it; any click skips;
`prefers-reduced-motion` makes every cue an instant state change. One move takes under about 1.2 s.
Durations are tokens, not constants scattered in code. Age transition and victory are overlays driven
by the phase, not events. No sound.

## 10. Departures from the old design

| Old | New | Why |
|---|---|---|
| emoji placeholders | drawn SVG icons | looks finished; 24 icons to draw |
| `AnimationQueue` with a typed list | cues from events, FLIP, skippable | the backend already sends events by component id |
| action modal, no cost detail | anchored popover / sheet with the quote's price | quotes exist; modals are heavy |
| toggle between tableaux | right-panel tabs per city (wide), tabs (phone) | both cities one tap away |
| military in a collapsible header chip | the framed centrepiece strip | it is the game's tension |
| empty art area | generated motif | readable without commissioned art |
| cards grouped under colour labels | stacks per colour, banners visible | the stack is the colour |
| Wonders as a text list | flat Wonder cards, same anatomy as buildings | they carry large effects |
| row 6 at the bottom | row 0 at the top, accessible row at the bottom | matches `Layout` |
| fixed player colours | you cyan, opponent bronze, per viewer | nobody tracks who is which colour |
| winner and score only | reason, plus breakdown when the wire has it | needs a backend item |
| host rematch / leave | platform's job | out of the game's scope |
| mouse only | keyboard-operable buttons, reduced motion | cheap |
| no phone layout | tabbed panels, portrait and landscape (§3) | owner's request |
| first player vote buttons | `chooseStarter` offers (already built) | the flow changed |

## 11. Order of work

13 — tokens, card/wonder components, icons, the pyramid and draft, the city stacks and the action
popover, the layout and breakpoints. 14 — cues from events. 15 — the military track, science, tokens,
pending prompts, end screen. 16 — the real-stack pass. Each plan opens with the screens it changes
shown as a mockup the owner reacts to before code is written.
