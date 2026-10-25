import { describe, expect, it } from "vitest";
import type { Cue } from "../../main/ts/core/cues.js";
import { tweensOf, type Rect } from "../../main/ts/core/flip.js";

const rect = (left: number, top: number, width = 100, height = 150): Rect => ({
  left,
  top,
  width,
  height,
});
const travel = (component: string): Cue => ({
  kind: "travel",
  id: 1,
  parent: -1,
  component,
  from: null,
  to: null,
});

describe("tweensOf", () => {
  it("starts a travelling card at the offset and scale of where it was", () => {
    const tweens = tweensOf(
      [travel("palisade")],
      new Map([["palisade", rect(300, 200, 60, 90)]]),
      new Map([["palisade", rect(100, 400, 120, 180)]]),
    );

    expect(tweens).toEqual([
      { kind: "travel", component: "palisade", dx: 200, dy: -200, sx: 0.5, sy: 0.5 },
    ]);
  });

  it("scales both axes alike, so a card flying into a banner is not stretched", () => {
    const [tween] = tweensOf(
      [travel("palisade")],
      new Map([["palisade", rect(0, 0, 100, 150)]]),
      new Map([["palisade", rect(0, 0, 50, 20)]]),
    );

    expect([tween.sx, tween.sy]).toEqual([2, 2]);
  });

  it("makes a card that was not on screen before appear in place", () => {
    const tweens = tweensOf([travel("baths")], new Map(), new Map([["baths", rect(10, 10)]]));

    expect(tweens).toEqual([{ kind: "appear", component: "baths", dx: 0, dy: 0, sx: 1, sy: 1 }]);
  });

  it("plays nothing for a card that is no longer on screen", () => {
    expect(tweensOf([travel("baths")], new Map([["baths", rect(0, 0)]]), new Map())).toEqual([]);
  });

  it("plays nothing for a card that did not move", () => {
    const same = new Map([["baths", rect(5, 5)]]);

    expect(tweensOf([travel("baths")], same, same)).toEqual([]);
  });

  it("turns an uncovered card in place", () => {
    const flip: Cue = { kind: "flip", id: 2, parent: -1, component: "baths" };
    const where = new Map([["baths", rect(5, 5)]]);

    expect(tweensOf([flip], where, where)).toEqual([
      { kind: "flip", component: "baths", dx: 0, dy: 0, sx: 1, sy: 1 },
    ]);
  });

  it("lights a chain badge and ignores cues that are not about a card's rectangle", () => {
    const cues: Cue[] = [
      { kind: "chain", id: 3, parent: -1, component: "baths" },
      { kind: "coins", id: 4, parent: -1, player: null, amount: 2 },
    ];
    const where = new Map([["baths", rect(5, 5)]]);

    expect(tweensOf(cues, where, where)).toEqual([
      { kind: "chain", component: "baths", dx: 0, dy: 0, sx: 1, sy: 1 },
    ]);
  });
});
