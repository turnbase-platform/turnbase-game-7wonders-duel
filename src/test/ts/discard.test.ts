import { describe, expect, it } from "vitest";
import { discardedOf } from "../../main/ts/core/discard.js";
import type { DuelView } from "../../main/ts/core/wire.js";

const view = (visible: DuelView["visible"]): DuelView => ({ visible }) as DuelView;

describe("discardedOf", () => {
  it("lists the discarded cards in the order they were thrown, and nothing else", () => {
    const out = discardedOf(
      view([
        { component: "baths", zone: "discard", key: "" },
        { component: "palisade", zone: "city", key: "p1" },
        { component: "altar", zone: "discard", key: "" },
      ]),
    );
    expect(out).toEqual(["baths", "altar"]);
  });

  it("is empty when nothing has been discarded", () => {
    expect(discardedOf(view([]))).toEqual([]);
  });
});
