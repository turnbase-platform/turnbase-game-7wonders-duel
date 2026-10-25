import { readFileSync } from "node:fs";
import { describe, expect, it } from "vitest";
import { makeCatalog } from "../../main/ts/core/catalog.js";
import { cityOf } from "../../main/ts/core/city.js";
import type { DuelView, ServerMessage } from "../../main/ts/core/wire.js";

const ONE = "00000000-0000-0000-0000-000000000001";
const TWO = "00000000-0000-0000-0000-000000000002";

function message(name: string): ServerMessage {
  return JSON.parse(readFileSync(`src/test/resources/wire/${name}.json`, "utf8")) as ServerMessage;
}

const catalog = makeCatalog(message("resync").catalog ?? { entries: [] });

function viewOf(name: string): DuelView {
  const { view } = message(name);
  if (view === null) throw new Error(`${name} has no view`);
  return view;
}

const late = viewOf("view-with-decision");
const early = viewOf("view-in-age");

describe("cityOf", () => {
  it("stacks a city's cards by colour in the fixed order, cards in the order built", () => {
    const stacks = cityOf(late, catalog, ONE).stacks;
    expect(stacks.map((s) => s.colour)).toEqual([
      "brown",
      "grey",
      "blue",
      "green",
      "yellow",
      "red",
      "purple",
    ]);
    expect(stacks.find((s) => s.colour === "blue")?.cards).toEqual([
      "altar",
      "rostrum",
      "town-hall",
    ]);
  });

  it("leaves out a colour the city has no card of", () => {
    const colours = cityOf(late, catalog, TWO).stacks.map((s) => s.colour);
    expect(colours).toEqual(["brown", "grey", "green", "yellow", "red"]);
  });

  it("has no stacks for a city that has built nothing", () => {
    expect(cityOf(early, catalog, ONE).stacks).toEqual([]);
  });

  it("reads the coins of the player", () => {
    expect(cityOf(late, catalog, ONE).coins).toBe(3);
    expect(cityOf(late, catalog, TWO).coins).toBe(9);
  });

  it("lists Wonders built and unbuilt, in a stable order", () => {
    const wonders = cityOf(late, catalog, ONE).wonders;
    expect(wonders.map((w) => [w.id, w.built])).toEqual([
      ["the-appian-way", true],
      ["the-great-lighthouse", true],
      ["the-hanging-gardens", true],
    ]);
    expect(cityOf(early, catalog, TWO).wonders.every((w) => !w.built)).toBe(true);
    expect(cityOf(early, catalog, TWO).wonders.map((w) => w.id)).toEqual([
      "piraeus",
      "the-colossus",
      "the-pyramids",
      "the-temple-of-artemis",
    ]);
  });

  it("keeps progress tokens apart from the colour stacks", () => {
    const withToken: DuelView = {
      ...early,
      visible: [...early.visible, { component: "agriculture", zone: "city", key: ONE }],
    };
    const city = cityOf(withToken, catalog, ONE);
    expect(city.tokens).toEqual(["agriculture"]);
    expect(city.stacks).toEqual([]);
  });

  it("tallies the resources its cards produce, in the order of the resource row", () => {
    expect(cityOf(late, catalog, ONE).produces).toEqual([
      { resource: "wood", count: 1 },
      { resource: "papyrus", count: 1 },
    ]);
    expect(cityOf(early, catalog, ONE).produces).toEqual([]);
  });

  it("counts science symbols across cards and the Law token, listing every symbol the game has", () => {
    const owning = (...ids: string[]): DuelView => ({
      ...early,
      visible: [
        ...early.visible,
        ...ids.map((component) => ({ component, zone: "city", key: ONE })),
      ],
    });
    const held = (view: DuelView) =>
      Object.fromEntries(cityOf(view, catalog, ONE).science.map((s) => [s.symbol, s.count]));

    expect(Object.keys(held(early)).sort()).toEqual([
      "astrolabe",
      "mortar-and-pestle",
      "plumb-bob",
      "quill",
      "scales",
      "sundial",
      "wheel",
    ]);
    expect(Object.values(held(early)).every((n) => n === 0)).toBe(true);
    expect(held(owning("apothecary", "school", "law", "library"))).toMatchObject({
      wheel: 2,
      scales: 1,
      quill: 1,
      sundial: 0,
    });
  });
});
