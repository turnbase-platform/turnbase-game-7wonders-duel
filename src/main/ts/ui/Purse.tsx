import { cuesOf } from "../core/cues.js";
import { seatName, type Session } from "../core/session.js";
import { Icon } from "./icons.js";
import { MOTION_MS } from "./motion.js";

/** The most small coins a cue spawns; more would only be a blur. */
const MAX_COINS = 12;
/** How long one small coin travels, in milliseconds; the rest of the cue is the stagger between coins. */
const TRAVEL_MS = 500;
/** An angle that never lines two coins up, however many there are. */
const GOLDEN_ANGLE = 2.399963;

/** `+2 coins` / `−2 coins`: what a coins cue says to a screen reader. */
function described(amount: number): string {
  return `${amount < 0 ? "−" : "+"}${String(Math.abs(amount))} coins`;
}

/**
 * Small coins as many as were gained, flying into the purse; or as many as were lost, flying out of
 * it. They start one after the other so the whole cue stays within MOTION_MS.coins.
 */
function CoinBurst({ amount }: { amount: number }) {
  const count = Math.min(Math.abs(amount), MAX_COINS);
  const stagger = count > 1 ? (MOTION_MS.coins - TRAVEL_MS) / (count - 1) : 0;
  return (
    <span
      className={`coinfx ${amount < 0 ? "is-loss" : "is-gain"}`}
      role="img"
      aria-label={described(amount)}
    >
      {Array.from({ length: count }, (_, i) => (
        <i
          key={i}
          className="coinfx__coin"
          style={{
            ["--x" as string]: Math.cos(i * GOLDEN_ANGLE).toFixed(3),
            ["--y" as string]: Math.sin(i * GOLDEN_ANGLE).toFixed(3),
            ["--delay" as string]: `${String(Math.round(i * stagger))}ms`,
            ["--travel" as string]: `${String(TRAVEL_MS)}ms`,
          }}
        >
          <Icon id="coin" />
        </i>
      ))}
    </span>
  );
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

/** A seat's coins, and the gains and losses of the last step flying into and out of them. */
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
          c.player === player &&
          c.amount !== 0 && (
            <CoinBurst key={`${String(step?.number)}:${String(c.id)}`} amount={c.amount} />
          ),
      )}
    </span>
  );
}
