import { describe, expect, it } from "vitest";
import { present, presentAll } from "../../main/ts/core/badges.js";
import { motifOf } from "../../main/ts/core/motif.js";
import type { Badge } from "../../main/ts/core/wire.js";
import { catalogEntries, catalogEntry } from "./fixture.js";

function badge(kind: string, amount = 0, of: string[] = [], larger = false): Badge {
  return { kind, amount, of, per: 1, larger };
}

describe("present", () => {
  it("repeats a resource icon as often as the card produces it", () => {
    expect(present(badge("produces", 1, ["wood"])).map((g) => g.icon)).toEqual(["wood"]);
    expect(present(badge("produces", 2, ["stone"])).map((g) => g.icon)).toEqual(["stone", "stone"]);
  });

  it("shows a fixed price as one resource-at-1-coin icon per resource", () => {
    expect(present(badge("fixedPrice", 1, ["clay", "wood"])).map((g) => g.icon)).toEqual([
      "priced-clay",
      "priced-wood",
    ]);
  });

  it("separates the resources a card may choose from with a slash", () => {
    const glyphs = present(badge("producesOneOf", 1, ["clay", "stone", "wood"]));
    expect(glyphs.map((g) => g.icon)).toEqual(["clay", "or", "stone", "or", "wood"]);
  });

  it("shows shields, points and coins with their amounts", () => {
    expect(present(badge("shields", 2))).toMatchObject([
      { icon: "shield", text: null },
      { icon: "shield", text: null },
    ]);
    expect(present(badge("points", 3))).toMatchObject([{ icon: "vp", text: "3" }]);
    expect(present(badge("coins", 4))).toMatchObject([{ icon: "coin", text: "4" }]);
    expect(present(badge("loseCoins", 3))).toMatchObject([{ icon: "coin-lost", text: "3" }]);
  });

  it("shows what is counted for a per-card reward, tinted by card colour, with the reward on its corner", () => {
    expect(present(badge("coinsPer", 2, ["color:brown"]))).toMatchObject([
      { icon: "card", tint: "brown", corners: [{ icon: "coin", text: "2", side: "right" }] },
    ]);
    expect(present(badge("pointsPer", 1, ["color:yellow"], true))).toMatchObject([
      { icon: "better" },
      { icon: "card", tint: "yellow", corners: [{ icon: "vp", text: "1", side: "right" }] },
    ]);
  });

  it("piles card colours counted together into one glyph, the reward on the pile", () => {
    const glyphs = present(badge("pointsPer", 1, ["color:brown", "color:grey"], true));
    expect(glyphs).toMatchObject([
      { icon: "better" },
      { icon: "card", pile: ["brown", "grey"], corners: [{ icon: "vp", side: "right" }] },
    ]);
  });

  it("makes the coins and points a guild pays for the same cards one icon", () => {
    const guild = [
      badge("coinsPer", 1, ["color:green"], true),
      badge("pointsPer", 1, ["color:green"], true),
    ];
    expect(presentAll(guild)).toMatchObject([
      { icon: "better" },
      {
        icon: "card",
        tint: "green",
        corners: [
          { icon: "vp", side: "right" },
          { icon: "coin", text: "1", side: "left" },
        ],
      },
    ]);
  });

  it("shows coins as the thing counted, in lots of the badge's `per`", () => {
    const moneylenders = { ...badge("pointsPer", 1, ["coins"], true), per: 3 };
    expect(present(moneylenders)[1]).toMatchObject({ icon: "coin", text: "3" });
  });

  it("shows what a progress token or a Wonder is counted as", () => {
    expect(present(badge("pointsPer", 3, ["progress"]))[0]).toMatchObject({ icon: "progress" });
    expect(present(badge("reduction", 2, ["wonder"]))[1]).toMatchObject({ icon: "wonder" });
  });

  it("shows the symbol a card gives or a building is freed by", () => {
    expect(present(badge("chain", 0, ["horseshoe"]))).toMatchObject([{ icon: "horseshoe" }]);
    expect(present(badge("science", 0, ["wheel"]))).toMatchObject([{ icon: "wheel" }]);
  });

  it("names a glyph for a screen reader", () => {
    expect(present(badge("points", 3))[0]?.title).toBe("3 victory points");
  });

  it("falls back to a generic glyph for a kind it does not know", () => {
    expect(present(badge("somethingNew", 2))).toMatchObject([
      { icon: "unknown", text: "2", title: "somethingNew" },
    ]);
  });

  it("yields at least one glyph for every badge in the catalog", () => {
    for (const e of catalogEntries()) {
      for (const b of e.badges) {
        expect(present(b).length, `${e.id} ${b.kind}`).toBeGreaterThan(0);
        expect(
          present(b).some((g) => g.icon === "unknown"),
          `${e.id} ${b.kind} is unknown`,
        ).toBe(false);
      }
    }
  });
});

describe("motifOf", () => {
  it("is the science symbol for a science card", () => {
    expect(motifOf(catalogEntry("workshop"))).toBe("plumb-bob");
  });

  it("is the shield for a military card", () => {
    expect(motifOf(catalogEntry("guard-tower"))).toBe("shield");
  });

  it("is the resource for a producer", () => {
    expect(motifOf(catalogEntry("lumber-yard"))).toBe("wood");
  });

  it("is the token itself for a progress token", () => {
    expect(motifOf(catalogEntry("agriculture"))).toBe("agriculture");
  });

  it("prefers a replay to the coins a Wonder also gives", () => {
    expect(motifOf(catalogEntry("the-appian-way"))).toBe("play-again");
  });

  it("is a known icon for every entry the board draws", () => {
    // Military tokens are shown by the track, never as cards.
    const drawn = catalogEntries().filter((e) => e.type !== "TOKEN" || e.tags.includes("progress"));
    for (const e of drawn) {
      expect(motifOf(e), e.id).not.toBe("unknown");
    }
  });
});
