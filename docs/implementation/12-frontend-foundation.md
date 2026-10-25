# Plan 12 — Frontend foundation: dev loop, state pipeline, first screen

**Why.** The backend now sends everything a purely visual client needs: the view (plan 6), the
per-viewer events (plan 10) and the catalog (plan 11). Before drawing cards we need three things the
skeleton does not have: a way to see changes within a second, a typed pipeline from wire message to
UI state that is testable without a browser, and one real screen proving the pipeline end to end.
The existing `main.tsx`, `App.tsx` and `model.ts` are placeholders and are deleted.

## What the corpus and the SDK already settle

- **Protocol** (`frontend-gdk-api.md` §4–§9): `TurnBaseClient` is the only transport. `init` carries
  role, self, roster, teams — **no game state**. The first `game_event` after `init` is the resync
  message (view plus catalog); every later one is a view plus events. `sendAction` throws before
  `init`. A rejected action arrives as `kind: "rejected"` to the actor only.
- **Iframe limits** (`game-asset-serving.md` §6): scripts only from the bundle, `style-src 'self'
  'unsafe-inline'`, `img-src 'self' data: blob:`, `font-src 'self' data:`, `connect-src 'self'`.
  So no remote fonts or images, no inline `<script>`; CSS is injected by the bundle's JS and
  images/icons are inlined into `main.js`. The manifest names one entry point, `frontend/main.js`.
- **The SDK already ships a local harness** (`turnbase-harness`, `test-harness.md`; installed in
  `~/.m2`). It validates and serves a built bundle on two origins, with player tabs
  (`/?player=<name>`), a spectator, a debug panel and forced resync, and it reads the frontend from
  disk on every request. Limits found: the seed is `System.nanoTime()` (no repeatable position), it
  needs a built jar and bundle, and there is no HMR (rebuild, then reload).

## Decisions

Owner's (2026-10-25): a dev harness for the daily loop, one pass through the real stack at the end;
the FE is purely visual and reacts to events by component id.

My calls (each cheap to undo):

1. **Three tiers, not two.** (a) *Own dev harness* for daily work: Vite with HMR, fixed seed,
   fast-forward. (b) *SDK harness* over the built bundle (`esbuild --watch`, then
   `HarnessMain target/bundle`) as a cheap protocol, CSP and two-origin check before the real
   stack — it costs nothing to build. (c) The real stack last. The SDK harness cannot replace (a)
   because of the seed and the missing HMR.
2. **Dev harness is test-scope Java plus a Vite host page.** `DevServer` (under `src/test/java`,
   never in the jar) runs the real `SevenWondersDuelGame` in-process on a plain JVM with a fixed
   seed, behind a small `com.sun.net.httpserver`: `POST /action/{seat}` calls `onAction`, effects
   fan out per viewer as server-sent events (`GET /stream/{viewer}`, spectator included, same rule
   as the platform: spectators get broadcast only), `POST /new?seed=N` restarts, `POST
   /advance?steps=K` plays K random legal moves with the existing `Bot` so a screenshot can start in
   Age 2 or at a decision. The host page shows three iframes (seat 1, seat 2, spectator) and
   translates between them and the server using the **unmodified** `TurnBaseClient` protocol
   (`ready` → `init` → `game_event`). Same origin in tier (a); the two-origin check is tier (b)'s job.
3. **Wire types are hand-written TypeScript, guarded by golden fixtures.** The GDK has no schema
   generator. Java tests assert that sample messages (resync, action, rejection, one event of each
   kind) serialize to checked-in JSON under `src/test/resources/wire/`; vitest parses the same files
   against the TS types. A wire change then fails on whichever side forgot to follow.
4. **Pure core, thin React.** `core/` has no DOM and no React: `catalog` (id lookup),
   `session` (a reducer from `init`, `ServerMessage` and `player_left`/`game_over` to a `Session`),
   and `log` (events to human-readable lines using catalog names). `ui/` only renders a `Session`.
   Everything with logic is tested in vitest without a browser.
5. **The view is the truth; events explain it.** The UI always renders the latest view. Events feed
   the log now and, in plan 14, the choreography (cues keyed by component id, played as FLIP
   transitions from the previous DOM rectangle to the new one). Nothing in this plan animates.
6. **No Playwright dependency in the repo.** Visual verification is manual through the Playwright
   MCP tools in the session, against the dev harness; screenshots are looked at, not committed.
   A committed end-to-end suite is a separate decision.
7. **Styling is the game's own** (plain CSS, CSS variables, light/dark by `prefers-color-scheme`).
   The platform's Astryx design system does not apply inside a game iframe. The old frontend's icon
   sprite (`seven-wonders-duel/frontend/public/assets/icons.svg`) is the starting point for icons;
   it is read, not imported, and each icon used is copied into this repo with its source noted.

