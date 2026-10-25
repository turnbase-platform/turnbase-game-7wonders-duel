import type { Seats } from "../core/seats.js";
import { seatName, type Session } from "../core/session.js";

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

/** The city of one seat; plan 13c draws it, until then coins and Wonders by name. */
export function City({ session, seats, side }: { session: Session; seats: Seats; side: Side }) {
  const view = session.view;
  const catalog = session.catalog;
  if (view === null || catalog === null) return null;
  const id = seats[side];
  const wonders = view.visible.filter((p) => p.zone.startsWith("wonders.") && p.key === id);
  return (
    <section className={`city owner-${side}`}>
      <h3>{sideLabel(session, seats, side)}</h3>
      <p>{view.players.find((p) => p.id === id)?.coins ?? 0} coins</p>
      {wonders.length > 0 && <p>{wonders.map((p) => catalog.name(p.component)).join(", ")}</p>}
    </section>
  );
}
