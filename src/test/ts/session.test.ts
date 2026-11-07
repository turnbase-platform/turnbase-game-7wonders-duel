import { readFileSync } from "node:fs";
import type { InitContext } from "@turnbase-platform/gdk";
import { describe, expect, it } from "vitest";
import {
  emptySession,
  reduce,
  seatName,
  HISTORY_LIMIT,
  type Session,
} from "../../main/ts/core/session.js";
import type { ServerMessage } from "../../main/ts/core/wire.js";

function message(name: string): ServerMessage {
  return JSON.parse(readFileSync(`src/test/resources/wire/${name}.json`, "utf8")) as ServerMessage;
}

const ONE = { id: "00000000-0000-0000-0000-000000000001", displayName: "One", teamId: "t1" };
const TWO = { id: "00000000-0000-0000-0000-000000000002", displayName: "Two", teamId: "t2" };
const INIT: InitContext = {
  role: "player",
  self: ONE,
  roster: [ONE, TWO],
  teams: [],
  sessionId: "s",
};

function joined(): Session {
  return reduce(emptySession(), { type: "init", context: INIT });
}

function resynced(): Session {
  return reduce(joined(), { type: "message", message: message("resync") });
}

describe("the session", () => {
  it("starts empty", () => {
    const s = emptySession();
    expect(s.view).toBeNull();
    expect(s.catalog).toBeNull();
    expect(s.history).toEqual([]);
    expect(s.rejection).toBeNull();
    expect(s.over).toBe(false);
  });

  it("init records who this client is and who is playing", () => {
    const s = joined();
    expect(s.role).toBe("player");
    expect(s.self).toBe(ONE.id);
    expect(s.roster).toEqual([ONE, TWO]);
    expect(s.view).toBeNull();
  });

  it("a spectator has no self", () => {
    const s = reduce(emptySession(), {
      type: "init",
      context: { ...INIT, role: "spectator", self: null },
    });
    expect(s.role).toBe("spectator");
    expect(s.self).toBeNull();
  });

  it("a resync replaces the view and the catalog and forgets the history", () => {
    const played = reduce(resynced(), {
      type: "message",
      message: message("view-after-draft-pick"),
    });
    expect(played.history).toHaveLength(1);

    const again = reduce(played, { type: "message", message: message("resync") });

    expect(again.view?.phase).toBe("WONDER_DRAFT");
    expect(again.catalog?.get("lumber-yard")?.name).toBe("Lumber Yard");
    expect(again.history).toEqual([]);
  });

  it("a view message keeps the catalog and files its events as one step", () => {
    const m = message("view-after-draft-pick");

    const s = reduce(resynced(), { type: "message", message: m });

    expect(s.catalog?.get("lumber-yard")).toBeDefined();
    expect(s.view).toEqual(m.view);
    expect(s.history).toEqual([{ number: 1, events: m.events }]);
  });

  it("a view message without events adds no step", () => {
    const m = { ...message("view-after-draft-pick"), events: [] };

    expect(reduce(resynced(), { type: "message", message: m }).history).toEqual([]);
  });

  it("a seat skips the broadcast that its own message follows", () => {
    const broadcast = { ...message("view-after-draft-pick"), broadcast: true };
    const before = resynced();

    expect(reduce(before, { type: "message", message: broadcast })).toBe(before);
  });

  it("a spectator applies the broadcast, which is all it gets", () => {
    const spectator = reduce(emptySession(), {
      type: "init",
      context: { ...INIT, role: "spectator", self: null },
    });
    const watching = reduce(spectator, { type: "message", message: message("resync") });
    const broadcast = { ...message("view-after-draft-pick"), broadcast: true };

    const s = reduce(watching, { type: "message", message: broadcast });

    expect(s.view).toEqual(broadcast.view);
    expect(s.history).toHaveLength(1);
  });

  it("a rejection keeps the view and is cleared by the next view", () => {
    const before = resynced();
    const rejected = reduce(before, { type: "message", message: message("rejected") });

    expect(rejected.view).toBe(before.view);
    expect(rejected.rejection).toBe("not your turn");

    const next = reduce(rejected, { type: "message", message: message("view-after-draft-pick") });
    expect(next.rejection).toBeNull();
  });

  it("keeps only the most recent steps", () => {
    let s = resynced();
    for (let i = 0; i < HISTORY_LIMIT + 5; i++) {
      s = reduce(s, { type: "message", message: message("view-after-draft-pick") });
    }

    expect(s.history).toHaveLength(HISTORY_LIMIT);
    expect(s.history[s.history.length - 1]?.number).toBe(HISTORY_LIMIT + 5);
    expect(s.history[0]?.number).toBe(6);
  });

  it("remembers who left and that the game is over", () => {
    const left = reduce(joined(), { type: "playerLeft", player: TWO });
    expect(left.left).toEqual([TWO]);

    const over = reduce(left, { type: "gameOver", results: { winner: ONE.id } });
    expect(over.over).toBe(true);
  });

  it("names a seat from the roster and falls back to a short id", () => {
    const s = joined();
    expect(seatName(s, TWO.id)).toBe("Two");
    expect(seatName(s, "12345678-aaaa")).toBe("12345678");
  });
});
