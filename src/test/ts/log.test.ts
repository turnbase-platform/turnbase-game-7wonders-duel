import { readFileSync } from "node:fs";
import { describe, expect, it } from "vitest";
import { makeCatalog } from "../../main/ts/core/catalog.js";
import { logLines, type LogLine } from "../../main/ts/core/log.js";
import type { CatalogEntry, EventView, ServerMessage } from "../../main/ts/core/wire.js";

const ONE = "00000000-0000-0000-0000-000000000001";
const TWO = "00000000-0000-0000-0000-000000000002";

function entry(id: string, name: string): CatalogEntry {
  return {
    id,
    name,
    type: "CARD",
    tags: [],
    back: null,
    cost: { coins: 0, resources: [], freedBy: [] },
    badges: [],
  };
}

const catalog = makeCatalog({
  entries: [
    entry("lumber-yard", "Lumber Yard"),
    entry("baths", "Baths"),
    entry("piraeus", "Piraeus"),
  ],
});
const names: Record<string, string> = { [ONE]: "Ann", [TWO]: "Bo" };
const who = (id: string): string => names[id] ?? "?";

function event(over: Partial<EventView> & Pick<EventView, "kind">): EventView {
  return {
    id: 0,
    parent: -1,
    cause: null,
    component: null,
    from: null,
    to: null,
    player: null,
    amount: 0,
    pawnFrom: null,
    pawnTo: null,
    ...over,
  };
}

function texts(events: EventView[]): string[] {
  const out: string[] = [];
  const walk = (lines: LogLine[], depth: number): void => {
    for (const l of lines) {
      out.push("  ".repeat(depth) + l.text);
      walk(l.children, depth + 1);
    }
  };
  walk(logLines(events, catalog, who), 0);
  return out;
}

describe("the event log", () => {
  it("says a card was uncovered rather than moved from the pyramid to the pyramid", () => {
    const uncovered = event({
      kind: "moved",
      component: "baths",
      from: { zone: "structure.down", key: "2:0" },
      to: { zone: "structure.up", key: "2:0" },
    });

    expect(texts([uncovered])).toEqual(["Baths was uncovered"]);
  });

  it("says where a named component moved", () => {
    const moved = event({
      kind: "moved",
      component: "lumber-yard",
      from: { zone: "structure.up", key: "" },
      to: { zone: "city", key: ONE },
    });

    expect(texts([moved])).toEqual(["Lumber Yard moved from the pyramid to Ann's city"]);
  });

  it("calls a component the viewer cannot identify a card", () => {
    const moved = event({
      kind: "moved",
      from: { zone: "deck", key: "1" },
      to: { zone: "box", key: "" },
    });

    expect(texts([moved])).toEqual(["A card moved from the deck to the box"]);
  });

  it("reports coins gained and lost, naming the cause", () => {
    expect(texts([event({ kind: "coins", player: ONE, amount: 3, cause: "baths" })])).toEqual([
      "Ann gains 3 coins (Baths)",
    ]);
    expect(texts([event({ kind: "coins", player: TWO, amount: -1 })])).toEqual(["Bo loses 1 coin"]);
  });

  it("reports a trade payment, a chain and the extra turn", () => {
    expect(texts([event({ kind: "trade", player: ONE, amount: 4 })])).toEqual([
      "Ann pays 4 coins to the bank for resources",
    ]);
    expect(texts([event({ kind: "chain", player: ONE, component: "baths" })])).toEqual([
      "Ann builds Baths free by chain",
    ]);
    expect(texts([event({ kind: "playAgainGranted", player: ONE, cause: "piraeus" })])).toEqual([
      "Ann plays again (Piraeus)",
    ]);
    expect(texts([event({ kind: "playAgainCleared", player: ONE })])).toEqual([
      "Ann has no extra turn left",
    ]);
  });

  it("reports the military pawn", () => {
    const toBo = event({
      kind: "pawn",
      pawnFrom: { side: null, space: 0 },
      pawnTo: { side: TWO, space: 2 },
    });
    const home = event({
      kind: "pawn",
      pawnFrom: { side: TWO, space: 1 },
      pawnTo: { side: null, space: 0 },
    });

    expect(texts([toBo])).toEqual(["The military pawn moves to Bo's side, 2 from the centre"]);
    expect(texts([home])).toEqual(["The military pawn returns to the centre"]);
  });

  it("folds an effect under the event that caused it", () => {
    const events = [
      event({
        id: 0,
        kind: "moved",
        component: "baths",
        from: { zone: "structure.up", key: "" },
        to: { zone: "city", key: ONE },
      }),
      event({ id: 1, parent: 0, kind: "coins", player: ONE, amount: 2, cause: "baths" }),
      event({ id: 2, parent: 1, kind: "playAgainGranted", player: ONE }),
      event({ id: 3, kind: "coins", player: TWO, amount: 1 }),
    ];

    expect(texts(events)).toEqual([
      "Baths moved from the pyramid to Ann's city",
      "  Ann gains 2 coins (Baths)",
      "    Ann plays again",
      "Bo gains 1 coin",
    ]);
  });

  it("describes every event of a real game", () => {
    const m = JSON.parse(
      readFileSync("src/test/resources/wire/view-in-age.json", "utf8"),
    ) as ServerMessage;
    const resync = JSON.parse(
      readFileSync("src/test/resources/wire/resync.json", "utf8"),
    ) as ServerMessage;
    const real = makeCatalog(resync.catalog ?? { entries: [] });

    const lines = logLines(m.events, real, who);

    expect(lines.length).toBeGreaterThan(0);
    const count = (l: LogLine): number => 1 + l.children.reduce((n, c) => n + count(c), 0);
    expect(lines.reduce((n, l) => n + count(l), 0)).toBe(m.events.length);
  });
});
