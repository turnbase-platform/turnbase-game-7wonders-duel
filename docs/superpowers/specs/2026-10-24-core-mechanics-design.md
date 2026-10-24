# 7 Wonders Duel — core mechanics design

Status: draft for approval · 2026-10-24 · scope: backend object model (state, zones, catalog,
effects and modifiers, agenda, flow, cost solver, views and events). Not in scope of *this
document*: the React frontend, entering the card data (a later step, §5a), test plans.

## 1. Intent

Model the *mechanics* of 7 Wonders Duel — not its cards — so that the base game is implemented
once, cleanly, and every expansion (Agora, Pantheon, Extra Wonders, future ones) arrives as **added
content and added rules, never as edits to core code**. Cards, Wonders and tokens are declarative
data over a small set of shared mechanics.

Source of truth for the rules: `docs/rules/` (`INDEX.md`, then `base.md` §10). Rule IDs (`B-…`,
`A-…`, `P-…`, `X-…`) are cited in code and tests.

## 2. Goals, non-goals, anti-goals

**Goals**
- Base game complete and correct; expansions pluggable through `ContentPack`s.
- One source of truth for rules: the server validates with the same functions that produce what the
  UI shows (legal actions, cost quotes, scores).
- The frontend can explain *what happened and why* from the data it receives, with no rule
  knowledge.
- Deterministic, replayable, serializable state.

**Non-goals**
- Implementing any expansion now. Expansion content with missing costs or names is not a blocker and
  is never invented.
- Leaders, and any content the rules corpus excludes.
- Performance tuning beyond "obviously not wasteful".

**Anti-goals** (a failure even if the game "works")
- A per-card `if`/`switch` in core code.
- Derived facts (production, chains, scores, accessibility) stored in state and drifting from
  ownership.
- A client that re-implements a rule.
- Hidden information reaching a client that may not see it — including through quote prices or log
  text.
- An abstraction built for a hypothetical that no rules document describes.

## 3. Constraints

- Java 21 in Espresso: no Java 22+ APIs, no I/O, no clocks, no unseeded randomness
  (`backend-gdk-api.md` §9). All randomness from `InitContext.seed()`; the RNG position is part of
  the state.
- **The domain model does not bend to the GDK's serialization rules.** The engine uses ordinary
  OOP: typed ids (`ComponentId`, `ZoneId`, `PlayerId`), sealed interfaces, value objects, rich
  collections. A separate **snapshot layer** converts between that model and
  `SevenWondersDuelState`, a flat record that obeys `backend-gdk-api.md` §5.1 (records, `List`,
  `Map` with `String`/`UUID`/enum keys, `String`, `UUID`, enums, boxed numerics; no interface-typed
  fields; ordered data as `List`). Only the snapshot package is subject to those rules, and a guard
  test enforces them there. Conversion is lossless: `fromSnapshot(toSnapshot(x)).equals(x)`.
- The GDK's `onAction(S state, …)` receives the snapshot, the game maps it to the domain model,
  applies the action, and maps the result back. The snapshot is the only thing that crosses the
  platform boundary.
- The GDK may be improved where it hinders game authors (we own it). A candidate is a game-supplied
  state codec, which would make the snapshot layer unnecessary; that is a later SDK item, not a
  prerequisite for this work.
- Depends on `turnbase-sdk` only. SDK gaps are fixed in the SDK, not worked around here.
- `SevenWondersDuelState` is what the round-trip test serializes.
- Output reaches players only through GDK effects (`Broadcast`, `SendToPlayer`, `Log`, `GameOver`).
  `LogEffect` is a public, platform-managed string with basic Markdown and no parameters.

## 4. Design

### 4.1 State — an immutable value, ownership is the only truth

```java
// domain model (free OOP)
record DuelState(Phase phase, PlayerId active, List<Placement> placements,
                 Map<PlayerId, PlayerState> players, Board board, Military military,
                 Agenda agenda, Map<PlayerId, Set<ComponentId>> knowledge,
                 RngCursor rng, List<PackId> packs)
record PlayerState(int coins, boolean playAgain, ...)
record Placement(ComponentId component, Location at)
// GDK boundary: SevenWondersDuelState — the same data as flat, serializer-safe records
```

- **Stored:** placements, coins, pawn position, agenda, per-player `playAgain` flag, per-player
  knowledge (which hidden components a player has seen), RNG cursor, enabled packs, active player.
