#!/usr/bin/env bash
#
# The frontend dev loop: the real game in-process behind /api (DevServer, test scope) and Vite
# with HMR in front of it. Open http://localhost:5173/dev/ for seat 1, seat 2 and a spectator.
#
#   scripts/dev.sh [seed]        # default seed 7
set -euo pipefail
cd "$(dirname "$0")/.."

# The enforcer pins the build to JDK 21 (conventions.md), and DevServer runs the game on it too.
"${JAVA_HOME:+$JAVA_HOME/bin/}java" -version 2>&1 | grep -q 'version "21' \
  || { echo "JAVA_HOME must point at a JDK 21" >&2; exit 1; }

SEED="${1:-7}"
PORT="${DEV_API_PORT:-8787}"
CP_FILE=target/dev.classpath

./mvnw -o -q test-compile
[ -f "$CP_FILE" ] || ./mvnw -o -q dependency:build-classpath -Dmdep.outputFile="$CP_FILE" -Dmdep.includeScope=test

java -cp "target/test-classes:target/classes:$(cat "$CP_FILE")" \
  io.turnbase.games.sevenwondersduel.playthrough.DevServer "$PORT" "$SEED" &
SERVER=$!
trap 'kill $SERVER 2>/dev/null' EXIT

DEV_API_PORT="$PORT" npx vite --open /dev/
