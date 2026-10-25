import { describe, expect, it } from "vitest";
import { STYLES, injectStyles } from "../../main/ts/ui/styles.js";

describe("styles", () => {
  it("defines every custom property the sheet reads", () => {
    const used = new Set([...STYLES.matchAll(/var\((--[\w-]+)/g)].map((m) => m[1]));
    const defined = new Set([...STYLES.matchAll(/(--[\w-]+)\s*:/g)].map((m) => m[1]));
    expect([...used].filter((name) => !defined.has(name))).toEqual([]);
  });

  it("restates the palette for the dark scheme", () => {
    expect(STYLES).toContain("@media (prefers-color-scheme: dark)");
    expect(STYLES.match(/--ground:/g)?.length).toBe(2);
  });

  it("injects itself once into the document head", () => {
    injectStyles(document);
    expect(document.head.querySelectorAll("style").length).toBeGreaterThan(0);
    expect(document.head.textContent).toContain("--ground");
  });
});
