import { useState } from "react";
import { cityOf } from "../core/city.js";
import type { Seats } from "../core/seats.js";
import { seatName, type Session } from "../core/session.js";
import type { ComponentId } from "../core/wire.js";
import { Card, type CardSize } from "./Card.js";
import { Icon } from "./icons.js";

export type Side = "me" | "opp";

/** The tab label of a city: "You" and "Opponent", or the names when the viewer is only watching. */
export function sideLabel(session: Session, seats: Seats, side: Side): string {
  if (seats.spectator) return seatName(session, seats[side]);
  return side === "me" ? "You" : "Opponent";
}

interface TabProps {
  label: string;
  active: boolean;
  tint?: Side;
  onPick: () => void;
}

export function Tab({ label, active, tint, onPick }: TabProps) {
  return (
    <button
      type="button"
      role="tab"
      aria-selected={active}
      className={`tab${tint ? ` owner-${tint}` : ""}`}
      onClick={onPick}
    >
      {label}
    </button>
  );
}

/** The city of one seat: coins and production, the colour stacks, the Wonders and progress tokens. */
export function City({ session, seats, side }: { session: Session; seats: Seats; side: Side }) {
  const [peek, setPeek] = useState<ComponentId | null>(null);
  const { view, catalog } = session;
  if (view === null || catalog === null) return null;
  const city = cityOf(view, catalog, seats[side]);
  const peekable = (id: ComponentId, size: CardSize, built = false) => {
    const entry = catalog.get(id);
    if (entry === undefined) return null;
    const show = (): void => {
      setPeek(id);
    };
    return (
      <button
        key={id}
        type="button"
        className="peekable"
        aria-label={entry.name}
        onMouseEnter={show}
        onFocus={show}
        onClick={() => {
          setPeek(peek === id ? null : id);
        }}
        onMouseLeave={() => {
          setPeek(null);
        }}
        onBlur={() => {
          setPeek(null);
        }}
      >
        <Card entry={entry} size={size} built={built} owner={built ? side : undefined} />
      </button>
    );
  };
  const peeked = peek === null ? undefined : catalog.get(peek);
  return (
    <section className={`city owner-${side}`}>
      <h3>{sideLabel(session, seats, side)}</h3>
      <p className="city__line">
        <span className="purse" title={`${String(city.coins)} coins`}>
          <Icon id="coin" /> <b>{city.coins}</b>
        </span>
        {city.produces.map((r) => (
          <span key={r.resource} className="glyph" title={`${String(r.count)} ${r.resource}`}>
            <Icon id={r.resource} />
            {r.count > 1 && <b>{r.count}</b>}
          </span>
        ))}
      </p>
      <div className="stacks">
        {city.stacks.map((stack) => (
          <div key={stack.colour} className={`stack color-${stack.colour}`}>
            {stack.cards.map((id) => peekable(id, "small"))}
          </div>
        ))}
      </div>
      {city.wonders.length > 0 && (
        <div className="wonders">
          {city.wonders.map((w) => peekable(w.id, "standard", w.built))}
        </div>
      )}
      {city.tokens.length > 0 && (
        <div className="tokens">{city.tokens.map((id) => peekable(id, "standard"))}</div>
      )}
      {peeked && (
        <div className="peek" aria-hidden="true">
          <Card entry={peeked} size="large" />
        </div>
      )}
    </section>
  );
}
