# Plan 14 — Choreography

Spec: `docs/design/frontend-visual-design.md` §9 (cues, FLIP, skippable, reduced motion, under about 1.2 s a move).

**Goal:** an accepted action is no longer a jump cut: the card travels, an uncovered card turns, coins float,
a chain lights up, the turn marker pulses. The view stays the truth; cues only explain how it got there.

## What the player sees

```
Build Palisade (Seat 2)     the card lifts from the pyramid slot, lands on the brown stack      ~400 ms
                            the slot behind it: its neighbours flip face-up ("was uncovered")  ~300 ms
                            "-2" floats off Two's purse and the count ticks down               ~500 ms, with the move
Discard                     the card travels to the discard pile corner and fades              ~350 ms
Build Wonder                the card slides under the Wonder, which gets its outline; coins float
Draft pick                  the Wonder card moves from the draft row to the city
Free by chain               the chain glyph on the built card lights for ~600 ms
Play again                  the seat's name in the header pulses once
Opponent's move             the same, played for the viewer; a spectator sees both
```

## Decisions

1. **`core/cues.ts` is pure.** `cuesOf(events): Cue[]` maps events to `travel | flip | coins | trade | chain | turn`
   cues in event order with their parent, no DOM. Tested in vitest like `log.ts`.
2. **FLIP by `data-component`.** After every commit a hook stores each `[data-component]` rectangle. On the next
   commit that brought a step, each travel cue animates its element from the stored rectangle to the new one with
   the Web Animations API (`transform` and `opacity` only). No animation library.
3. **No origin rectangle, no travel.** A component that was not on screen before (deck, discard, box, hidden
   cards) fades and scales in. Anchoring to the discard pile or deck is not in this plan: neither has a drawn
   place yet.
4. **Interruption never queues.** A new message, a click or a key finishes running animations instantly
   (`finish()`), then the new step plays. `prefers-reduced-motion` skips playing at all.
5. **Durations are tokens** in `ui/motion.ts` (travel, flip, float, pulse); nothing else holds a number.
6. **Pawn and loot cues move to plan 15.** The track strip is still a text stub; the pawn slide is built with it.
7. **Log noise fixed here.** `structure.down` → `structure.up` reads "was uncovered" (plan 12 finding).

## Tasks

1. **Cues (test first).** `core/cues.ts` and `cues.test.ts`: one event of each kind, parent nesting, a reveal vs a
   move, a spectator's events (no hidden names). Also the "was uncovered" log phrase, with a test.
2. **FLIP hook.** `ui/useChoreography.ts`: rectangle snapshot, play, finish on interrupt, reduced-motion bypass.
   Tested in jsdom with a stubbed `animate` that records calls (jsdom has none): which element, from/to, order.
3. **Wire it in.** `Board` passes the newest step to the hook; floating coin amounts and the chain/turn pulses are
   small components keyed by cue. Styles use the motion tokens.
4. **Playwright pass** in the dev harness at wide and portrait: build, discard, wonder, draft, chain, play again,
   a spectator frame, rapid clicks (no stuck or doubled card), reduced motion. Record findings.
5. **Checks and close-out:** `./mvnw -o verify`, lint, format:check, typecheck, test, build; Outcome section; INDEX.

## Open points

- Whether a discarded card should fly to a visible discard pile instead of fading (needs a place for it, 15).
- A cue played from a resync (reconnect) is never shown: a resync carries no events.
