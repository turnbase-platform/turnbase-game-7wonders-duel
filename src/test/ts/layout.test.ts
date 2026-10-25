import { describe, expect, it } from "vitest";
import { layoutFor } from "../../main/ts/core/layout.js";

describe("layoutFor", () => {
  it("is wide from 900px of width", () => {
    expect(layoutFor({ width: 900, height: 700 })).toBe("wide");
    expect(layoutFor({ width: 1280, height: 800 })).toBe("wide");
  });

  it("is medium from 600px up to 900px", () => {
    expect(layoutFor({ width: 899, height: 700 })).toBe("medium");
    expect(layoutFor({ width: 600, height: 900 })).toBe("medium");
  });

  it("is portrait below 600px", () => {
    expect(layoutFor({ width: 599, height: 900 })).toBe("portrait");
    expect(layoutFor({ width: 390, height: 844 })).toBe("portrait");
  });

  it("is landscape when a wide container is too short for the panels to stack", () => {
    expect(layoutFor({ width: 844, height: 390 })).toBe("landscape");
    expect(layoutFor({ width: 1100, height: 449 })).toBe("landscape");
    expect(layoutFor({ width: 1100, height: 450 })).toBe("wide");
  });

  it("does not take a short, narrow container for landscape", () => {
    expect(layoutFor({ width: 300, height: 200 })).toBe("portrait");
  });
});
