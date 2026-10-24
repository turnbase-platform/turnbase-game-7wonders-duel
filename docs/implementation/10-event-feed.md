# Plan 10 — The event feed

**Why.** The frontend needs to say what just happened (a card was built, coins moved, the pawn
advanced, a chain paid out), not only what the table looks like now. The engine already records
that as the causation tree (`Outcome.events()`); `SevenWondersDuelGame` drops it and ships only
`view` snapshots. A client that diffs two views cannot attribute causes (coins from a trigger vs
from the card) and cannot tell a reveal from a hidden move.

**Decision (owner, 2026-10-25).** Each accepted action sends the new view **and** the events,
filtered per viewer. The view stays the single source of truth for rendering (so a resync is always
right); the events only drive feedback.

## Wire

`ServerMessage` gains `events` (a list, empty for a resync and for a rejection). `EventView` is a
flat record, like `WireAction`, because the GDK has no polymorphic serialization:

| `kind` | fields used |
|---|---|
| `moved` | `component` (null when the viewer cannot identify it), `from`, `to` |
| `coins` | `player`, `amount` (the delta) |
| `trade` | `player`, `amount` (paid to the bank) |
| `chain` | `player`, `component` (the card built through a chain) |
| `pawn` | `pawnFrom`, `pawnTo` |
| `playAgainGranted` / `playAgainCleared` | `player` |

Every event also carries `id`, `parent` (the causation tree) and `cause` (the component whose effect
or trigger emitted it; null for the player's own action or when the viewer cannot identify it).

## Hidden information

An identity appears in an event exactly when the viewer could read it off the view of the state
after the action (`Views.canSee`): public zone, own zone, or seen before. Otherwise the field is
null. Places (`from`, `to`) are never redacted: the view's `hidden` counts show them anyway.

## Tasks

1. `EventViews.of(events, after, rules, viewer)` and the three view records, tested per kind and
   per viewer (seat, opponent, spectator).
2. `ServerMessage.events`; `SevenWondersDuelGame.shown` sends each viewer its own events. The
   public broadcast carries the spectator's events. Resync and rejection carry none.
3. Playthrough invariant: across the seeded games no seat's events name a component its view
   hides.

## Not in this plan

Animations, the log panel and any FE code (plan 11).
