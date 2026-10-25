import { describe, expect, it } from "vitest";
import { cuesOf } from "../../main/ts/core/cues.js";
import type { EventView, PlaceView } from "../../main/ts/core/wire.js";

const ONE = "00000000-0000-0000-0000-000000000001";

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

const at = (zone: string, key: string): PlaceView => ({ zone, key });

describe("cuesOf", () => {
  it("makes a card that changes place travel from where it was to where it is", () => {
    const moved = event({
      kind: "moved",
      id: 3,
      component: "palisade",
      from: at("structure.up", "3:1"),
      to: at("city", ONE),
    });

    expect(cuesOf([moved])).toEqual([
      { kind: "travel", id: 3, parent: -1, component: "palisade", from: moved.from, to: moved.to },
    ]);
  });

  it("turns a card that was uncovered instead of moving it", () => {
    const uncovered = event({
      kind: "moved",
      id: 4,
      component: "baths",
      from: at("structure.down", "2:0"),
      to: at("structure.up", "2:0"),
    });

    expect(cuesOf([uncovered])).toEqual([{ kind: "flip", id: 4, parent: -1, component: "baths" }]);
  });

  it("cues nothing for a move whose component is not known to this viewer", () => {
    expect(cuesOf([event({ kind: "moved", component: null })])).toEqual([]);
  });

  it("floats coins gained or lost by the seat they belong to", () => {
    expect(cuesOf([event({ kind: "coins", id: 1, player: ONE, amount: -2 })])).toEqual([
      { kind: "coins", id: 1, parent: -1, player: ONE, amount: -2 },
    ]);
  });

  it("leaves a trade payment to the coins it already took", () => {
    expect(cuesOf([event({ kind: "trade", id: 2, player: ONE, amount: 3 })])).toEqual([]);
  });

  it("lights the chain badge of the card built free", () => {
    expect(cuesOf([event({ kind: "chain", id: 5, component: "baths" })])).toEqual([
      { kind: "chain", id: 5, parent: -1, component: "baths" },
    ]);
  });

  it("pulses the turn marker when a replay is granted and settles it when cleared", () => {
    expect(
      cuesOf([
        event({ kind: "playAgainGranted", id: 6, player: ONE }),
        event({ kind: "playAgainCleared", id: 7, player: ONE }),
      ]),
    ).toEqual([
      { kind: "turn", id: 6, parent: -1, player: ONE, granted: true },
      { kind: "turn", id: 7, parent: -1, player: ONE, granted: false },
    ]);
  });

  it("leaves the pawn to the military track", () => {
    expect(cuesOf([event({ kind: "pawn" })])).toEqual([]);
  });

  it("keeps event order and carries the parent so children play with their cause", () => {
    const cues = cuesOf([
      event({
        kind: "moved",
        id: 1,
        component: "baths",
        from: at("deck", ""),
        to: at("city", ONE),
      }),
      event({ kind: "coins", id: 2, parent: 1, player: ONE, amount: 2 }),
    ]);

    expect(cues.map((c) => [c.id, c.parent])).toEqual([
      [1, -1],
      [2, 1],
    ]);
  });
});
