import type { Seats } from "../core/seats.js";
import { seatName, type Session } from "../core/session.js";
import type { DuelView, VictoryReason } from "../core/wire.js";

const HOW: Record<VictoryReason, string> = {
  MILITARY: "Military Supremacy",
  SCIENCE: "Scientific Supremacy",
  POINTS: "Victory Points",
  FORFEIT: "the other player leaving",
};

/** Who won and how, over the finished board; the cities stay in view for the count (B-END-1). */
export function Outcome({
  session,
  view,
  seats,
}: {
  session: Session;
  view: DuelView;
  seats: Seats;
}) {
  if (view.phase !== "OVER") return null;
  const winner = view.victoryWinner;
  const headline =
    winner === null
      ? "A draw"
      : !seats.spectator && winner === seats.me
        ? "You win"
        : `${seatName(session, winner)} wins`;
  return (
    <section className="outcome" role="status" aria-label="Game over">
      <h2>{headline}</h2>
      {view.victoryReason !== null && <p>by {HOW[view.victoryReason]}</p>}
    </section>
  );
}
