import { cuesOf, type Cue } from "../core/cues.js";
import { seatName, type Session } from "../core/session.js";
import type { Seats } from "../core/seats.js";

/** `+2` / `−2`: what a coins cue floats off the purse it belongs to. */
function signed(amount: number): string {
  return amount < 0 ? `−${String(-amount)}` : `+${String(amount)}`;
}

export function Header({ session, seats }: { session: Session; seats: Seats }) {
  const view = session.view;
  if (view === null) return null;
  const step = session.history.at(-1);
  const cues: Cue[] = step === undefined ? [] : cuesOf(step.events);
  const coins = (id: string) => view.players.find((p) => p.id === id)?.coins ?? 0;
  const replays = cues.some((c) => c.kind === "turn" && c.granted);
  return (
    <header className="header">
      <span className="phase">{view.phase === "AGE" ? `Age ${String(view.age)}` : view.phase}</span>
      <span
        // A new key restarts the pulse for every action that grants a replay.
        key={replays ? `pulse-${String(step?.number)}` : "turn"}
        className={`turn${replays ? " is-pulse" : ""}`}
      >
        {view.phase === "OVER" ? "Game over" : `${seatName(session, view.active)} to act`}
      </span>
      {[seats.me, seats.opp].map((id, i) => (
        <span key={id} className={`purse ${i === 0 ? "owner-me" : "owner-opp"}`}>
          {seatName(session, id)}: {coins(id)} coins
          {cues.map(
            (c) =>
              c.kind === "coins" &&
              c.player === id && (
                <span
                  key={`${String(step?.number)}:${String(c.id)}`}
                  className={`float ${c.amount < 0 ? "is-loss" : "is-gain"}`}
                >
                  {signed(c.amount)}
                </span>
              ),
          )}
        </span>
      ))}
      {seats.spectator && <span>(spectating)</span>}
    </header>
  );
}
