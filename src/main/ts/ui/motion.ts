/** How long each cue plays, in milliseconds. One move stays under about 1.2 s (design §9). */
export const MOTION_MS = {
  travel: 400,
  appear: 250,
  flip: 300,
  chain: 600,
  /** A whole coin cue: the small coins start one after the other and the last one lands within this. */
  coins: 1000,
  pulse: 600,
  burn: 550,
  /** The pop-up naming who starts a round; it also covers the time the player needs to read it. */
  starter: 2400,
} as const;

export const EASE = "cubic-bezier(.2, .7, .2, 1)";

/** True when the viewer asked for no motion; every cue then becomes an instant state change. */
export function prefersReducedMotion(): boolean {
  return (
    typeof window.matchMedia === "function" &&
    window.matchMedia("(prefers-reduced-motion: reduce)").matches
  );
}
