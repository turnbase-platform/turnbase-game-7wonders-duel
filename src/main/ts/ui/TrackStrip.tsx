import type { DuelView } from "../core/wire.js";

/** A framed place for the military track and the board's progress tokens (plan 15 fills it). */
export function TrackStrip({ view }: { view: DuelView }) {
  return (
    <div className="strip" aria-label="Military track">
      Military · pawn {view.pawnSide === null ? "in the centre" : `at ${String(view.pawnSpace)}`}
    </div>
  );
}