- **Derived, never stored:** production, chains, science symbols, scores, accessibility, legal
  actions, costs.
- **`playAgain` is per player** (`base.md` B-TURN-6): a flag, not a counter, settable on either
  player (Shwedagon Pagoda) and consumed at the end of the holder's next turn.

### 4.2 Zones — data-driven

Every card, Wonder and token has exactly one `Location(ZoneId zone, String key)`, e.g.
`("city","P1")`, `("structure","3:4")`, `("underWonder","pyramids")`.

A pack registers `ZoneKind(id, visibility, ordered, terminal)`. Visibility decides who may see a
component's *identity* in that zone (public / owner only / nobody) and drives view redaction
automatically.

Base zones: `deck(age)`, `structure`, `city(player)`, `underWonder`, `discard`, `draft`,
`player.unbuilt`, `board`, `box`, `destroyed`.

- **`box`** — out of the game since setup: removed cards, progress tokens, undrafted Wonders, unused
  Guilds. Effects that "draw from what was removed at setup" are **queries over `box`** by component
  type (and Age for cards).
- **`destroyed`** — terminal: the engine rejects any move out of it. Holds what leaves play for good
  (used military tokens, Wonders removed by the limit or by an effect). Never a draw source.

Movement is a single engine operation `move(id, to)` that validates terminality and emits a
`ComponentMoved` event. Steal, swap, tuck, un-build and discard are all moves.

### 4.3 Catalog and content packs

`Component` (id, name, `effects()`) with subtypes `CardDef`, `WonderDef`, `TokenDef` (later
`DivinityDef`, `DecreeDef`, …). Definitions live in the catalog, never in state; state refers to
them by `ComponentId`.

A `ContentPack` contributes definitions, structure layouts, zone kinds, phases, turn hooks, extra
player actions, and cost-solver steps. The game builds one `RuleSet` from the packs listed in the
state. Base game is the first pack.

### 4.4 Effects and modifiers

| Kind | Interface | Role |
|---|---|---|
| Instant | `Effect.apply(Resolution)` | Changes state, emits events, may push steps. Permanent once applied (B-EFF-1) |
| Modifier | narrow interfaces: `ProductionSource`, `PriceModifier`, `CostReduction`, `ChainSource`, `SymbolSource` | Queried, never applied; collected from everything a player owns |
| Trigger | `Trigger<E extends GameEvent>` | Reacts to an event type while its source is owned |
| Scoring / victory | `ScoringRule`, `VictoryCondition` | Final score lines; supremacy checks on events |

Triggers run through a small event bus: active player's first, then the opponent's (B-EFF-4).
A card is declarative data, e.g. `Stable = {cost: Wood, chainOut: Horseshoe, effects: [Shields(1)]}`.

### 4.5 Player actions and the agenda

- `PlayerAction` (sealed): `Build`, `Discard`, `BuildWonder`, `DraftWonder`, `ChooseStarter`,
  `Answer(decisionId, choice)`, plus pack-added actions.
- **One agenda**: a stack of `Step`s, each an `EffectStep` (resolves automatically) or a
  `Decision(owner, options)` (waits for that player; the owner may be the opponent).
- The engine pops steps, resolving effects until it reaches a decision, then returns. `Answer`
  resumes the loop. Follow-ups are pushed to the front: deterministic, depth-first, printed order.
- A `Step` is a sealed interface in the domain (`EffectStep`, `DecisionStep`) holding
  `EffectRef(sourceId, index)` and the choices bound so far; the snapshot layer flattens it to a
  kind-tagged record.
- Infeasible options are not offered; an effect with no feasible option does nothing (B-EFF-3).

### 4.6 Flow

`Phase` objects (`WonderDraft`, `AgeStart`, `Turn`, `Scoring`, …) each provide
`legalActions(state, viewer)` and `handle(state, action) → Transition`. Packs add turn-start hooks
and extra actions. Supremacy is checked at the points in `base.md` §10.9 and ends the game
immediately.

### 4.7 Cost solver

A pure `CostSolver.quote(state, player, target) → CostQuote`, built as an ordered list of
`CostStep`s following `base.md` §10.2 (chain, coin cost, resource reduction, production, unit price,
payee, post-payment hooks). Packs insert or replace steps.

