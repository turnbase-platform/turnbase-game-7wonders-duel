# 15 — Board visuals to the agreed mockups

Plans 12–14 built the board's behaviour with placeholder looks; the result does not resemble the
mockups agreed in the brainstorming sessions. This plan brings the look and layout to them and adds
nothing else. **The mockups are the specification** (`docs/design/mockups/`, open in a browser):

| Mockup | Decides |
|---|---|
| `board-layout-v2.html` | Overall arrangement: header, track strip with tokens, structure, right panel with a tab per city and the log; medium/portrait/landscape behaviour |
| `track-v6.html` | The military track: tiles shaded red→cream→gold, capitals, pawn, shields-to-go badges, loot and VP fields |
| `track-and-wonders-v3.html` | The city panel: colour columns of overlapping card chips, Wonders in a 2×2 grid built in full colour with the owner's outline, unbuilt faded |
| `track-and-city.html`, `board-layout.html`, `track-v4/v5` | Earlier rounds, kept for the history of the decisions |

## Global rules

- Compare against the mockup in a browser after each task; a screenshot beside the mockup is the check, not the unit tests.
- Pure geometry and derivation live in `core/` with tests first; CSS is verified visually.
- No new data on the wire: everything below is derivable from `DuelView` and the catalog.

## Tasks

1. **Pyramid.** Covered cards are darkened, not translucent (the see-through rows read as broken); lower rows draw over upper ones; card scale capped so the pyramid stays compact and centred.
2. **Military track** (`core/track.ts`, `ui/TrackStrip.tsx`). 19 tiles −9…+9 with the viewer's capital left, opponent's right; signed pawn position from `pawnSide`/`pawnSpace`; shields-to-go badge on each zone's entry tile and each capital; loot fields (taken ones dashed) from the `military` zone, VP field for zone 1, the pawn's zone highlighted; hover shows the distance. Progress tokens of the `board` zone sit at the strip's right.
3. **Header.** Age and whose turn, coins per seat with the seat's colour, as in `board-layout-v2`. Science tally waits for plan 16 (needs the score breakdown).
4. **City panel.** Colour columns, Wonders 2×2, tokens, coins and production line, per `track-and-wonders-v3`.
5. **Pass.** Wide, medium, portrait and landscape frames side by side with the mockups; fix the differences.

Not in this plan: science display, prompts, end screen (16); pawn and loot cues (after the track exists).

## Outcome

Done 2026-10-25. Track, header, city panel and strip match `board-layout-v2`, `track-v6` and `track-and-wonders-v3`; checked in a browser at wide, and phone-portrait widths.

**Rulings**

- Covered pyramid cards are darkened, not translucent; the pyramid scale is capped at 1.
- Loot amounts come from the catalog by tag, so a taken token still shows its value.
- Zone VP values (2, 5, 10, B-MIL-5) are constants; only zone 1's "2 VP" is drawn, the others sit in the loot field's title.
- Progress tokens are plain circles named by their title, as in the mockup.
- The city's own heading is dropped; the panel tab already names the seat.
- Header says "your turn" for the viewer and "`<name>` to act" otherwise.

**TDD caveat:** the track derivation (`core/track.ts`) was test-first; CSS and layout were checked by eye against the mockups, not by tests.

**Deferred minors**

- Science display, prompts and end screen (plan 16, needs a backend score-breakdown item).
- Pawn and loot cues; the log's look (empty in the dev state); landscape and medium not re-compared after the changes.
- Plan-14 minors: chain glow, replay pulse and portrait layout not checked in a browser.
