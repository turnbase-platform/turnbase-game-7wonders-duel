# turnbase-game-7wonders-duel — CLAUDE.md

7 Wonders Duel for TurnBase. **A consumer of the published GDK, not part of the platform.** Currently a
skeleton cloned from the shape of `turnbase-game-tictactoe`; the frontend is React 19.

**This file is additive** — corpus, registry and conventions live in the parent repository
(`../docs/implementation/conventions.md`).

- The enforcer bans `turnbase-game-host` and `turnbase-grpc`. If the game cannot be written against
  `turnbase-sdk` alone, the fix is in the SDK or its spec, not here.
- The backend is Java 21 and runs in Espresso: no Java 22+ APIs, no I/O, no clocks, no unseeded
  randomness (`backend-gdk-api.md` §9) — shuffle the deck from `InitContext.seed()`. State is a value
  (`§5.1`): records, `List`, `String`, `UUID`.
- Hidden information (the face-down cards of the pyramid) must never reach a client that may not see
  it: build per-viewer views in `onResync` and per-player `sendToPlayer` effects.
- `SevenWondersDuelState` is what the round trip test serializes; keep it that way.
- Bundle layout is `bundle-format.md` §2; the archive's file-name stem is the fixed game id.
