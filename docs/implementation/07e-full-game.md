# Full Game Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans (native, inline). Steps use checkbox (`- [ ]`) syntax.

**Goal:** Play complete seeded games over the real base content, end to end, and assert the invariants that unit tests over fixture catalogues cannot: every legal action resolves, nothing is lost or duplicated, every game ends, and a seed fixes the game.

**Architecture:** A test-only `Bot` lists the legal actions for whoever must act and picks one with a seeded `java.util.Random` under a *policy* (random, scientist, warmonger). `Playthrough` drives `Engine` from `Setup.newGame` to `Phase.OVER` and hands every intermediate state to an invariant check. `FullGameTest` runs it over many seeds and policies; `GdkFullGameTest` plays one game through `SevenWondersDuelGame` (`onInit`/`onAction`) to cover the wire and per-viewer views. No production code is expected to change; a defect found is fixed test-first in its own commit and ledgered.

**Tech Stack:** Java 21, JUnit 6, AssertJ. Test code may use `java.util.Random` (the clock/randomness ban is for production code).

**Spec:** `docs/rules/base.md` §5 (setup), §6 (turn), B-END-1..3, B-MIL-3, B-SCI-1..3.

**Builds on:** 7a-7d (the whole base catalogue), 6c (`SevenWondersDuelGame`), `Scoring`, `Snapshots`.

**Out of scope:** UI; expansions; performance; deleting the fixture-based unit tests (they isolate mechanics; this plan adds the real-content layer beside them).

## Global Constraints

- Test code only under `src/test/java/.../playthrough/`, unless a defect forces a fix.
- Legal actions are derived from game state through public APIs (`Engine`, `CostSolver`, `Structure`, `Draft`, `Placements`); the bot never reaches into engine internals, and illegal-action exceptions are test failures, not control flow.
- A game must end within a step cap (400 actions); exceeding it is a failure naming the seed and policy.
- Failures print seed, policy and step so any run is reproducible.

## Review Focus

- Termination: every seed under every policy reaches `Phase.OVER` with a `Victory`; no pending decision or empty-bot dead end.
- Conservation: every catalogue component is placed exactly once at every step, in a zone the registry knows; destroyed and Box contents never return.
- Coins never negative; at most seven Wonders ever built; each Wonder built at most once.
- Points victories: `Victory.winner` agrees with `Scoring.score` totals and tie-breakers (B-END-3); military victory has the pawn on a capital; science victory has six different symbols for the winner.
- Determinism: same seed and policy give an equal final `DuelState`; a snapshot round trip at a random mid-game step changes nothing.
- Hidden information: through the GDK, neither player's view ever lists the id of a face-down structure card or an unknown Box token.

---

### Task 1: Bot and Playthrough

**Files:** create `src/test/java/.../playthrough/Bot.java`, `Policy.java`, `Playthrough.java`; test `PlaythroughTest.java`.

**Interfaces:**
- Produces: `Policy` enum `RANDOM`, `SCIENTIST` (prefers building green cards), `WARMONGER` (prefers building red cards); `Bot.legalActions(RuleSet, DuelState) -> Map<PlayerId, List<PlayerAction>>` (one entry: who acts); `Playthrough.play(RuleSet, long seed, Policy) -> Result(DuelState finalState, List<DuelState> states, int steps)`.

- [ ] Test first: `legalActions` in the draft lists exactly the offered Wonders for the player to move; when the starter is undecided lists a `ChooseStarter` per seated player for the deciding player; with a pending decision lists an `Answer` per option; in an Age lists `Discard` for every takeable card plus affordable `Build` / `BuildWonder` per unbuilt Wonder while the limit allows. Every listed action is accepted by `Engine.apply`.
- [ ] Test first: `Playthrough.play` is deterministic for (seed, policy) and stops with a clear message at the step cap.
- [ ] Implement; `spotless:apply`, `verify`, commit.

### Task 2: Invariants over many seeds

**Files:** create `playthrough/Invariants.java`, `FullGameTest.java`.

**Interfaces:**
- Consumes: Task 1 `Playthrough`.
- Produces: `Invariants.check(RuleSet, DuelState)` throwing `AssertionError` with a description.

- [ ] Tests first (each a `@ParameterizedTest` over seeds 1..120 × the three policies unless noted):
  - every game reaches `OVER` with a `Victory`;
  - conservation: placed ids equal the catalogue ids, each once, at every state;
  - coins ≥ 0; ≤ 7 Wonders in `wonders.built` ever; Box and destroyed zones only ever grow;
  - POINTS: winner agrees with `Scoring.score` totals (tie on points broken by blue points, else shared); MILITARY: pawn on a capital space; SCIENCE: winner holds six different symbols;
  - same seed and policy twice give equal final states;
  - across the whole run each of MILITARY, SCIENCE and POINTS occurs at least once, and every progress token and every Wonder is acquired/built at least once (this proves the bots reach the content; if a policy cannot, widen the policy, never weaken the assertion).
- [ ] Implement `Invariants`; run; any production defect found is fixed test-first in a separate commit and ledgered as a Ruling.
- [ ] `spotless:apply`, `verify`, commit.

### Task 3: One game through the GDK

**Files:** create `GdkFullGameTest.java` in the game package, reusing `GdkDoubles`.

**Interfaces:** Consumes Task 1 `Bot` over a `DuelState` decoded from the snapshot; `WireAction` for the encoding.

- [ ] Test first: play one seeded game (seed 11, `RANDOM`) through `onInit`/`onAction`, encoding each bot action as a `WireAction`; it ends with a `GameOverEffect` exactly once; after every action the state decodes to the same `DuelState` the engine produced; no view sent to either player contains the id of a card the viewer cannot see (face-down structure cards, Box tokens), checked against the viewer's own knowledge.
- [ ] Implement helpers only; `spotless:apply`, `verify`, commit; register this plan in `docs/INDEX.md`.
