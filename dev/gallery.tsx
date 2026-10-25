// Every catalog entry at every size, for looking at cards without playing a game
// (http://localhost:5173/dev/gallery.html). Switch the OS or browser colour scheme to see the dark
// palette. The catalog is the wire fixture, so it is the real one.
import { createRoot } from "react-dom/client";
import resync from "../src/test/resources/wire/resync.json";
import type { CatalogEntry } from "../src/main/ts/core/wire";
import { Card, CardBack, type CardSize } from "../src/main/ts/ui/Card";
import { IconSprite, SPRITE_IDS, Icon } from "../src/main/ts/ui/icons";
import { injectStyles } from "../src/main/ts/ui/styles";

const SIZES: CardSize[] = ["small", "standard", "large"];
// Military tokens are not drawn anywhere: the track already says what they are worth.
const entries = (
  resync as unknown as { catalog: { entries: CatalogEntry[] } }
).catalog.entries.filter((e) => e.type !== "TOKEN" || e.tags.includes("progress"));
const backs = [...new Set(entries.map((e) => e.back).filter((b): b is string => b !== null))];

function Section({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <section style={{ margin: "12px 0" }}>
      <h2 style={{ font: "600 14px var(--sans)" }}>{title}</h2>
      <div style={{ display: "flex", flexWrap: "wrap", gap: 10, alignItems: "flex-start" }}>
        {children}
      </div>
    </section>
  );
}

function Gallery() {
  return (
    <div
      style={{ background: "var(--ground)", color: "var(--ink)", padding: 16, minHeight: "100vh" }}
    >
      <IconSprite />
      <Section title="Icons">
        {SPRITE_IDS.map((id) => (
          <span key={id} title={id} style={{ fontSize: 28, display: "inline-flex" }}>
            <Icon id={id} />
          </span>
        ))}
      </Section>
      {SIZES.map((size) => (
        <Section key={size} title={size}>
          {entries.map((e) => (
            <Card key={e.id} entry={e} size={size} />
          ))}
        </Section>
      ))}
      <Section title="Wonders built (me, opponent)">
        {entries
          .filter((e) => e.type === "WONDER")
          .slice(0, 4)
          .map((e, i) => (
            <Card key={e.id} entry={e} size="standard" built owner={i % 2 ? "opp" : "me"} />
          ))}
      </Section>
      <Section title="Backs">
        {[...backs, null].map((b) => (
          <CardBack key={b ?? "none"} group={b} size="standard" />
        ))}
      </Section>
    </div>
  );
}

const root = document.getElementById("root");
if (!root) {
  throw new Error("no #root");
}
injectStyles();
createRoot(root).render(<Gallery />);
