# GDK Wiring Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans (native, inline). Steps use checkbox (`- [ ]`) syntax.

**Goal:** `SevenWondersDuelGame` implements the four GDK hooks over the engine: a seeded two-player game, wire actions decoded and applied, per-seat views plus a public broadcast after every accepted action, private rejection of illegal actions, forfeits, and a structured `GameOutcome`.

**Architecture:** (1) `wire/WireAction` is one flat record (the GDK has no polymorphic deserialization, `backend-gdk-api.md` §5.1) and `wire/WireActions.decode` maps it to a `PlayerAction`, throwing `ActionDecodeException`. (2) `SevenWondersDuelGame(List<ContentPack>)` holds the rule set (no-arg constructor = `BasePack`; tests inject fixture packs). (3) Each hook is `snapshot → DuelState → Engine → snapshot`; the snapshot gains `teams` (player → team) because `GameOutcome` needs team ids and the engine does not. (4) After an accepted action: `sendToPlayer` each seat its `DuelView` and `broadcast` the spectator view, wrapped in `ServerMessage`. An illegal or undecodable action returns the state unchanged plus a `sendToPlayer(actor, rejected)`. (5) `wire/Outcomes.of(state, rules, teams)` builds the `GameOutcome` from `Victory` and `Scorecard`.

**Tech Stack:** Java 21, JUnit 6, AssertJ; GDK `turnbase-sdk` (read-only). GDK contexts are implemented as small test doubles.

**Spec:** `backend-gdk-api.md` §3–§5.1, §7–§9; `docs/rules/base.md` §10.8, B-END-1..3. Decided with the user: per-seat views + public broadcast; illegal actions rejected privately with state unchanged.

**Builds on:** 1 (`Snapshots`), 4b (`Setup.newGame`), 5 (`Engine`), 6a (`Scoring`, `Victory`), 6b (`Views`, `DuelView`).

**Out of scope:** the React frontend and bundle manifest; base-game content (`BasePack.components()` is still empty, so the default constructor cannot start a real game yet); `onPlayerJoined` (default kept); per-event animation payloads; card-back groups in `HiddenView`.

## Global Constraints

- No I/O, clocks or unseeded randomness; the seed is `InitContext.seed()`.
- Wire payloads (`ServerMessage`, `WireAction`) use only GDK-legal types; no `Optional`, no interface-typed fields, no `Map` keyed by anything but `UUID`/`String`.
- A view sent to a seat is `Views.of(.., Seat)`; the broadcast is `Views.of(.., Spectator)`. Nothing else leaves the game.
- A hook throws only for bugs; every player mistake is a private `rejected` message.
- `Victory.Reason` gains `FORFEIT` (winner required, like MILITARY/SCIENCE).

## Review Focus

- A rejected action changes nothing and tells only the actor; the opponent and spectators receive nothing (no broadcast).
- The broadcast payload never contains a seat's private knowledge (a card tucked under a Wonder, a secret pending option).
- Decoding is total: unknown `type`, missing or extra fields, a malformed id, or an `answer` with both or neither of `component`/`player` yield `ActionDecodeException`, never a stray `NullPointerException`.
- A player who is not seated (a spectator, a stranger) cannot act; acting after the game is over is rejected.
- The game-over effect is emitted exactly once, on the action that ends the game; a forfeit after the game is over changes nothing.
- A shared points victory gives `draw = true` and both teams placement 1; a decisive win gives 1 and 2.
- The state after every hook survives the GDK serialization round trip, `teams` included.

---

### Task 1: The wire action

**Files:** create `wire/WireAction.java`, `wire/WireActions.java`. Test: `wire/WireActionsTest`.

**Interfaces:** Produces `record WireAction(String type, String component, String wonder, UUID player)` and `static PlayerAction WireActions.decode(WireAction)`. Types: `draftWonder` (component), `chooseStarter` (player), `discard` (component), `build` (component), `buildWonder` (component = card, wonder), `answerComponent` (component), `answerPlayer` (player). Anything else, or a missing/extra field for the type, throws `ActionDecodeException`.

