import type { CSSProperties } from "react";
import { PILE_STEP, presentAll, span, type Glyph } from "../core/badges.js";
import { inCostOrder } from "../core/cost.js";
import { motifOf } from "../core/motif.js";
import type { CatalogEntry } from "../core/wire.js";
import { Icon } from "./icons.js";
import { HoverFloat } from "./QuoteTable.js";

export type CardSize = "small" | "standard" | "large";

function GlyphView({ glyph }: { glyph: Glyph }) {
  const tint = glyph.tint === null ? "" : ` tint-${glyph.tint}`;
  return (
    <span
      className={`glyph${glyph.pile.length > 1 ? " is-pile" : ""}${tint}${glyph.corners.length > 0 ? " has-corner" : ""}${glyph.icon === "better" ? " is-better" : ""}`}
      role="img"
      aria-label={glyph.title}
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
            <span className="cost__coin" role="img" aria-label={`${String(coins)} coins`}>
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
            <span key={symbol} className="cost__free" role="img" aria-label={`free with ${symbol}`}>
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
}

/** A progress token is round and its icon is the whole token: nothing is drawn around it. */
function ProgressToken({ entry, size }: { entry: CatalogEntry; size: CardSize }) {
  return (
    <div
      className={`token token--${size}`}
      role="img"
      aria-label={entry.name}
      data-component={entry.id}
    >
      <Icon id={entry.id} />
      <HoverFloat>
        <b className="tip__name">{entry.name}</b>
        {entry.description !== "" && <span className="tip__text">{entry.description}</span>}
      </HoverFloat>
    </div>
  );
}

/** A building, Wonder or token, drawn from its catalog entry alone. */
export function Card({ entry, size, built = false }: CardProps) {
  if (entry.type === "TOKEN" && entry.tags.includes("progress")) {
    return <ProgressToken entry={entry} size={size} />;
  }
  const colour = entry.tags.find((t) => t.startsWith("color:"))?.slice("color:".length);
  const classes = ["card", `card--${size}`];
  if (entry.type === "WONDER") {
    classes.push("card--wonder", built ? "is-built" : "is-unbuilt");
  } else if (entry.type === "TOKEN") {
    classes.push("card--token");
  } else if (colour) {
    classes.push(`color-${colour}`);
  }
  const chain = entry.badges.find((b) => b.kind === "chain");
  const glyphs = presentAll(entry.badges);
  return (
    <div className={classes.join(" ")} data-component={entry.id}>
      <div
        className="card__banner"
        style={{ "--span": glyphs.reduce((n, g) => n + span(g), 0) } as CSSProperties}
      >
        {glyphs.map((g, i) => (
          <GlyphView key={i} glyph={g} />
        ))}
        {chain && (
          <span className="card__chain" role="img" aria-label={`gives ${chain.of.join(", ")}`}>
            <Icon id={chain.of[0] ?? "chain"} />
          </span>
        )}
      </div>
      <Cost entry={entry} />
      <div className="card__art">
        <Icon id={motifOf(entry)} />
      </div>
      <div className="card__name">{entry.name}</div>
      {size === "small" && <HoverFloat>{entry.name}</HoverFloat>}
    </div>
  );
}

/** The face-down side of a card: one look per back group. */
export function CardBack({ group, size }: { group: string | null; size: CardSize }) {
  return <div className={`card card--back card--${size} back-${group ?? "none"}`} />;
}
