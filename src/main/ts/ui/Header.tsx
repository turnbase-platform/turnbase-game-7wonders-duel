import type { Seats } from "../core/seats.js";
import type { Session } from "../core/session.js";
import { Purse } from "./Purse.js";
import { Status } from "./Status.js";

/** The Age, the turn and both purses, for the arrangements that show one city at a time. */
export function Header({ session, seats }: { session: Session; seats: Seats }) {
  if (session.view === null) return null;
  return (
    <header className="header">
      <Status session={session} seats={seats} />
      <span className="purses">
        <Purse session={session} player={seats.me} side="me" named />
        <Purse session={session} player={seats.opp} side="opp" named />
        {seats.spectator && <span className="who">(spectating)</span>}
      </span>
    </header>
  );
}
