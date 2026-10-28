import { useState } from "react";
import type { Seats } from "../core/seats.js";
import { seatName, type Session } from "../core/session.js";
import type { DuelView, ScoreView, VictoryReason } from "../core/wire.js";

const HOW: Record<VictoryReason, string> = {
  MILITARY: "Military Supremacy",
  SCIENCE: "Scientific Supremacy",
  POINTS: "Civilian Victory",
  FORFEIT: "the other player leaving",
};

/** What each score line is called; a line the table does not know is named after its id. */
const LINE: Record<string, string> = {
  blue: "Blue cards",
  green: "Green cards",
  yellow: "Yellow cards",
  purple: "Guilds",
  wonders: "Wonders",
  progress: "Progress tokens",
  coins: "Coins",
  military: "Military track",
};

const labelOf = (line: string): string =>
  LINE[line] ?? line.charAt(0).toUpperCase() + line.slice(1).replaceAll("-", " ");

/** Points per category and player, in the order the server sends the lines (B-END-2). */
function ScoreTable({
  session,
  scores,
  seats,
  winner,
}: {
  session: Session;
  scores: ScoreView[];
  seats: Seats;
  winner: string | null;
}) {
  const columns = [seats.me, seats.opp]
    .map((id) => scores.find((s) => s.player === id))
    .filter((s): s is ScoreView => s !== undefined);
  const first = columns.at(0);
  if (!first) return null;
  return (
    <table className="outcome__scores">
      <thead>
        <tr>
          <th scope="col">Category</th>
          {columns.map((c) => (
            <th key={c.player} scope="col" className={c.player === winner ? "is-winner" : ""}>
              {!seats.spectator && c.player === seats.me ? "You" : seatName(session, c.player)}
            </th>
          ))}
        </tr>
      </thead>
      <tbody>
        {first.lines.map((l) => (
          <tr key={l.line}>
            <th scope="row">{labelOf(l.line)}</th>
            {columns.map((c) => (
              <td key={c.player}>{c.lines.find((x) => x.line === l.line)?.points ?? 0}</td>
            ))}
          </tr>
        ))}
      </tbody>
      <tfoot>
        <tr>
          <th scope="row">Total</th>
          {columns.map((c) => (
            <td key={c.player} className={c.player === winner ? "is-winner" : ""}>
              {c.total}
            </td>
          ))}
        </tr>
      </tfoot>
    </table>
  );
}

/**
 * Who won and how, as a card over the blurred board (B-END-1). A Civilian Victory adds the score
 * per category (B-END-2); the card can be put aside to look at the finished cities.
 */
export function Outcome({
  session,
  view,
  seats,
}: {
  session: Session;
  view: DuelView;
  seats: Seats;
}) {
  const [aside, setAside] = useState(false);
  if (view.phase !== "OVER") return null;
  const winner = view.victoryWinner;
  const headline =
    winner === null
      ? "A draw"
      : !seats.spectator && winner === seats.me
        ? "You win"
        : `${seatName(session, winner)} wins`;
  if (aside) {
    return (
      <button
        type="button"
        className="outcome__reopen"
        onClick={() => {
          setAside(false);
        }}
      >
        {headline} · results
      </button>
    );
  }
  return (
    <div className="outcome__scrim">
      <section className="outcome" role="status" aria-label="Game over">
        <h2>{headline}</h2>
        {view.victoryReason !== null && <p>by {HOW[view.victoryReason]}</p>}
        {view.victoryReason === "POINTS" && (
          <ScoreTable session={session} scores={view.scores} seats={seats} winner={winner} />
        )}
        <button
          type="button"
          onClick={() => {
            setAside(true);
          }}
        >
          Show the board
        </button>
      </section>
    </div>
  );
}
