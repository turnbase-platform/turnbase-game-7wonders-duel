/** How long each cue plays, in milliseconds. One move stays under about 1.2 s (design §9). */
export const MOTION_MS = {
  travel: 400,
  appear: 250,
  flip: 300,
  chain: 600,
  coins: 500,
  pulse: 600,
} as const;

export const EASE = "cubic-bezier(.2, .7, .2, 1)";

/** True when the viewer asked for no motion; every cue then becomes an instant state change. */
export function prefersReducedMotion(): boolean {
  return (
    typeof window.matchMedia === "function" &&
    window.matchMedia("(prefers-reduced-motion: reduce)").matches
  );
}
