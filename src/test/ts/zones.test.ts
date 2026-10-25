import { readFileSync } from "node:fs";
import { describe, expect, it } from "vitest";
import { groupZones } from "../../main/ts/core/zones.js";
import type { ServerMessage } from "../../main/ts/core/wire.js";

function viewOf(name: string) {
  const message = JSON.parse(
    readFileSync(`src/test/resources/wire/${name}.json`, "utf8"),
  ) as ServerMessage;
  if (!message.view) throw new Error("fixture has no view");
  return message.view;
}

describe("groupZones", () => {
  it("gathers the components of a place together, in the order the view lists them", () => {
    const groups = groupZones(viewOf("view-after-draft-pick"));
    const draft = groups.find((g) => g.zone === "draft");

    expect(draft?.key).toBe("");
    expect(draft?.components).toHaveLength(3);
  });

  it("keeps places of the same zone but another key apart", () => {
    const groups = groupZones(viewOf("view-in-age"));
    const unbuilt = groups.filter((g) => g.zone === "wonders.unbuilt");

    expect(unbuilt).toHaveLength(2);
    expect(unbuilt.every((g) => g.components.length === 4)).toBe(true);
  });

  it("counts face-down cards of a place without naming them", () => {
    const groups = groupZones(viewOf("view-in-age"));
    const down = groups.find((g) => g.zone === "structure.down" && g.key === "1:3");

    expect(down?.components).toEqual([]);
    expect(down?.hidden).toBe(1);
  });

  it("puts a place that holds both seen and hidden cards in one group", () => {
    const groups = groupZones(viewOf("view-after-draft-pick"));
    const deck = groups.filter((g) => g.zone === "deck" && g.key === "1");

    expect(deck).toHaveLength(1);
    expect(deck[0]?.hidden).toBe(20);
  });
});
