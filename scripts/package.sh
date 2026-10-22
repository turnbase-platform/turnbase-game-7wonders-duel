#!/usr/bin/env bash
#
# Assembles the game bundle (bundle-format.md §2.1) at target/seed/<GAME_ID>.zip:
#
#   manifest.json, INFO.md, backend/sevenwondersduel.jar, frontend/main.js
#
# The archive is named after the game's fixed id, because that is how the platform's seeder
# learns it (local-environment.md §6, IMP-GATE-71). Run `./mvnw package && npm run build` first.
set -euo pipefail
cd "$(dirname "$0")/.."

GAME_ID="95364554-17f0-4ce6-b3ca-0976638b484a"
DIR=target/bundle

[ -f target/sevenwondersduel.jar ] || { echo "target/sevenwondersduel.jar missing — run ./mvnw package" >&2; exit 1; }
[ -f "$DIR/frontend/main.js" ] || { echo "$DIR/frontend/main.js missing — run npm run build" >&2; exit 1; }

mkdir -p "$DIR/backend"
cp manifest.json INFO.md "$DIR/"
cp target/sevenwondersduel.jar "$DIR/backend/sevenwondersduel.jar"

mkdir -p target/seed
rm -f "target/seed/$GAME_ID.zip"
# -X: no extra file attributes; manifest.json first, though only its location is fixed.
(cd "$DIR" && zip -qrX "../seed/$GAME_ID.zip" manifest.json INFO.md backend frontend)
echo "target/seed/$GAME_ID.zip"
