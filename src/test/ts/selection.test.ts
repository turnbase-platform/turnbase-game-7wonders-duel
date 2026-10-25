import { describe, expect, it } from "vitest";
import type { Offer } from "../../main/ts/core/moves.js";
import { attention, selectionReducer, selectedOf } from "../../main/ts/core/selection.js";

function offer(target: string, type: "build" | "discard" | "draftWonder" = "build"): Offer {
  return { target, label: type, action: { type, component: target } };
}

describe("selectionReducer", () => {
  it("selects a card, and selecting it again lets go", () => {
    const one = selectionReducer(null, { type: "toggle", component: "a" });
    expect(one).toBe("a");
    expect(selectionReducer(one, { type: "toggle", component: "a" })).toBeNull();
  });

  it("moves the selection to another card", () => {
    expect(selectionReducer("a", { type: "toggle", component: "b" })).toBe("b");
  });

  it("clears", () => {
    expect(selectionReducer("a", { type: "clear" })).toBeNull();
  });
});

describe("selectedOf", () => {
  it("keeps a selection that is still offered", () => {
    expect(selectedOf("a", [offer("a"), offer("a", "discard")])).toBe("a");
  });

  it("drops a selection the view no longer offers, such as after the card was taken", () => {
    expect(selectedOf("a", [offer("b")])).toBeNull();
    expect(selectedOf(null, [offer("b")])).toBeNull();
  });
});

describe("attention", () => {
  it("points at the structure when the viewer has something to do there", () => {
    expect(attention([offer("a")])).toBe("structure");
  });

  it("points nowhere when the viewer has nothing to do", () => {
    expect(attention([])).toBeNull();
  });
});
