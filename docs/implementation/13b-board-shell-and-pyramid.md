# Plan 13b — Board shell, breakpoints and the structure

**Why.** The first screen (plan 12) dumps zones as chips. This plan replaces `App.tsx` with the board of
`docs/design/frontend-visual-design.md` §3 and §7: header, track strip, structure, right panel, and the
breakpoints that rearrange them. It needs the card component of plan 13a.

## What is already settled

- Every `structure.up` / `structure.down` entry has the key `row:x` (`Slot.key()`, x in half-card
  units); row 0 is the top row and the bottom row is accessible. Hidden cards appear as `HiddenView`
  with the same key and a `back` group. Quotes exist only for takeable cards.
- Wide ≥ 900px; medium ≈ 600–900px (panel under the structure); phone portrait tabs
  Structure · Cities · Log; phone landscape side by side with a thin header (design §3).
- You are cyan, the opponent bronze, per viewer; a spectator sees seat 1 as "you" (left).

## Decisions

1. **Layout is a pure function.** `core/layout.ts`: `layoutFor({width, height}): Layout` →
   `"wide" | "medium" | "portrait" | "landscape"`, tested at the boundaries. A `useLayout(ref)` hook
   (ResizeObserver) feeds it the *container's* size, not `window`, because the dev harness shows three
   small iframes and the platform shows the game in a frame.
2. **Seats are derived once.** `core/seats.ts`: `seatsOf(session): {me, opp}` (player ids, and whether
   the viewer is a spectator). Components take `me`/`opp`, never compare ids themselves.
3. **The pyramid is geometry from keys.** `core/pyramid.ts`: `pyramidOf(view): {slots, rows, cols}`
   from every `structure.*` entry — position `{row, x}`, state `up | down`, the component or the back
   group — plus `fit(width): scale` for the container. Tested on the real fixtures (Age I, II, III
   layouts).
4. **The track strip is a stub here** (a framed placeholder with the pawn position as text); the track
   itself is plan 15. The strip exists now so the layout is right.
5. **Phase content**: the structure area shows the pyramid during an Age, the Wonder draft (reusing the
   same card component, 4+4 offers) during `WONDER_DRAFT`, and the starter choice during `deciding`.
   The old chips go away.
6. **Takeable cards** (quote present) glow and are buttons; the rest are dimmed and inert. Clicking
   one only *selects* it here; the popover is 13c.

## Tasks

1. `layoutFor` and `seatsOf`, test-first (boundaries, spectator, seat-1/seat-2 mapping).
2. `pyramidOf` and `fit`, test-first on `view-in-age`, `resync`, a late-Age fixture; assert row 0 top,
   x positions, hidden cards keep their slots, a removed card leaves a gap.
3. Shell: `<Board>`, `<Header>`, `<TrackStrip>` (stub), `<Structure>`, `<SidePanel>` with the log;
   breakpoints via CSS classes from `Layout`; the phone tab bar with the highlight-never-switch rule.
4. `<Pyramid>`, `<Draft>`, `<StarterChoice>` replacing the chips; selection state in a small reducer.
5. Playwright pass in the dev harness at 1280×800, 800×900, 390×844 and 844×390: nothing overflows, the
   pyramid is centred and fits, tabs behave, spectator layout correct. Findings in this plan.
6. Remove dead styles/components; checks clean.

## Done when

The dev harness plays a game from draft to Age II on the new board at all four sizes, only quoted cards
are interactive, and `App.tsx` no longer lists zones.

## Not in this plan

City stacks and the action popover (13c), the real track (15), motion (14).

## Outcome (2026-10-25)

Built as planned: `core/{layout,seats,pyramid,selection}.ts` and `ui/{Board,Header,TrackStrip,Structure,Pyramid,City,Log,useSize}.tsx`
with `ui/style/board.ts`. `App.tsx` is now only the "waiting" guard and `<Board>`. 133 frontend tests, bundle 222 KB (was 205).

**Rulings**

- `layoutFor` order: landscape first (wider than tall, shorter than 450px, at least 600 wide), then wide
  ≥ 900, medium ≥ 600, else portrait. 450 rather than 500: a laptop window of 750×487 is not a phone.
- Seats come from `view.players` order (first = seat 1), so a spectator's "me" is seat 1 without the
  roster. `seatsOf` is null until the first view.
- The pyramid frame never shrinks within an Age (`pyramidOf(view, floor)`, the floor held in a ref keyed
  by Age), so cards leaving do not move the rest. A taken card is simply absent.
- Scale fits width **and** height (`fit(box, extent)`), clamped to 0.3–1.25. Design said width only; a
  short frame (844×390) needs height too.
- Three states of a face-up pyramid card: **covered** (no quote for anyone: dimmed), **waiting** (a quote
  exists, not the viewer's turn: normal, inert), **takeable** (the viewer has an offer: glow, button).
  Design only named the first and last; without "waiting" every card dims on the opponent's turn.
- The Wonder draft and starter choice send their action directly (their offers are already single
  actions); only pyramid cards select first (design §5, popover in 13c).
- `attention()` returns "structure" for every offer today. Pending answers (destroy a card, pick a token)
  belong to a city or the strip and are re-pointed in plan 15 when those prompts exist.
- The city panel is a stub (coins, Wonder names) until 13c; the track strip is a text stub until 15.
- `groupZones` and its test are deleted: nothing renders zones as lists any more.

**Findings**

- The dev harness mounts the game in an unnamed `div`, so `height: 100%` on the board resolved to auto.
  It is now `#root`, and the board fills the frame.
- Checked at 1280×800, 800×900, 390×844 and 844×390 in the harness: layouts resolve wide, medium, portrait
  and landscape; no horizontal overflow; the pyramid fits and is centred in all four; the phone tab bar
  and header stay on screen. Age II's inverted pyramid draws correctly from keys alone.
- A covered row's name bar is hidden by the row in front (design §7's 30% overlap); the banner stays
  visible. Acceptable, revisit if it reads badly with real play.
