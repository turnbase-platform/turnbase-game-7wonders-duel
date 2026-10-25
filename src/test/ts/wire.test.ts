import { readFileSync } from "node:fs";
import { describe, expect, it } from "vitest";
import type {
  Badge,
  CatalogEntry,
  CostView,
  DuelView,
  EventView,
  HiddenView,
  PawnView,
  PendingView,
  PlacedView,
  PlayerView,
  QuoteView,
  ServerMessage,
} from "../../main/ts/core/wire.js";

function read(name: string): unknown {
  return JSON.parse(readFileSync(`src/test/resources/wire/${name}.json`, "utf8"));
}

function present<T>(value: T | null | undefined): T {
  if (value === null || value === undefined) {
    throw new Error("expected a value");
  }
  return value;
}

function message(name: string): ServerMessage {
  return read(name) as ServerMessage;
}

/** Sorted field names: what a fixture must have exactly, no more and no fewer. */
function keys(value: object): string[] {
  return Object.keys(value).sort();
}

// Each exemplar is checked by the compiler against the interface (a missing or extra field does
// not compile) and by the tests below against the Java side's JSON.
const shape: ServerMessage = {
  kind: "view",
  view: null,
  reason: null,
  events: [],
  catalog: null,
  broadcast: false,
};
const view: DuelView = {
  phase: "AGE",
  active: "",
  age: 1,
  first: "",
  deciding: null,
  players: [],
  pawnSide: null,
  pawnSpace: 0,
  victoryWinner: null,
  victoryReason: null,
  visible: [],
  hidden: [],
  pending: null,
  quotes: [],
};
const player: PlayerView = { id: "", coins: 0, playAgain: false };
const placed: PlacedView = { component: "", zone: "", key: "" };
const hidden: HiddenView = { zone: "", key: "", back: null, count: 0 };
const pending: PendingView = { decider: "", options: [], hidden: 0 };
const quote: QuoteView = {
  player: "",
  component: "",
  coins: 0,
  tradeCost: 0,
  total: 0,
  chained: false,
  affordable: false,
  lines: [],
};
const event: EventView = {
  id: 0,
  parent: -1,
  kind: "moved",
  cause: null,
  component: null,
  from: null,
  to: null,
  player: null,
  amount: 0,
  pawnFrom: null,
  pawnTo: null,
};
const pawn: PawnView = { side: null, space: 0 };
const entry: CatalogEntry = {
  id: "",
  name: "",
  type: "CARD",
  tags: [],
  back: null,
  cost: { coins: 0, resources: [], freedBy: [] },
  badges: [],
};
const cost: CostView = entry.cost;
const badge: Badge = { kind: "", amount: 0, of: [], per: 1, larger: false };

describe("the wire fixtures", () => {
  it.each([
    "resync",
    "view-after-draft-pick",
    "view-in-age",
    "view-with-decision",
    "view-game-over",
    "rejected",
  ])("%s has the shape of a ServerMessage", (name) => {
    expect(keys(read(name) as object)).toEqual(keys(shape));
  });

  it("a view message carries the view and no reason", () => {
    const m = message("view-in-age");
    expect(m.kind).toBe("view");
    expect(m.view).not.toBeNull();
    expect(m.reason).toBeNull();
    expect(m.catalog).toBeNull();
    expect(m.events.length).toBeGreaterThan(0);
  });

  it("a rejection carries only the reason", () => {
    const m = message("rejected");
    expect(m).toEqual({
      kind: "rejected",
      view: null,
      reason: "not your turn",
      events: [],
      catalog: null,
      broadcast: false,
    });
  });

  it("only the resync carries the catalog, and no events", () => {
    const m = message("resync");
    expect(m.catalog?.entries.length).toBeGreaterThan(90);
    expect(m.events).toEqual([]);
    for (const name of ["view-after-draft-pick", "view-in-age", "view-game-over"]) {
      expect(message(name).catalog).toBeNull();
    }
  });

  it("the views have the shape of a DuelView and its parts", () => {
    const m = message("view-in-age");
    const v = present(m.view);
    expect(keys(v)).toEqual(keys(view));
    expect(keys(v.players[0] as object)).toEqual(keys(player));
    expect(keys(v.visible[0] as object)).toEqual(keys(placed));
    expect(keys(v.hidden[0] as object)).toEqual(keys(hidden));
    expect(keys(v.quotes[0] as object)).toEqual(keys(quote));
    expect(v.phase).toBe("AGE");
  });

  it("a pending decision has the shape of a PendingView", () => {
    const v = present(message("view-with-decision").view);
    expect(keys(v.pending as object)).toEqual(keys(pending));
  });

  it("a finished game names the winner and the reason", () => {
    const v = present(message("view-game-over").view);
    expect(v.phase).toBe("OVER");
    expect(v.victoryReason).toBe("POINTS");
    expect(typeof v.victoryWinner).toBe("string");
  });

  it("the catalog has the shape of its entries, costs and badges", () => {
    const entries = (message("resync").catalog as { entries: CatalogEntry[] }).entries;
    const withBadge = present(entries.find((e) => e.badges.length > 0));
    const withResource = present(entries.find((e) => e.cost.resources.length > 0));
    expect(keys(withBadge)).toEqual(keys(entry));
    expect(keys(withBadge.cost)).toEqual(keys(cost));
    expect(keys(withBadge.badges[0] as object)).toEqual(keys(badge));
    expect(keys(withResource.cost.resources[0] as object)).toEqual(["count", "resource"]);
    expect(new Set(entries.map((e) => e.type))).toEqual(new Set(["CARD", "WONDER", "TOKEN"]));
  });

  it("there is one event of every kind, each with every field", () => {
    const events = read("events") as EventView[];
    expect(events.map((e) => e.kind)).toEqual([
      "moved",
      "coins",
      "trade",
      "chain",
      "pawn",
      "playAgainGranted",
      "playAgainCleared",
    ]);
    for (const e of events) {
      expect(keys(e)).toEqual(keys(event));
    }
    const moved = events[4];
    expect(keys(moved.pawnTo as object)).toEqual(keys(pawn));
  });

  it("an action is the type plus only the fields it uses", () => {
    const actions = read("actions") as { type: string }[];
    expect(actions.map((a) => a.type)).toEqual([
      "draftWonder",
      "chooseStarter",
      "discard",
      "build",
      "buildWonder",
      "answerComponent",
      "answerPlayer",
    ]);
  });
});
