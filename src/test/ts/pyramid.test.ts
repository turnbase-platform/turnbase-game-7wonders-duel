import { readFileSync } from "node:fs";
import { describe, expect, it } from "vitest";
import {
  CARD_W,
  COL_STEP,
  CARD_H,
  centringOf,
  fit,
  pyramidOf,
  ROW_STEP,
  widthOf,
} from "../../main/ts/core/pyramid.js";
import type { DuelView, ServerMessage } from "../../main/ts/core/wire.js";

function viewOf(name: string): DuelView {
  const message = JSON.parse(
    readFileSync(`src/test/resources/wire/${name}.json`, "utf8"),
  ) as ServerMessage;
  if (message.view === null) throw new Error(`${name} has no view`);
  return message.view;
}

describe("pyramidOf", () => {
  it("lays out a whole Age I: row 0 on top, face-up and face-down cards in their slots", () => {
    const pyramid = pyramidOf(viewOf("view-in-age"));
    expect(pyramid.slots).toHaveLength(20);
    expect(pyramid.rows).toBe(5);
    expect(pyramid.slots.filter((s) => s.state === "up")).toHaveLength(12);
    const top = pyramid.slots.filter((s) => s.row === 0).map((s) => s.x);
    expect(top).toEqual([4, 6]);
    const bottom = pyramid.slots.filter((s) => s.row === 4);
    expect(bottom.every((s) => s.state === "up")).toBe(true);
  });

  it("keeps a face-down card as its back group, without a component", () => {
    const down = pyramidOf(viewOf("view-in-age")).slots.find((s) => s.state === "down");
    expect(down).toMatchObject({ back: "brown", component: null });
  });

  it("names the component of a face-up card", () => {
    const up = pyramidOf(viewOf("view-in-age")).slots.find((s) => s.state === "up");
    expect(up?.component).toEqual(expect.any(String));
    expect(up?.back).toBeNull();
  });

  it("measures the width in half-cards, a card being two wide", () => {
    const pyramid = pyramidOf(viewOf("view-in-age"));
    const rightmost = Math.max(...pyramid.slots.map((s) => s.x));
    expect(pyramid.cols).toBe(rightmost + 2);
  });

  it("leaves a gap where a card was taken, in a late Age", () => {
    const pyramid = pyramidOf(viewOf("view-with-decision"));
    expect(pyramid.slots).toHaveLength(11);
    const keys = new Set(pyramid.slots.map((s) => `${String(s.row)}:${String(s.x)}`));
    expect(keys.size).toBe(11);
    expect(keys.has("4:0")).toBe(false);
  });

  it("is empty when the view has no structure", () => {
    const pyramid = pyramidOf(viewOf("resync"));
    expect(pyramid).toEqual({ slots: [], rows: 0, cols: 0 });
  });

  it("never shrinks below an extent it was given, so nothing reflows as cards go", () => {
    const early = pyramidOf(viewOf("view-in-age"));
    const late = pyramidOf(viewOf("view-with-decision"), early);
    expect(late.rows).toBe(early.rows);
    expect(late.cols).toBe(early.cols);
  });
});

describe("fit", () => {
  const extent = { rows: 5, cols: 10 };
  const roomy = 100000;

  it("shrinks a pyramid that is wider than its container", () => {
    expect(fit({ width: widthOf(extent.cols) / 2, height: roomy }, extent)).toBeCloseTo(0.5);
  });

  it("shrinks a pyramid that is taller than its container", () => {
    const tall = (extent.rows - 1) * ROW_STEP + CARD_H;
    expect(fit({ width: roomy, height: tall / 2 }, extent)).toBeCloseTo(0.5);
  });

  it("stops enlarging a pyramid at twice and a fifth a card's natural size", () => {
    expect(fit({ width: roomy, height: roomy }, extent)).toBeLessThanOrEqual(2.2);
  });

  it("never reaches zero", () => {
    expect(fit({ width: 0, height: 0 }, extent)).toBeGreaterThan(0);
  });

  it("is 1 for an empty pyramid", () => {
    expect(fit({ width: 100, height: 100 }, { rows: 0, cols: 0 })).toBe(1);
  });
});

describe("the grid", () => {
  it("leaves a 2 px gap between neighbours in a row", () => {
    expect(COL_STEP * 2 - CARD_W).toBe(2);
  });
});

describe("centringOf", () => {
  const slot = (x: number) => ({ row: 0, x, state: "up" as const, component: null, back: null });

  it("shifts nothing while the cards fill the extent", () => {
    expect(centringOf([slot(0), slot(8)], 10)).toBe(0);
  });

  it("moves the remaining cards to the middle of the extent when an edge is taken", () => {
    // Cards at 3..9 inside a 10-wide extent: they belong at 2..8.
    expect(centringOf([slot(3), slot(7)], 10)).toBe(-1);
    expect(centringOf([slot(0), slot(4)], 10)).toBe(2);
  });

  it("has nothing to shift for an empty structure", () => {
    expect(centringOf([], 10)).toBe(0);
  });
});