- [ ] Failing tests: each type decodes to its `PlayerAction`; unknown type, null action, missing field, extra field each throw `ActionDecodeException` naming the type. Red → green → commit `feat(wire): the flat wire action and its decoder`.

### Task 2: Teams in the state and `onInit`

**Files:** modify `SevenWondersDuelState` (add `Map<UUID, UUID> teams`), `snapshot/Snapshots` (empty `teams` on `toSnapshot`; `fromSnapshot` ignores it), `SevenWondersDuelGame` (constructors, `onInit`). Test: `GameInitTest`, `snapshot/SnapshotsTest`, the existing round-trip test.

**Interfaces:** `SevenWondersDuelState.withTeams(Map<UUID,UUID>)`; `SevenWondersDuelGame()` = `BasePack`; `SevenWondersDuelGame(List<ContentPack>)`. `onInit` needs exactly two players in `ctx.teams()` (any split), seeds `Setup.newGame(rules, ctx.seed(), a, b)` with `a`,`b` in team order, records `teams`, no effects.

- [ ] Failing tests: two players → a `SevenWondersDuelState` in `WONDER_DRAFT` with both seats and `teams`; same seed → equal state, different seed → different; one or three players throw `IllegalArgumentException`; snapshot round-trip keeps `teams`. Red → green → commit `feat(game): onInit seeds a two-player duel`.

### Task 3: `onAction` — accept

**Files:** create `wire/ServerMessage.java` (`record ServerMessage(String kind, DuelView view, String reason)`, kinds `view`, `rejected`); modify `SevenWondersDuelGame`. Test: `GameActionTest`.

**Interfaces:** after an accepted action, effects are `sendToPlayer(seat, ServerMessage.view(Views.of(.., Seat)))` for both seats (sorted by id) and one `broadcast(ServerMessage.view(Views.of(.., Spectator)))`. State returned is `toSnapshot(next).withTeams(old.teams())`.

- [ ] Failing tests: a legal draft pick returns the new state, two `sendToPlayer` (one per seat, each equal to that seat's view) and one broadcast equal to the spectator view; the broadcast never mentions a hidden id. Red → green → commit `feat(game): onAction applies and shows the result`.

### Task 4: `onAction` — reject

- [ ] Failing tests: wrong turn, unaffordable build, undecodable payload, unseated actor and an action after game over each return the same state object content and exactly one `sendToPlayer(actor, rejected)` carrying the reason, no broadcast. Red → green → commit `feat(game): illegal actions are rejected privately`.

### Task 5: `onResync`

- [ ] Failing tests: a `PLAYER` viewer gets `Views.of(.., Seat)`, a `SPECTATOR` gets the spectator view; a `PLAYER` role with an unseated viewer is treated as spectator; the call leaves the state untouched and is repeatable. Red → green → commit `feat(game): onResync gives each viewer their own picture`.

### Task 6: Game over and the outcome

**Files:** create `wire/Outcomes.java`; add `Victory.Reason.FORFEIT`; modify `SevenWondersDuelGame` (`onAction` ends the game, `onPlayerLeft`). Test: `wire/OutcomesTest`, `GameOverTest`.

**Interfaces:** `static GameOutcome Outcomes.of(DuelState, RuleSet, Map<UUID,UUID> teams)`: a team result per seat, placement 1 for the winner and 2 for the loser; for a shared victory both 1 and `draw = true`. Stats per player: `reason`, each `Scorecard` line, `total`. `gameOver(outcome, ServerMessage.view(spectator view))` is appended to the effects of the action that first sets `Victory`. `onPlayerLeft`: if the game is running, `Victory(opponent, FORFEIT)`, phase `OVER`, views and game-over effect; if already over, state unchanged, no effects.

- [ ] Failing tests: decisive and shared outcomes; a game-ending action carries one `gameOver`; a following rejected action carries none; forfeit awards the opponent; forfeit after the end is a no-op; `FORFEIT` without a winner is rejected by `Victory`. Red → green → commit `feat(game): game over, outcome and forfeit`.
