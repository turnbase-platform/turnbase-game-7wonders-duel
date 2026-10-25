import { present, type Glyph } from "../core/badges.js";
import { motifOf } from "../core/motif.js";
import type { CatalogEntry } from "../core/wire.js";
import { Icon } from "./icons.js";

export type CardSize = "small" | "standard" | "large";

function GlyphView({ glyph }: { glyph: Glyph }) {
  const tint = glyph.tint === null ? "" : ` tint-${glyph.tint}`;
  return (
    <span className={`glyph${tint}`} title={glyph.title}>
      <Icon id={glyph.icon} />
      {glyph.text !== null && <b>{glyph.text}</b>}
    </span>
  );
}

function Cost({ entry }: { entry: CatalogEntry }) {
  const { coins, resources, freedBy } = entry.cost;
  if (coins === 0 && resources.length === 0 && freedBy.length === 0) {
    return null;
  }
  return (
    <div className="card__cost">
      {coins > 0 && (
        <span className="cost__coin" title={`${String(coins)} coins`}>
          <Icon id="coin" />
          <b>{coins}</b>
        </span>
      )}
      {resources.flatMap((r) =>
        Array.from({ length: r.count }, (_, i) => (
          <Icon key={`${r.resource}${String(i)}`} id={r.resource} />
        )),
      )}
      {freedBy.map((symbol) => (
        <span key={symbol} className="cost__free" title={`free with ${symbol}`}>
          <Icon id={symbol} />
        </span>
      ))}
    </div>
  );
}

interface CardProps {
  entry: CatalogEntry;
  size: CardSize;
  /** A Wonder that has been constructed. */
  built?: boolean;
  /** Whose Wonder it is, for the outline. */
  owner?: "me" | "opp";
}

/** A building, Wonder or token, drawn from its catalog entry alone. */
export function Card({ entry, size, built = false, owner }: CardProps) {
  const colour = entry.tags.find((t) => t.startsWith("color:"))?.slice("color:".length);
  const classes = ["card", `card--${size}`];
  if (entry.type === "WONDER") {
    classes.push("card--wonder", built ? "is-built" : "is-unbuilt");
    if (built && owner) {
      classes.push(`owner-${owner}`);
    }
  } else if (entry.type === "TOKEN") {
    classes.push("card--token");
  } else if (colour) {
    classes.push(`color-${colour}`);
  }
  const chain = entry.badges.find((b) => b.kind === "chain");
  const glyphs = entry.badges.filter((b) => b.kind !== "chain").flatMap(present);
  return (
    <div className={classes.join(" ")} title={entry.name} data-component={entry.id}>
      <div className="card__banner">
        {glyphs.map((g, i) => (
          <GlyphView key={i} glyph={g} />
        ))}
      </div>
      <Cost entry={entry} />
      <div className="card__art">
        <Icon id={motifOf(entry)} />
      </div>
      <div className="card__name">{entry.name}</div>
      {chain && (
        <span className="card__chain" title={`gives ${chain.of.join(", ")}`}>
          <Icon id={chain.of[0] ?? "chain"} />
        </span>
      )}
      {built && <span className="card__paid" />}
    </div>
  );
}

/** The face-down side of a card: one look per back group. */
export function CardBack({ group, size }: { group: string | null; size: CardSize }) {
  return <div className={`card card--back card--${size} back-${group ?? "none"}`} />;
}
