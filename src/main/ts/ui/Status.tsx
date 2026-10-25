import { cuesOf } from "../core/cues.js";
import type { Seats } from "../core/seats.js";
import { seatName, type Session } from "../core/session.js";

/** "Age I" in small caps: the Age's number as a Roman numeral, which the phases past the Ages lack. */
const ROMAN = ["", "I", "II", "III"];

/** Which Age or phase the game is in, and whose turn it is. */
export function Status({ session, seats }: { session: Session; seats: Seats }) {
  const view = session.view;
  if (view === null) return null;
  const step = session.history.at(-1);
  const replays =
    step !== undefined && cuesOf(step.events).some((c) => c.kind === "turn" && c.granted);
  const mine = !seats.spectator && view.active === seats.me;
  return (
    <span>
      <b className="phase">
        {view.phase === "AGE" ? `Age ${ROMAN[view.age] ?? String(view.age)}` : view.phase}
      </b>
      {" · "}
      <span
        // A new key restarts the pulse for every action that grants a replay.
        key={replays ? `pulse-${String(step.number)}` : "turn"}
        className={`turn${replays ? " is-pulse" : ""}`}
      >
        {view.phase === "OVER"
          ? "Game over"
          : mine
            ? "your turn"
            : `${seatName(session, view.active)} to act`}
      </span>
    </span>
  );
}