```java
record CostQuote(ComponentId target, boolean payable, int coins, int shortfall,
                 List<PaymentLine> breakdown, Optional<ChainId> freeBy)
record PaymentLine(Source source, Resource resource, int units, int coins) // BASE, TRADE, CHAIN, REDUCTION, DISCOUNT(componentId)
```

The same function validates `Build`/`BuildWonder` and feeds the view.

### 4.8 What the players receive

| Tier | Contents | Delivered as |
|---|---|---|
| State | Authoritative; never sent | — |
| View | State projected for one viewer, hidden identities redacted, plus engine-derived data: **quotes**, legal actions, accessibility, scores, science progress, trade prices | snapshot after every action and on `onResync` (`SendToPlayer`) |
| Events | What just changed, as a **causation tree** | `ActionResolved{actor, action, events[]}` |

- **Events** carry `id`, `parentId` and `source` (component + effect index), so the frontend can
  render "Colossus → +2 shields → pawn 3→5 → Loot 2 → opponent −2$". The same `GameEvent` stream
  drives triggers, UI animation and the log.
- **Delivery:** one `Broadcast` when nothing in the action is viewer-dependent; otherwise one
  redacted `SendToPlayer` per player.
- **Log:** `LogEffect`, one line per meaningful event of the **public projection**, with a cause
  suffix (e.g. "(Loot 2)"). Hidden facts are never logged ("drew 3 tokens", not which).
- **Quotes** cover **every face-up card in the structure and every unbuilt Wonder, for both
  players**, computed from the current state whether or not the card can be built now (so the UI can
  show "opponent is 3$ short"). **Face-down cards are not quoted**: a price would leak identity.

## 5. Decisions made (and by whom)

Decided with the user: behaviour by id in a catalog, not in state · one unified agenda · event-driven
triggers · `Map` allowed in state · `Box` and `RemovedAtSetup` merged, with a terminal `Destroyed`
zone for in-play removals · zones are data-driven · `playAgain` per player · snapshot view plus
events after every action · quotes for all face-up cards and unbuilt Wonders, both players, from
current state, none for face-down cards · events as a causation tree, log from the public
projection.

## 5a. Calls made by the author (please check)

- **Card data is a later step, not part of this design.** This document fixes the mechanics. The
  base game's full data (cards, Wonders, tokens, Guilds: `base.md` §3) is entered afterwards as
  declarative catalog content; the engine is first built against a small fixture catalog, and "base
  game playable end to end" is the milestone that closes the base work.
- **Extension points: build what the base game itself uses, no more.** The base game is the first
  `ContentPack`, so the pack registry, `ZoneKind` (including terminal `destroyed`), phases, cost
  steps, triggers and per-player `knowledge` (Great Library) all have a base-game user. Turn-start
  hooks, extra player actions and un-build are **not** built until a pack needs them; the `Turn`
  phase is a pipeline of named stages so adding a hook is an addition, not a rewrite.
- **Out-of-turn answers are supported by the GDK.** `ActionContext` identifies the acting player
  and the platform enforces no turn order, so the game validates "may this player act now" (own
  turn, or owner of the top `Decision`). No SDK change is needed.
- **Base needs `destroyed`, not un-build.** Base has no Wonder-removing effect; `destroyed` holds
  used military tokens and Wonders removed by the 7-Wonder limit. Un-build ships with its pack.
- **View and Events are plain records in a `view` package**, serialized as JSON by the platform.
  Their shape is the contract with the frontend; it is documented there when the frontend is
  designed, not frozen now.
- **"Replayable" means deterministic, not logged.** Same seed and same actions give the same state;
  the state round-trips through serialization. No action history is stored in state.
- **Quotes cover every face-up card in the structure, covered or not**, and every unbuilt Wonder,
  for both players.

## 6. Left to the builder

Package layout inside `io.turnbase.games.sevenwondersduel`; exact record fields and naming; the
event bus mechanics; the serialization form of `Location` keys and `Step`s; how the `RuleSet` is
assembled; test strategy.

## 7. Open points

- **UNBUILD semantics:** Anubis and Easter Island un-build a Wonder back to its owner's unbuilt
  Wonders (`pantheon.md` P-FAQ-4), while Sabotage and Atlantis Destroy it. `extra-wonders.md` §3's
  tag table now says both; confirm per Wonder when those packs are specified.
- **Snapshot size** is assumed small; revisit only if a payload approaches the 10 MB effect limit.
