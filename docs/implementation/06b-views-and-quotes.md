# Per-Viewer Views and Quotes Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans (native, inline). Steps use checkbox (`- [ ]`) syntax.

**Goal:** A pure projection `Views.of(state, rules, viewer)` that gives each player (and each spectator) exactly the part of the game they may see, with what every takeable card and unbuilt Wonder would cost each player right now.

**Architecture:** (1) *Knowledge is finally written*: `Resolution.move` into a PUBLIC zone makes the component known to both players, so a card tucked under a Wonder stays known to the two players who saw it in the pyramid, and a box or deck card never is. (2) `view/Views.canSee(state, rules, viewer, component)`: PUBLIC zone → everyone; OWNER zone → the owner; otherwise the viewer's `knowledge`; spectators see PUBLIC zones only. (3) `DuelView` is a wire-safe record (records, `List`, `String`, `UUID`, `int`, `boolean`): visible placements carry the component id; hidden ones are *collapsed* into `(zone, key, count)` so neither the id nor the shuffled order of the deck leaks. (4) `QuoteView`s come from the same `CostSolver` the engine uses, so what the client shows is what `Engine` charges.

**Tech Stack:** Java 21, JUnit 6, AssertJ; same build as plans 1–6a.

**Spec:** `docs/rules/base.md` §10.8 (visibility and per-player knowledge), B-COST/B-TRADE (cost), `docs/design/core-mechanics.md`. GDK: `backend-gdk-api.md` §5.1 (state is a value), §9; `onResync` wiring is plan 6c.

**Builds on:** 1 (`DuelState.knowledge`, zones), 5a (`CostSolver`), 6a (`Scoring`). Decided with the user: quotes for both players; hiding driven by `DuelState.knowledge`; spectators see only public zones.

**Out of scope:** `onResync`/`sendToPlayer` wiring and the action codec (6c); per-event hidden-information effects (6c); knowledge writers (the base Great Library, Espionage, Athena: they call the same rule through their own `Resolution` primitive); a score display (the `Scorecard` already exists; 6c adds it to `GameOutcome`).

## Global Constraints

- New code in `view/`; one change in `effect/Resolution.move`. No game state is added; `knowledge` already exists and is already in the snapshot.
- `DuelView` and nested records use only GDK-legal types (§5.1); no `Optional`, no `Map` keyed by anything but `UUID`/`String`.
- A hidden placement never exposes its component id, and its order in the output is fixed by `(zone, key)`, never by placement order.
- Quotes: one per `(player, component)` for the uncovered face-up structure cards (`Structure.takeable`) and for each player's unbuilt Wonders, computed by `CostSolver.quote`; `affordable` = `quote.payableWith(coins)`.
- A viewer who is not a seated player is a spectator.

## Review Focus

- A face-down card, a deck card, a box card and the deck order are absent from every viewer's view, including spectators; nothing about them leaks through `toString`, the order of lists or counts that change when a card is swapped.
- A card tucked under a Wonder shows its identity to both players and not to a spectator... *(ruling: under a Wonder is a NONE zone; known players see it, spectators do not)*.
- A face-down card turning face up becomes visible to both players at once; a card the viewer has seen stays visible after it moves to a hidden zone.
- Quotes agree with `Engine`: a quote marked affordable is a `Build` the engine accepts, one that is not is rejected, for either player, for chained, trade and Strategy-style prices.
- Every card the engine would accept for `Build` has a quote for the active player, and no covered or face-down card has one.
- The view carries no secret through the agenda: a pending decision shows its decider and option ids only, and options are public components.

---

### Task 1: Seeing a card is remembered

**Files:** modify `effect/Resolution.java` (`move`), `state/DuelState.java` (`withKnowledge`, if absent). Test: `effect/ResolutionTest`.

**Interfaces:** Produces `DuelState.withKnowledge(Map<PlayerId, Set<ComponentId>>)`. `move` into a zone whose `ZoneKind.visibility()` is `PUBLIC` adds the component to both players' knowledge (no event).

- [ ] Failing tests: a card moved from the deck to `structure.up` is known to both; moved on to `underWonder` it stays known; a card moved deck → `box` is known to nobody; a second move into a public zone changes nothing. Red → green → commit `feat(game): a component seen in a public zone is remembered`.

### Task 2: Who can see what

**Files:** create `view/Viewer.java` (`sealed interface Viewer { record Seat(PlayerId player); record Spectator() }`), `view/Views.java` (`static boolean canSee(DuelState, RuleSet, Viewer, ComponentId)`). Test: `view/VisibilityTest`.

- [ ] Failing tests: PUBLIC zone visible to all three; `deck`/`structure.down`/`box` hidden to all; `underWonder` visible to a seat that knows it and hidden to a spectator and a seat that does not; an OWNER-visibility fixture zone is visible to the keyed owner only. Red → green → commit `feat(view): who can see a component (base.md §10.8)`.

### Task 3: The hidden parts collapse

**Files:** create `view/DuelView.java`, `view/PlacedView.java` (`record PlacedView(String component, String zone, String key)`), `view/HiddenView.java` (`record HiddenView(String zone, String key, int count)`); `Views.of` fills phase, active, age, first, deciding, players (`PlayerView(UUID id, int coins, boolean playAgain)`), pawn, victory (`winner`, `reason`), `visible`, `hidden`, `pending` (`PendingView(UUID decider, List<String> options)` for a decision on top of the agenda, else `null`). Test: `view/ViewsTest`.

- [ ] Failing tests with a started Age I: the deck appears only as one `HiddenView("deck","",n)`; the 5 face-down structure slots each appear as `HiddenView("structure.down", slot, 1)`; swapping two deck cards (same state otherwise) gives an identical view; the id string of a hidden card occurs nowhere in `view.toString()`; a spectator additionally loses `underWonder` contents. Red → green → commit `feat(view): per-viewer projection of the game`.

### Task 4: Quotes for both players

**Files:** create `view/QuoteView.java` (`record QuoteView(UUID player, String component, int coins, int tradeCost, int total, boolean chained, boolean affordable)`); `Views.of` adds `quotes`. Test: `view/QuotesTest`.

- [ ] Failing tests: one quote per player for each takeable card and each own unbuilt Wonder; none for covered or face-down cards; a chained card quotes `total 0, chained`; trade cost matches `Quote.tradeCost`; `affordable` agrees with `Engine.apply(Build)` accepting or rejecting for each player (parameterised over cheap, trade-priced and unaffordable cards). Red → green → `verify` → commit `feat(view): what each player would pay now`.
