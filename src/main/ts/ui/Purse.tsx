import { useEffect, useLayoutEffect, useState } from "react";
import { createPortal } from "react-dom";
import { cuesOf } from "../core/cues.js";
import { seatName, type Session } from "../core/session.js";
import { Icon } from "./icons.js";
import { MOTION_MS, prefersReducedMotion } from "./motion.js";

/** The most small coins a cue spawns; more would only be a blur. */
const MAX_COINS = 12;
/** How long one small coin travels, in milliseconds; the rest of the cue is the stagger between coins. */
const TRAVEL_MS = 500;
/** The longest wait between two small coins. */
const STAGGER_MS = 70;
/** One shake of the counter coin, left and back. */
const SHAKE_MS = 110;
/** An angle that never lines two coins up, however many there are. */
const GOLDEN_ANGLE = 2.399963;

/** `+2 coins` / `−2 coins`: what a coins cue says to a screen reader. */
function described(amount: number): string {
  return `${amount < 0 ? "−" : "+"}${String(Math.abs(amount))} coins`;
}

const SHAKE: Keyframe[] = [
  { transform: "translateX(-2px) rotate(-7deg)" },
  { transform: "translateX(2px) rotate(7deg)" },
];

/**
 * Small coins as many as were gained, rising from below into the counter coin; or as many as were
 * lost, sinking out of it. They are drawn over the whole page, since a column would clip them, and
 * the counter coin shakes while they arrive or leave. The whole cue stays within MOTION_MS.coins.
 */
function CoinBurst({ amount, anchor }: { amount: number; anchor: HTMLElement | null }) {
  const [at, setAt] = useState<{ x: number; y: number; size: number } | null>(null);
  const count = Math.min(Math.abs(amount), MAX_COINS);
  const stagger = staggerOf(count);
  const gain = amount > 0;
  useLayoutEffect(() => {
    const coin = anchor?.querySelector(".icon");
    if (!coin || prefersReducedMotion()) return;
    const box = coin.getBoundingClientRect();
    setAt({ x: box.left + box.width / 2, y: box.top + box.height / 2, size: box.width });
    // A gain shakes the coin as the first small coin lands, a loss as the first one leaves.
    const span = (count - 1) * stagger + TRAVEL_MS;
    const delay = gain ? TRAVEL_MS * 0.7 : 0;
    // jsdom has no Web Animations.
    (coin as Partial<Element>).animate?.(SHAKE, {
      duration: SHAKE_MS,
      delay,
      iterations: Math.max(2, Math.ceil((span - delay) / SHAKE_MS)),
      direction: "alternate",
      easing: "ease-in-out",
    });
  }, [anchor, count, stagger, gain]);
  return createPortal(
    <span
      className={`coinfx ${gain ? "is-gain" : "is-loss"}`}
      role="img"
      aria-label={described(amount)}
      style={
        at
          ? {
              left: at.x,
              top: at.y,
              fontSize: Math.max(10, at.size * 0.42),
              ["--r" as string]: `${String(Math.max(36, at.size * 1.3))}px`,
            }
          : undefined
      }
    >
      {at &&
        Array.from({ length: count }, (_, i) => (
          <i
            key={i}
            className="coinfx__coin"
            style={{
              // They keep to a narrow column below the coin.
              ["--x" as string]: (Math.cos(i * GOLDEN_ANGLE) * 0.6).toFixed(3),
              ["--y" as string]: (1 + Math.sin(i * GOLDEN_ANGLE) * 0.25).toFixed(3),
              ["--delay" as string]: `${String(Math.round(i * stagger))}ms`,
              ["--travel" as string]: `${String(TRAVEL_MS)}ms`,
            }}
          >
            <Icon id="coin" />
          </i>
        ))}
    </span>,
    document.body,
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

/** The coins a seat shows after `landed` of the `count` small coins of a cue of `amount` have moved. */
function shownAfter(coins: number, amount: number, count: number, landed: number): number {
  return coins - amount + Math.round((amount * landed) / count);
}

/** The pace of a cue: how far apart its small coins start. */
function staggerOf(count: number): number {
  return count > 1 ? Math.min(STAGGER_MS, (MOTION_MS.coins - TRAVEL_MS) / (count - 1)) : 0;
}

/** A seat's coins, and the gains and losses of the last step flying into and out of them. */
export function Purse({ session, player, side, named = false, big = false }: Props) {
  const step = session.history.at(-1);
  const [purse, setPurse] = useState<HTMLElement | null>(null);
  const [ticked, setTicked] = useState<{ key: string; shown: number } | null>(null);
  const coins = session.view?.players.find((p) => p.id === player)?.coins ?? 0;
  const amount = (step === undefined ? [] : cuesOf(step.events)).reduce(
    (sum, c) => (c.kind === "coins" && c.player === player ? sum + c.amount : sum),
    0,
  );
  const key =
    amount === 0 || step === undefined ? null : `${String(step.number)}:${String(amount)}`;
  const count = Math.min(Math.abs(amount), MAX_COINS);
  // The number counts with the small coins: up as each one lands, down as each one leaves.
  useEffect(() => {
    if (key === null || prefersReducedMotion()) return;
    const stagger = staggerOf(count);
    const timers = Array.from({ length: count }, (_, i) =>
      window.setTimeout(
        () => {
          setTicked({ key, shown: shownAfter(coins, amount, count, i + 1) });
        },
        Math.round(i * stagger) + (amount > 0 ? TRAVEL_MS : 0),
      ),
    );
    return () => {
      timers.forEach((t) => {
        window.clearTimeout(t);
      });
    };
  }, [key, coins, amount, count]);
  const shown =
    key === null || prefersReducedMotion()
      ? coins
      : ticked?.key === key
        ? ticked.shown
        : shownAfter(coins, amount, count, 0);
  return (
    <span
      ref={setPurse}
      className={`purse owner-${side}${big ? " purse--big" : ""}`}
      title={`${String(coins)} coins`}
    >
      {named && <small className="who">{seatName(session, player)}</small>}
      <Icon id="coin" /> <b>{shown}</b>
      {key !== null && <CoinBurst key={key} amount={amount} anchor={purse} />}
    </span>
  );
}
