import { readFileSync } from "node:fs";
import { renderToStaticMarkup } from "react-dom/server";
import { describe, expect, it } from "vitest";
import { makeCatalog } from "../../main/ts/core/catalog.js";
import type { Seats } from "../../main/ts/core/seats.js";
import type { DuelView, ServerMessage } from "../../main/ts/core/wire.js";
import { TrackStrip } from "../../main/ts/ui/TrackStrip.js";
import { catalogEntries } from "./fixture.js";

const catalog = makeCatalog({ entries: catalogEntries() });
const message = JSON.parse(
  readFileSync("src/test/resources/wire/view-in-age.json", "utf8"),
) as ServerMessage;
if (message.view === null) throw new Error("the fixture has no view");
const view: DuelView = message.view;
const [ONE, TWO] = view.players.map((p) => p.id) as [string, string];
const seats: Seats = { me: ONE, opp: TWO, spectator: false };

const html = (v: DuelView): string =>
  renderToStaticMarkup(<TrackStrip view={v} catalog={catalog} seats={seats} />);

describe("TrackStrip pawn", () => {
  it("is one element whose place is a custom property, so a move can slide it", () => {
    const out = html({ ...view, pawnSide: TWO, pawnSpace: 2 });
    expect(out.split('class="pawn"')).toHaveLength(2);
    expect(out).toContain("--at:11");
  });

  it("stands on the centre tile at the start", () => {
    expect(html({ ...view, pawnSide: null, pawnSpace: 0 })).toContain("--at:9");
  });
});
