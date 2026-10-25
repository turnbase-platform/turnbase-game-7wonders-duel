import type { CSSProperties } from "react";
import { PILE_STEP, presentAll, span, type Glyph } from "../core/badges.js";
import { inCostOrder } from "../core/cost.js";
import { motifOf } from "../core/motif.js";
import type { CatalogEntry } from "../core/wire.js";
import { Icon } from "./icons.js";

export type CardSize = "small" | "standard" | "large";

function GlyphView({ glyph }: { glyph: Glyph }) {
  const tint = glyph.tint === null ? "" : ` tint-${glyph.tint}`;
  return (
    <span
      className={`glyph${glyph.pile.length > 1 ? " is-pile" : ""}${tint}${glyph.corners.length > 0 ? " has-corner" : ""}${glyph.icon === "better" ? " is-better" : ""}`}
      title={glyph.title}
      style={{ "--n": glyph.pile.length } as CSSProperties}
    >
      {glyph.pile.length > 1 ? (
        glyph.pile.map((colour, i) => (
          <span
            key={colour}
            className={`glyph__pile tint-${colour}`}
            style={{ "--i": i * PILE_STEP } as CSSProperties}
          >
            <Icon id={glyph.icon} />
          </span>
        ))
      ) : (
        <Icon id={glyph.icon} />
      )}
      {glyph.text !== null && <b>{glyph.text}</b>}
      {glyph.corners.map((c) => (
        <span key={c.side} className={`glyph__corner is-${c.side}`}>
          <Icon id={c.icon} />
          <b>{c.text}</b>
        </span>
      ))}
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
      {(coins > 0 || resources.length > 0) && (
        <div className="cost__bar">
          {coins > 0 && (
            <span className="cost__coin" title={`${String(coins)} coins`}>
              <Icon id="coin" />
              <b>{coins}</b>
            </span>
          )}
          {inCostOrder(resources).flatMap((r) =>
            Array.from({ length: r.count }, (_, i) => (
              <Icon key={`${r.resource}${String(i)}`} id={r.resource} />
            )),
          )}
        </div>
      )}
      {freedBy.length > 0 && (
        <div className="cost__bar">
          {freedBy.map((symbol) => (
            <span key={symbol} className="cost__free" title={`free with ${symbol}`}>
              <Icon id={symbol} />
            </span>
          ))}
        </div>
      )}
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

/** A progress token is round and its icon is the whole token: nothing is drawn around it. */
function ProgressToken({ entry, size }: { entry: CatalogEntry; size: CardSize }) {
  return (
    <div className={`token token--${size}`} title={entry.name} data-component={entry.id}>
      <Icon id={entry.id} />
    </div>
  );
}

/** A building, Wonder or token, drawn from its catalog entry alone. */
export function Card({ entry, size, built = false, owner }: CardProps) {
  if (entry.type === "TOKEN" && entry.tags.includes("progress")) {
    return <ProgressToken entry={entry} size={size} />;
  }
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
  const glyphs = presentAll(entry.badges);
  return (
    <div className={classes.join(" ")} title={entry.name} data-component={entry.id}>
      <div
        className="card__banner"
        style={{ "--span": glyphs.reduce((n, g) => n + span(g), 0) } as CSSProperties}
      >
        {glyphs.map((g, i) => (
          <GlyphView key={i} glyph={g} />
        ))}
        {chain && (
          <span className="card__chain" title={`gives ${chain.of.join(", ")}`}>
            <Icon id={chain.of[0] ?? "chain"} />
          </span>
        )}
      </div>
      <Cost entry={entry} />
      <div className="card__art">
        <Icon id={motifOf(entry)} />
      </div>
      <div className="card__name">{entry.name}</div>
      {built && <span className="card__paid" />}
    </div>
  );
}

/** The face-down side of a card: one look per back group. */
export function CardBack({ group, size }: { group: string | null; size: CardSize }) {
  return <div className={`card card--back card--${size} back-${group ?? "none"}`} />;
}
