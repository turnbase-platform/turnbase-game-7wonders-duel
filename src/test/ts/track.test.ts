import { readFileSync } from "node:fs";
import { describe, expect, it } from "vitest";
import { makeCatalog } from "../../main/ts/core/catalog.js";
import type { Seats } from "../../main/ts/core/seats.js";
import { trackOf } from "../../main/ts/core/track.js";
import type { DuelView, ServerMessage } from "../../main/ts/core/wire.js";
import { catalogEntries } from "./fixture.js";

const catalog = makeCatalog({ entries: catalogEntries() });
const message = JSON.parse(
  readFileSync("src/test/resources/wire/view-in-age.json", "utf8"),
) as ServerMessage;
if (message.view === null) throw new Error("the fixture has no view");
const view: DuelView = message.view;
const [ONE, TWO] = view.players.map((p) => p.id) as [string, string];
const seats: Seats = { me: ONE, opp: TWO, spectator: false };

/** The view with the pawn moved: `space` toward `side`'s capital. */
const pawnAt = (side: string | null, space: number, base: DuelView = view): DuelView => ({
  ...base,
  pawnSide: side,
  pawnSpace: space,
});

describe("trackOf tiles", () => {
  it("runs from the viewer's capital to the opponent's, nineteen tiles", () => {
    const { tiles } = trackOf(view, catalog, seats);
    expect(tiles.map((t) => t.position)).toEqual(Array.from({ length: 19 }, (_, i) => i - 9));
    expect(tiles[0]).toMatchObject({ capital: "me" });
    expect(tiles[18]).toMatchObject({ capital: "opp" });
    expect(tiles.filter((t) => t.capital === null).map((t) => t.label)).toEqual([
      "8",
      "7",
      "6",
      "5",
      "4",
      "3",
      "2",
      "1",
      "0",
      "1",
      "2",
      "3",
      "4",
      "5",
      "6",
      "7",
      "8",
    ]);
  });

  it("puts the pawn on the centre at the start", () => {
    const { tiles } = trackOf(view, catalog, seats);
    expect(tiles.filter((t) => t.pawn).map((t) => t.position)).toEqual([0]);
  });

  it("reads a pawn on the opponent's side as positive and on the viewer's as negative", () => {
    const pawn = (v: DuelView) => trackOf(v, catalog, seats).tiles.find((t) => t.pawn)?.position;
    expect(pawn(pawnAt(TWO, 2))).toBe(2);
    expect(pawn(pawnAt(ONE, 4))).toBe(-4);
  });

  it("gives every other tile its distance as a hover title", () => {
    const { tiles } = trackOf(pawnAt(TWO, 2), catalog, seats);
    expect(tiles.find((t) => t.position === 5)?.title).toBe("+3 shields");
    expect(tiles.find((t) => t.position === -1)?.title).toBe("−3 shields");
    expect(tiles.find((t) => t.pawn)?.title).toBeNull();
  });

  it("says on a capital's hover what reaching it means", () => {
    const { tiles } = trackOf(pawnAt(TWO, 2), catalog, seats);
    expect(tiles[18]?.title).toBe("+7 shields · Capital: Military Supremacy for the other player");
    expect(tiles[0]?.title).toBe("−11 shields · Capital: Military Supremacy for the other player");
  });
});

describe("trackOf fields", () => {
  it("lays out the loot and VP fields under the tiles they cover", () => {
    const { fields } = trackOf(view, catalog, seats);
    expect(fields.map((f) => [f.kind, f.from, f.to, f.text])).toEqual([
      ["loot", 1, 3, "5"],
      ["loot", 4, 6, "2"],
      ["vp", 7, 8, "2 VP"],
      ["vp", 10, 11, "2 VP"],
      ["loot", 12, 14, "2"],
      ["loot", 15, 17, "5"],
    ]);
  });

  it("marks a loot token that has been taken, and still says what it was worth", () => {
    const withoutOne = {
      ...view,
      visible: view.visible.filter((p) => p.component !== "loot-2-a"),
    };
    const loot = trackOf(withoutOne, catalog, seats).fields.filter((f) => f.kind === "loot");
    expect(loot.filter((f) => f.taken).map((f) => [f.from, f.text])).toEqual([[4, "2"]]);
  });

  it("follows the viewer: a spectator sees the first seat on the left", () => {
    const spectator: Seats = { me: ONE, opp: TWO, spectator: true };
    expect(trackOf(view, catalog, spectator).tiles[0]?.capital).toBe("me");
    const swapped: Seats = { me: TWO, opp: ONE, spectator: false };
    expect(trackOf(pawnAt(TWO, 2), catalog, swapped).tiles.find((t) => t.pawn)?.position).toBe(-2);
  });

  it("highlights the zone the pawn is in, and none at the centre", () => {
    const current = (v: DuelView) =>
      trackOf(v, catalog, seats)
        .fields.filter((f) => f.current)
        .map((f) => f.from);
    expect(current(view)).toEqual([]);
    expect(current(pawnAt(TWO, 2))).toEqual([10]);
    expect(current(pawnAt(ONE, 4))).toEqual([4]);
    expect(current(pawnAt(TWO, 9))).toEqual([]);
  });

  it("says what a zone is worth in victory points in the loot field's title", () => {
    const loot = trackOf(view, catalog, seats).fields.filter((f) => f.kind === "loot");
    expect(loot.map((f) => f.title)).toEqual([
      "Zone worth 10 VP; loot 5 coins",
      "Zone worth 5 VP; loot 2 coins",
      "Zone worth 5 VP; loot 2 coins",
      "Zone worth 10 VP; loot 5 coins",
    ]);
  });
});
