import { useState } from "react";
import { cityOf } from "../core/city.js";
import type { Seats } from "../core/seats.js";
import { seatName, type Session } from "../core/session.js";
import type { CatalogEntry, ComponentId } from "../core/wire.js";
import { Card, CardBack, type CardSize } from "./Card.js";
import { Purse } from "./Purse.js";
import { HoverQuote } from "./QuoteTable.js";
import { Science } from "./Science.js";
import { replayStep } from "./Status.js";

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

/**
 * A card as it lies in a city. A built Wonder shows what built it, as on the table: a card face
 * down, tucked under the Wonder and sticking out on its right.
 */
function WonderSlot({
  entry,
  size,
  built,
}: {
  entry: CatalogEntry;
  size: CardSize;
  built: boolean;
}) {
  if (entry.type !== "WONDER") return <Card entry={entry} size={size} />;
  return (
    <span className="wonder">
      {built && (
        <span className="wonder__under" aria-hidden="true">
          <CardBack group={null} size={size} />
        </span>
      )}
      <Card entry={entry} size={size} built={built} />
    </span>
  );
}

/** The city of one seat: its owner, coins, the Wonders, the colour stacks and progress tokens. */
export function City({
  session,
  seats,
  side,
  title,
  coins = false,
}: {
  session: Session;
  seats: Seats;
  side: Side;
  /** Names the city and marks it while its owner is to act, when it stands on its own rather than under a tab. */
  title?: string;
  /** Shows the city's coins in its head, when no header does. */
  coins?: boolean;
}) {
  const [peek, setPeek] = useState<ComponentId | null>(null);
  const { view, catalog } = session;
  if (view === null || catalog === null) return null;
  const city = cityOf(view, catalog, seats[side]);
  const peekable = (id: ComponentId, size: CardSize, built = false) => {
    const entry = catalog.get(id);
    if (entry === undefined) return null;
    const quote = view.quotes.find((q) => q.player === seats[side] && q.component === id);
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
        <WonderSlot entry={entry} size={size} built={built} />
        {quote && !built && <HoverQuote quote={quote} brief={side === "opp"} />}
      </button>
    );
  };
  const replay = replayStep(session);
  const peeked = peek === null ? undefined : catalog.get(peek);
  return (
    <section className={`city owner-${side}`}>
      {title !== undefined && (
        <header className="city__head">
          <h3>{title}</h3>
          {view.active === seats[side] && view.phase !== "OVER" && (
            <span
              // A new key restarts the pulse for every action that grants a replay.
              key={replay === null ? "turn" : `pulse-${String(replay)}`}
              className={`turn city__turn${replay === null ? "" : " is-pulse"}`}
              title="To act"
              role="img"
              aria-label="To act"
            />
          )}
          <Science symbols={city.science} />
          {coins && <Purse session={session} player={seats[side]} side={side} big />}
        </header>
      )}
      {title === undefined && <Science symbols={city.science} />}
      {city.wonders.length > 0 && (
        <div className="wonders">
          {city.wonders.map((w) => peekable(w.id, "standard", w.built))}
        </div>
      )}
      <div className="stacks">
        {city.stacks.map((stack) => (
          <div key={stack.colour} className={`stack color-${stack.colour}`}>
            {stack.cards.map((id) => peekable(id, "standard"))}
          </div>
        ))}
      </div>
      {city.tokens.length > 0 && (
        <div className="tokens">{city.tokens.map((id) => peekable(id, "standard"))}</div>
      )}
      {peeked && (
        <div className="peek" aria-hidden="true">
          <WonderSlot
            entry={peeked}
            size="large"
            built={city.wonders.some((w) => w.id === peeked.id && w.built)}
          />
        </div>
      )}
    </section>
  );
}