## Tasks

1. **Golden wire fixtures (Java, then TS types).** Test `WireFixturesTest` writes and compares
   `resync`, `view-with-events`, `rejected` and one `EventView` per kind. Add `src/main/ts/core/wire.ts`
   mirroring `ServerMessage`, `DuelView`, `EventView`, `CatalogView`, `Badge`, `WireAction`. Vitest
   `wire.test.ts` loads the fixtures. Confirm the exact JSON the GDK serializer produces (UUIDs,
   nulls, empty lists) from the fixtures, not from assumption.
2. **Core.** `catalog.ts` (`byId`, name, colour tag, badge lookup), `session.ts` (reducer; keeps
   `self`, `role`, roster, the latest view, the catalog, a bounded event history with message
   numbers, the last rejection, game over), `log.ts` (one line per top-level event, children folded
   under their cause). Tests first, one behaviour each: resync replaces everything; a view message
   keeps the catalog; a rejection does not change the view; the log names a hidden component as
   "a card" when `component` is null.
3. **Dev harness.** `DevServer` and its test (fan-out, spectator rule, restart, advance), the Vite
   config and host page, `npm run dev` (starts both; the Java side is launched from a classpath
   file produced once by `dependency:build-classpath`, so no new plugin is needed — verify in the
   task), `npm run dev:seed`. Done when three iframes show the game and an action in seat 1
   reaches seat 2 and the spectator, with seat 1 receiving the events of its own action.
4. **First screen.** Delete `App.tsx`, `model.ts`, `main.tsx`. New `main.tsx` wires `TurnBaseClient`
   to the reducer. `ui/` renders: header (phase, Age, whose turn, coins), the draft as a row of
   named placeholders per zone (clickable when it is the viewer's decision, sending the matching
   `WireAction`), the event log, the last rejection. Names and colours come from the catalog; hidden
   cards render as backs. This is deliberately plain: it proves clicks, views, events and
   rejections, and is what plans 13–15 replace piece by piece.
5. **Build and bundle.** `npm run build` injects the stylesheet from JS and inlines icons; `lint`,
   `typecheck`, `format:check`, `test` clean; `target/bundle/frontend/main.js` size noted. Run the
   built bundle in the SDK harness (tier b) with a short checklist: loads, `init` arrives, a draft
   click works, the spectator tab sees broadcast only, forced resync restores the screen, no CSP
   warning.
6. **Playwright pass (tier a).** Drive the draft with seat 1 and seat 2 in the dev harness, check
   that seat 2's view of seat 1's pick matches the event log, that the spectator never shows a
   hidden card's name, and that a deliberately illegal click shows the rejection. Record findings in
   this plan.

## Not in this plan

Plan 13 pyramid, cities and wonders drawn properly; plan 14 choreography and feedback from events;
plan 15 military track, science, progress tokens, end screen; plan 16 the real-stack pass (needs the
platform checkout and the seeded bundle). Responsive layout below tablet width, accessibility
beyond keyboard-operable buttons, and localisation are not planned.

## Open points

- The exact classpath launch for `DevServer` is decided in task 3 by trying it.
- Whether the resync should also carry recent events (a late-joining client sees no history) is
  deferred from plan 10; the log starts empty after a resync.

## Outcome (2026-10-25)

Built as planned; `npm run dev` (or `scripts/dev.sh [seed]`) starts `DevServer` and Vite, and
`http://localhost:5173/dev/` shows seat 1, seat 2 and a spectator over the same in-process game.
Bundle: 199 KB, stylesheet injected from JS, no icons yet.

**Findings from the Playwright pass (tier a) and the SDK harness (tier b):**

- A pick in one seat reached the other seat and the spectator, each with exactly one log entry
  (the `broadcast` flag keeps a seat from filing its own action twice). An out-of-turn action was
  refused to the actor alone; the spectator shows hidden cards as counts only.
- Bug found and fixed: `structure.up` holds every face-up card, covered ones included. Only the
  cards with a quote may be taken, so offers are derived from the quotes (`moves.ts`).
- The bundle runs in the SDK harness on two origins (`-Dturnbase.harness.dir=target/bundle`, the
  unpacked bundle, not the zip) with no console errors; forced resync restores the screen.
  A second tab opened in the background by automation does not load its iframe until it is the
  selected tab; that is the browser's throttling, not the game.
- Log noise for plan 14: an uncovered pyramid card reads "moved from the pyramid to the pyramid".
  The phrase table should say "was uncovered" for `structure.down` → `structure.up`.
- Not yet driven: the build-wonder offer (needs the card-then-wonder choice UI, plan 13) and a
  game over screen (plan 15).
