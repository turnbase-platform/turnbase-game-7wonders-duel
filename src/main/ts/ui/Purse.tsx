import { cuesOf } from "../core/cues.js";
import { seatName, type Session } from "../core/session.js";
import { Icon } from "./icons.js";

/** `+2` / `−2`: what a coins cue floats off the purse it belongs to. */
function signed(amount: number): string {
  return amount < 0 ? `−${String(-amount)}` : `+${String(amount)}`;
}

interface Props {
  session: Session;
  /** The seat whose coins these are. */
  player: string;
  side: "me" | "opp";
  /** Names the seat beside the coin; a city already says whose it is. */
  named?: boolean;
  big?: boolean;
}

/** A seat's coins, and the gains and losses of the last step floating off them. */
export function Purse({ session, player, side, named = false, big = false }: Props) {
  const step = session.history.at(-1);
  const cues = step === undefined ? [] : cuesOf(step.events);
  const coins = session.view?.players.find((p) => p.id === player)?.coins ?? 0;
  return (
    <span
      className={`purse owner-${side}${big ? " purse--big" : ""}`}
      title={`${String(coins)} coins`}
    >
      {named && <small className="who">{seatName(session, player)}</small>}
      <Icon id="coin" /> <b>{coins}</b>
      {cues.map(
        (c) =>
          c.kind === "coins" &&
          c.player === player && (
            <span
              key={`${String(step?.number)}:${String(c.id)}`}
              className={`float ${c.amount < 0 ? "is-loss" : "is-gain"}`}
            >
              {signed(c.amount)}
            </span>
          ),
      )}
    </span>
  );
}
