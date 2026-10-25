import { renderToStaticMarkup } from "react-dom/server";
import { describe, expect, it } from "vitest";
import { catalogEntries, catalogEntry } from "./fixture.js";
import { Card, CardBack } from "../../main/ts/ui/Card.js";

function html(node: React.ReactElement): string {
  return renderToStaticMarkup(node);
}

function count(haystack: string, needle: string): number {
  return haystack.split(needle).length - 1;
}

describe("Card", () => {
  it("shows the name and the colour of a building", () => {
    const out = html(<Card entry={catalogEntry("lumber-yard")} size="standard" />);
    expect(out).toContain("Lumber Yard");
    expect(out).toContain("color-brown");
    expect(out).toContain("card--standard");
  });

  it("draws one glyph in the banner per glyph its badges present", () => {
    // The Merchants' Guild: coins per yellow card (coin, card) and points per yellow card in the
    // larger city (vp, card, larger).
    const out = html(<Card entry={catalogEntry("merchants-guild")} size="standard" />);
    const banner = out.slice(out.indexOf("card__banner"), out.indexOf("card__art"));
    expect(count(banner, 'class="glyph')).toBe(5);
  });

  it("shows no cost overlay for a free card", () => {
    expect(html(<Card entry={catalogEntry("lumber-yard")} size="standard" />)).not.toContain(
      "card__cost",
    );
  });

  it("shows coins and each resource of a cost", () => {
    const out = html(<Card entry={catalogEntry("the-appian-way")} size="large" />);
    const cost = out.slice(out.indexOf("card__cost"), out.indexOf("card__art"));
    expect(count(cost, "icon-clay")).toBe(2);
    expect(count(cost, "icon-stone")).toBe(2);
    expect(count(cost, "icon-papyrus")).toBe(1);
  });

  it("shows the symbol that makes a building free", () => {
    const chained = catalogEntries().find((e) => e.cost.freedBy.length > 0);
    if (!chained) {
      throw new Error("no chained card in the catalog");
    }
    const out = html(<Card entry={chained} size="standard" />);
    expect(out).toContain("cost__free");
    expect(out).toContain(`icon-${chained.cost.freedBy[0]}`);
  });

  it("puts the chain symbol a building gives at the top right, not in the banner", () => {
    const out = html(<Card entry={catalogEntry("stable")} size="standard" />);
    expect(out).toContain("card__chain");
    expect(out.slice(out.indexOf("card__banner"), out.indexOf("card__art"))).not.toContain(
      "icon-horseshoe",
    );
  });

  it("draws a Wonder in its own variant, faded until it is built", () => {
    const unbuilt = html(<Card entry={catalogEntry("the-appian-way")} size="standard" />);
    expect(unbuilt).toContain("card--wonder");
    expect(unbuilt).toContain("is-unbuilt");
    const built = html(
      <Card entry={catalogEntry("the-appian-way")} size="standard" built owner="me" />,
    );
    expect(built).toContain("is-built");
    expect(built).toContain("owner-me");
    expect(built).not.toContain("is-unbuilt");
  });

  it("draws a progress token as a token with its own picture", () => {
    const out = html(<Card entry={catalogEntry("agriculture")} size="standard" />);
    expect(out).toContain("card--token");
    expect(out).toContain("icon-agriculture");
  });

  it("hides the name bar on a small card but keeps it as the title", () => {
    const out = html(<Card entry={catalogEntry("lumber-yard")} size="small" />);
    expect(out).toContain('title="Lumber Yard"');
  });
});

describe("CardBack", () => {
  it("is coloured by its back group", () => {
    const out = html(<CardBack group="violet" size="standard" />);
    expect(out).toContain("back-violet");
    expect(out).toContain("card--standard");
  });

  it("has a neutral face when it has no group", () => {
    expect(html(<CardBack group={null} size="small" />)).toContain("back-none");
  });
});
