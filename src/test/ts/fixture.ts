import { readFileSync } from "node:fs";
import type { CatalogEntry, ServerMessage } from "../../main/ts/core/wire.js";

/** The catalog the Java side serialized into the resync fixture: the real 99 entries. */
export function catalogEntries(): CatalogEntry[] {
  const resync = JSON.parse(
    readFileSync("src/test/resources/wire/resync.json", "utf8"),
  ) as ServerMessage;
  if (resync.catalog === null) {
    throw new Error("the resync fixture has no catalog");
  }
  return resync.catalog.entries;
}

export function catalogEntry(id: string): CatalogEntry {
  const found = catalogEntries().find((e) => e.id === id);
  if (!found) {
    throw new Error(`no ${id} in the catalog`);
  }
  return found;
}
