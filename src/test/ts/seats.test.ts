import { readFileSync } from "node:fs";
import { describe, expect, it } from "vitest";
import { makeCatalog } from "../../main/ts/core/catalog.js";
import { seatsOf } from "../../main/ts/core/seats.js";
import { emptySession, type Session } from "../../main/ts/core/session.js";
import type { ServerMessage } from "../../main/ts/core/wire.js";

const ONE = "00000000-0000-0000-0000-000000000001";
const TWO = "00000000-0000-0000-0000-000000000002";

function sessionAs(self: string | null): Session {
  const message = JSON.parse(
    readFileSync("src/test/resources/wire/view-in-age.json", "utf8"),
  ) as ServerMessage;
  return {
    ...emptySession(),
    role: self === null ? "spectator" : "player",
    self,
    view: message.view,
    catalog: makeCatalog({ entries: [] }),
  };
}

describe("seatsOf", () => {
  it("is null before the first view", () => {
    expect(seatsOf(emptySession())).toBeNull();
  });

  it("puts the viewing player as me, whichever seat they hold", () => {
    expect(seatsOf(sessionAs(ONE))).toEqual({ me: ONE, opp: TWO, spectator: false });
    expect(seatsOf(sessionAs(TWO))).toEqual({ me: TWO, opp: ONE, spectator: false });
  });

  it("shows a spectator seat 1 as me", () => {
    expect(seatsOf(sessionAs(null))).toEqual({ me: ONE, opp: TWO, spectator: true });
  });
});
