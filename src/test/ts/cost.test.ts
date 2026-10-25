import { describe, expect, it } from "vitest";
import { inCostOrder } from "../../main/ts/core/cost.js";

const of = (...resources: string[]) => resources.map((resource) => ({ resource, count: 1 }));

describe("inCostOrder", () => {
  it("puts manufactured goods before raw materials", () => {
    expect(
      inCostOrder(of("stone", "papyrus", "wood", "glass", "clay")).map((r) => r.resource),
    ).toEqual(["glass", "papyrus", "wood", "clay", "stone"]);
  });

  it("keeps the counts with their resource and leaves the input alone", () => {
    const input = [
      { resource: "stone", count: 2 },
      { resource: "glass", count: 1 },
    ];
    expect(inCostOrder(input)).toEqual([input[1], input[0]]);
    expect(input[0]?.resource).toBe("stone");
  });

  it("sends a resource it does not know to the end", () => {
    expect(inCostOrder(of("mithril", "stone")).map((r) => r.resource)).toEqual([
      "stone",
      "mithril",
    ]);
  });
});
