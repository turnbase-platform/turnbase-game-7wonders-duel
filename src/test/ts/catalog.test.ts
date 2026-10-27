import { describe, expect, it } from "vitest";
import { makeCatalog } from "../../main/ts/core/catalog.js";
import type { CatalogEntry } from "../../main/ts/core/wire.js";

function entry(
  id: string,
  name: string,
  tags: string[],
  type: CatalogEntry["type"] = "CARD",
): CatalogEntry {
  return {
    id,
    name,
    description: "",
    type,
    tags,
    back: null,
    cost: { coins: 0, resources: [], freedBy: [] },
    badges: [],
  };
}

const catalog = makeCatalog({
  entries: [
    entry("lumber-yard", "Lumber Yard", ["age:1", "color:brown"]),
    entry("the-colossus", "The Colossus", ["wonder"], "WONDER"),
  ],
});

describe("the catalog", () => {
  it("finds an entry by id", () => {
    expect(catalog.get("lumber-yard")?.name).toBe("Lumber Yard");
    expect(catalog.get("nope")).toBeUndefined();
  });

  it("names a known component and falls back to the id for an unknown one", () => {
    expect(catalog.name("the-colossus")).toBe("The Colossus");
    expect(catalog.name("mystery")).toBe("mystery");
  });

  it("reads the value of a tag by its prefix", () => {
    expect(catalog.tag("lumber-yard", "color")).toBe("brown");
    expect(catalog.tag("lumber-yard", "age")).toBe("1");
    expect(catalog.tag("lumber-yard", "missing")).toBeNull();
    expect(catalog.tag("nope", "color")).toBeNull();
  });
});
