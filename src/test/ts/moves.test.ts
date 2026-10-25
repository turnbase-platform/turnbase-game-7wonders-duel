import { readFileSync } from "node:fs";
import type { InitContext, Player } from "@turnbase/gdk";
import { describe, expect, it } from "vitest";
import { offers } from "../../main/ts/core/moves.js";
import { emptySession, reduce, type Session } from "../../main/ts/core/session.js";
import type { DuelView, ServerMessage } from "../../main/ts/core/wire.js";

function message(name: string): ServerMessage {
  return JSON.parse(readFileSync(`src/test/resources/wire/${name}.json`, "utf8")) as ServerMessage;
}

const ONE: Player = { id: "00000000-0000-0000-0000-000000000001", displayName: "One", teamId: "a" };
const TWO: Player = { id: "00000000-0000-0000-0000-000000000002", displayName: "Two", teamId: "b" };

function init(self: Player | null): InitContext {
  return {
    role: self ? "player" : "spectator",
    self,
    roster: [ONE, TWO],
    teams: [],
    sessionId: "s",
  };
}

/** The age fixture once the starter has been chosen: nobody is deciding, cards may be played. */
function playing(self: Player, change: Partial<DuelView> = {}): Session {
  const base = message("view-in-age");
  if (!base.view) throw new Error("fixture has no view");
  const started = reduce(emptySession(), { type: "init", context: init(self) });
  const view = { ...base.view, deciding: null, ...change };
  return reduce(started, { type: "message", message: { ...base, view } });
}

/** A session that has been initialised and has received one fixture as its picture. */
function seen(fixture: string, self: Player | null): Session {
  const started = reduce(emptySession(), { type: "init", context: init(self) });
  return reduce(started, { type: "message", message: message(fixture) });
}

describe("offers", () => {
  it("offers the draft wonders to the seat whose turn it is", () => {
    const found = offers(seen("view-after-draft-pick", ONE));

    expect(found).toHaveLength(3);
    expect(found.every((o) => o.action.type === "draftWonder")).toBe(true);
  });

  it("offers nothing to the seat that is waiting", () => {
    expect(offers(seen("view-after-draft-pick", TWO))).toEqual([]);
  });

  it("offers nothing to a spectator", () => {
    expect(offers(seen("view-after-draft-pick", null))).toEqual([]);
  });

  it("offers to build the cards a quote says the seat can afford, and to discard any exposed card", () => {
    const session = playing(TWO);
    const builds = offers(session).filter((o) => o.action.type === "build");
    const discards = offers(session).filter((o) => o.action.type === "discard");

    expect(builds.length).toBeGreaterThan(0);
    expect(discards.length).toBeGreaterThanOrEqual(builds.length);
    expect(builds.map((o) => o.target)).toContain("palisade");
  });

  it("offers nothing on a card the pyramid still covers", () => {
    const session = playing(TWO);
    const up = session.view?.visible.filter((p) => p.zone === "structure.up") ?? [];
    const quoted = new Set(session.view?.quotes.filter((q) => q.takeable).map((q) => q.component));
    const covered = up.filter((p) => !quoted.has(p.component));

    expect(covered.length).toBeGreaterThan(0);
    const targets = new Set(offers(session).map((o) => o.target));
    expect(covered.some((p) => targets.has(p.component))).toBe(false);
  });

  it("does not offer to build what the seat cannot pay for", () => {
    const base = message("view-in-age").view;
    if (!base) throw new Error("fixture has no view");
    const session = playing(TWO, {
      quotes: base.quotes.map((q) => ({ ...q, affordable: false })),
    });

    expect(offers(session).some((o) => o.action.type === "build")).toBe(false);
    expect(offers(session).some((o) => o.action.type === "discard")).toBe(true);
  });

  it("offers to build a Wonder with an exposed card for each unbuilt Wonder the seat can pay for", () => {
    const base = message("view-in-age").view;
    if (!base) throw new Error("fixture has no view");
    const session = playing(TWO, {
      quotes: base.quotes.map((q) =>
        q.component === "piraeus" || q.component === "the-colossus"
          ? { ...q, affordable: true }
          : q,
      ),
    });
    const wonders = offers(session).filter((o) => o.action.type === "buildWonder");
    const forPalisade = wonders.filter((o) => o.target === "palisade");

    expect(forPalisade.map((o) => o.action)).toEqual([
      { type: "buildWonder", component: "palisade", wonder: "piraeus" },
      { type: "buildWonder", component: "palisade", wonder: "the-colossus" },
    ]);
    expect(wonders).toHaveLength(
      2 * offers(session).filter((o) => o.action.type === "discard").length,
    );
  });

  it("offers no Wonder while none is affordable, and none on a covered card", () => {
    const session = playing(TWO);
    expect(offers(session).some((o) => o.action.type === "buildWonder")).toBe(false);
  });

  it("offers the starting player choice to the seat that decides, and nothing else", () => {
    const found = offers(seen("view-in-age", TWO));
    const answers = found.filter((o) => o.action.type === "chooseStarter");
    // view-in-age has a deciding seat: the choice comes before any card
    expect(answers.map((o) => o.target).sort()).toEqual([ONE.id, TWO.id]);
    expect(found.every((o) => o.action.type === "chooseStarter")).toBe(true);
  });

  it("offers the options of a pending decision to the decider only", () => {
    const mine = offers(seen("view-with-decision", TWO));
    expect(mine).toHaveLength(5);
    expect(mine.every((o) => o.action.type === "answerComponent")).toBe(true);
    expect(offers(seen("view-with-decision", ONE))).toEqual([]);
  });

  it("offers nothing once the game is over", () => {
    expect(offers(seen("view-game-over", ONE))).toEqual([]);
  });
});
