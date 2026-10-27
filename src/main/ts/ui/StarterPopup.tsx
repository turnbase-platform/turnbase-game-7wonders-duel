import type { Seats } from "../core/seats.js";
import { seatName, type Session } from "../core/session.js";
import type { Round } from "../core/starter.js";
import { ROMAN } from "./Status.js";

/** Who starts the Wonder draft or the Age that has just begun (B-WND-1, B-AGE-0); it leaves by itself. */
export function StarterPopup({
  round,
  session,
  seats,
}: {
  round: Round;
  session: Session;
  seats: Seats | null;
}) {
  const mine = seats !== null && !seats.spectator && round.starter === seats.me;
  const what =
    round.kind === "draft" ? "Wonder draft" : `Age ${ROMAN[round.age] ?? String(round.age)}`;
  return (
    <div className="starter" role="status" aria-label={`${what}: who starts`}>
      <div className="starter__round">{what}</div>
      <div className="starter__who">
        {mine ? "You start" : `${seatName(session, round.starter)} starts`}
      </div>
    </div>
  );
}
