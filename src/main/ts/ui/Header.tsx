import { seatName, type Session } from "../core/session.js";
import type { Seats } from "../core/seats.js";

export function Header({ session, seats }: { session: Session; seats: Seats }) {
  const view = session.view;
  if (view === null) return null;
  const coins = (id: string) => view.players.find((p) => p.id === id)?.coins ?? 0;
  return (
    <header className="header">
      <span className="phase">{view.phase === "AGE" ? `Age ${String(view.age)}` : view.phase}</span>
      <span className="turn">
        {view.phase === "OVER" ? "Game over" : `${seatName(session, view.active)} to act`}
      </span>
      {[seats.me, seats.opp].map((id, i) => (
        <span key={id} className={`purse ${i === 0 ? "owner-me" : "owner-opp"}`}>
          {seatName(session, id)}: {coins(id)} coins
        </span>
      ))}
      {seats.spectator && <span>(spectating)</span>}
    </header>
  );
}
