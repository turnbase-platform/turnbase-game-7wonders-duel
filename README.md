# turnbase-game-7wonders-duel

7 Wonders Duel for TurnBase. A **consumer** of the published GDK, like `turnbase-game-tictactoe`: the
backend (Java 21, runs in Espresso) compiles against `io.turnbase:turnbase-sdk` (scope `provided`)
and the frontend (React 19) bundles `@turnbase/gdk`. A dependency on `turnbase-game-host` or
`turnbase-common` is banned by the enforcer.

**Status: skeleton.** Two players alternate an `END_TURN` action for six turns, then the game is a
draw; leaving forfeits. The manifest, bundle, hooks and UI wiring are real; the rules are not.

```bash
./mvnw verify                  # backend: Spotless, unit tests, JaCoCo ≥ 70 %
npm ci && npm run lint && npm run typecheck && npm test && npm run build
./scripts/package.sh           # → target/seed/95364554-17f0-4ce6-b3ca-0976638b484a.zip
```

`95364554-17f0-4ce6-b3ca-0976638b484a` is the game's fixed id: the platform's seeder takes the file-name stem as `game_id`
(`local-environment.md` §6). Copy the zip into the directory `turnbase.seed.bundles-dir` names to
seed it into a local stack.

`vendor/turnbase-gdk-0.1.0.tgz` is `npm pack` of `turnbase-sdk`'s FE half — a bootstrap stand-in for
the npm package until the SDK's first release tag exists (`conventions.md` §5).

## Where the rules go

| Concern | File |
|---|---|
| Game state (a value type) | `SevenWondersDuelState.java` |
| Actions a client may send | `Action.java` |
| Events sent to clients | `GameMessage.java` |
| Hooks: init, action, player-left, resync | `SevenWondersDuelGame.java` |
| Frontend model (pure reducers) / screen | `model.ts` / `App.tsx` |
