import { describe, expect, it } from "vitest";
import type { QuoteView } from "../../main/ts/core/wire.js";
import { priceText } from "../../main/ts/ui/Actions.js";

const quote = (over: Partial<QuoteView>): QuoteView => ({
  player: "",
  component: "",
  coins: 0,
  tradeCost: 0,
  total: 0,
  chained: false,
  affordable: true,
  takeable: true,
  lines: [],
  ...over,
});

const line = { resource: "wood", needed: 1, produced: 1, discounted: 0, bought: 0, unitPrice: 2 };

describe("priceText", () => {
  it("names the coins to pay, without a trade remark", () => {
    expect(priceText(quote({ coins: 1, tradeCost: 3, total: 4, lines: [line] }))).toBe("4 coins");
  });

  it("says production satisfied when the city covers the resources and no coins are due", () => {
    expect(priceText(quote({ lines: [line] }))).toBe("Production satisfied");
  });

  it("says free for a card that costs nothing", () => {
    expect(priceText(quote({}))).toBe("Free");
  });

  it("says free by chain when a chain symbol waives the cost", () => {
    expect(priceText(quote({ chained: true }))).toBe("Free by chain");
  });
});
