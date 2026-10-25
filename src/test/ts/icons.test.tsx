import { renderToStaticMarkup } from "react-dom/server";
import { describe, expect, it } from "vitest";
import { present } from "../../main/ts/core/badges.js";
import { motifOf } from "../../main/ts/core/motif.js";
import { catalogEntries } from "./fixture.js";
import { Icon, IconSprite, SPRITE_IDS } from "../../main/ts/ui/icons.js";

describe("icon sprite", () => {
  it("draws every icon a badge or a motif in the catalog can ask for", () => {
    const wanted = new Set<string>();
    for (const e of catalogEntries()) {
      wanted.add(motifOf(e));
      for (const b of e.badges) {
        present(b).forEach((g) => wanted.add(g.icon));
      }
      e.cost.freedBy.forEach((s) => wanted.add(s));
      e.cost.resources.forEach((r) => wanted.add(r.resource));
    }
    const missing = [...wanted].filter(
      (id) => !SPRITE_IDS.includes(id) && !id.includes(":") && id !== "progress" && id !== "wonder",
    );
    expect(missing).toEqual([]);
    for (const id of ["card", "progress", "wonder", "larger", "chain", "coin", "vp", "unknown"]) {
      expect(SPRITE_IDS).toContain(id);
    }
  });

  it("names every symbol", () => {
    const sprite = renderToStaticMarkup(<IconSprite />);
    for (const id of SPRITE_IDS) {
      expect(sprite).toContain(`<symbol id="i-${id}" viewBox="0 0 24 24"><title>`);
    }
  });

  it("refers to the sprite, and to the unknown icon for an id it lacks", () => {
    expect(renderToStaticMarkup(<Icon id="shield" />)).toContain('href="#i-shield"');
    expect(renderToStaticMarkup(<Icon id="not-an-icon" />)).toContain('href="#i-unknown"');
  });
});
