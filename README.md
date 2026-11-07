# turnbase-game-7wonders-duel

7 Wonders Duel for TurnBase. A **consumer** of the published GDK, like `turnbase-game-tictactoe`: the
backend (Java 21, runs in Espresso) compiles against `io.github.turnbase-platform:turnbase-sdk` (scope `provided`)
and the frontend (React 19) bundles `@turnbase-platform/gdk`. A dependency on `turnbase-game-host` or
`turnbase-common` is banned by the enforcer.

**Credits.** 7 Wonders Duel (2015) was designed by Antoine Bauza and Bruno Cathala, with art by
Miguel Coimbra, and is published by Repos Production. This repository is an independent
implementation; the credits are carried in `manifest.json` (`credits`) and shown on the game's page.

**Status: the base game is complete** — setup and Wonder draft, three Ages, cost solver and trading,
military, science, tokens, guilds, scoring and every victory — with the full React UI (pyramid,
cities, military track, token tray, log, choreography). The expansions (Agora, Pantheon, Extra
Wonders) are not implemented; the engine is built so they are additions. Rules: `docs/rules/`; build
plan: `docs/implementation/`.

```bash
./mvnw verify                  # backend: Spotless, unit tests, JaCoCo ≥ 70 %
npm ci && npm run lint && npm run typecheck && npm test && npm run build
./scripts/package.sh           # → target/seed/95364554-17f0-4ce6-b3ca-0976638b484a.zip
```

`95364554-17f0-4ce6-b3ca-0976638b484a` is the game's fixed id: the platform's seeder takes the file-name stem as `game_id`
(`local-environment.md` §6). Copy the zip into the directory `turnbase.seed.bundles-dir` names to
seed it into a local stack.

Frontend dev loop (the real game in-process, Vite with HMR; needs JDK 21): `npm run dev`, then
<http://localhost:5173/dev/> for seat 1, seat 2 and a spectator.

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

## Third-party licences

What this repository distributes is audited for its dependencies' licences (allowed families:
`docs/implementation/conventions.md` §6 *Third-party licences*). The report is generated, not committed:

```bash
./mvnw org.codehaus.mojo:license-maven-plugin:2.8.0:add-third-party -Dlicense.excludedScopes=test,provided
# -> target/generated-sources/license/THIRD-PARTY.txt
```

The SDK is `provided`, so the backend jar carries no third-party code. The frontend: `npm run licenses` (pinned checker, `licenses.csv`, git-ignored); `main.js` bundles React (MIT). The bundle embeds no fonts.
