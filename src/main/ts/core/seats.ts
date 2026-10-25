import type { Session } from "./session.js";
import type { PlayerId } from "./wire.js";

/** The two seats as the viewer sees them; a spectator sees the first seat as `me`. */
export interface Seats {
  me: PlayerId;
  opp: PlayerId;
  spectator: boolean;
}

export function seatsOf({ view, self }: Session): Seats | null {
  const first = view?.players[0]?.id;
  const second = view?.players[1]?.id;
  if (first === undefined || second === undefined) return null;
  if (self === second) return { me: second, opp: first, spectator: false };
  if (self === first) return { me: first, opp: second, spectator: false };
  return { me: first, opp: second, spectator: true };
}
