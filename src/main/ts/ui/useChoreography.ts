import { useEffect, useLayoutEffect, useRef, useState, type RefObject } from "react";
import { cuesOf } from "../core/cues.js";
import { burnsOf, tweensOf, type Burn, type Rect, type Tween } from "../core/flip.js";
import type { Step } from "../core/session.js";
import { EASE, MOTION_MS, prefersReducedMotion } from "./motion.js";

type Rects = Map<string, Rect>;

/** A component's element on the board; copies in the action dialog or the peek are not it. */
function boardElements(root: HTMLElement): Map<string, HTMLElement> {
  const found = new Map<string, HTMLElement>();
  for (const el of root.querySelectorAll<HTMLElement>("[data-component]")) {
    const id = el.dataset.component;
    if (id !== undefined && !found.has(id) && el.closest('[role="dialog"], .peek') === null) {
      found.set(id, el);
    }
  }
  return found;
}

function measure(elements: Map<string, HTMLElement>): Rects {
  return new Map(
    [...elements].map(([id, el]) => {
      const { left, top, width, height } = el.getBoundingClientRect();
      return [id, { left, top, width, height }];
    }),
  );
}

function keyframes(t: Tween): Keyframe[] {
  switch (t.kind) {
    case "travel":
      return [
        {
          transform: `translate(${String(t.dx)}px, ${String(t.dy)}px) scale(${String(t.sx)}, ${String(t.sy)})`,
          transformOrigin: "0 0",
          zIndex: 20, // a card in flight passes over its neighbours
        },
        { transform: "none", transformOrigin: "0 0", zIndex: 20 },
      ];
    case "appear":
      return [
        { opacity: 0, transform: "scale(.85)" },
        { opacity: 1, transform: "none" },
      ];
    case "flip":
      return [{ transform: "scaleX(.05)" }, { transform: "none" }];
    case "chain":
      return [
        { boxShadow: "0 0 0 4px var(--accent, #e8c35a), 0 0 18px var(--accent, #e8c35a)" },
        { boxShadow: "0 0 0 0 transparent" },
      ];
  }
}

/**
 * Plays the cues of each new step as FLIP transitions: the rectangle every component had after the
 * last draw is the start, where it sits now is the end. The board always shows the latest view, so
 * an animation is never waited for: a new step, a click or a key finishes the running ones at once.
 */
export function useChoreography(
  root: RefObject<HTMLElement | null>,
  step: Step | null,
): { burns: Burn[]; clearBurns: () => void } {
  const [burns, setBurns] = useState<Burn[]>([]);
  const before = useRef<Rects>(new Map());
  const running = useRef<Animation[]>([]);
  // The step already on screen when the board appears was not seen happening, so it is not replayed.
  const played = useRef<number | null>(step?.number ?? null);

  const finishRunning = (): void => {
    const animations = running.current;
    running.current = [];
    for (const a of animations) a.finish();
  };
  /** Empties the ghosts without a render when there are none, so a click is not interrupted by one. */
  const clearBurns = (): void => {
    setBurns((now) => (now.length === 0 ? now : []));
  };
  const interrupt = (): void => {
    finishRunning();
    clearBurns();
  };

  useLayoutEffect(() => {
    const board = root.current;
    if (board === null) return;
    const fresh = step !== null && step.number !== played.current;
    if (!fresh && running.current.length > 0) return;
    finishRunning();
    const elements = boardElements(board);
    const after = measure(elements);
    if (fresh && !prefersReducedMotion()) {
      const cues = cuesOf(step.events);
      setBurns(burnsOf(cues, before.current));
      for (const t of tweensOf(cues, before.current, after)) {
        const animation = elements.get(t.component)?.animate(keyframes(t), {
          duration: MOTION_MS[t.kind],
          easing: EASE,
        });
        if (animation !== undefined) {
          running.current.push(animation);
          animation.onfinish = () => {
            running.current = running.current.filter((a) => a !== animation);
          };
        }
      }
    }
    played.current = step?.number ?? null;
    before.current = after;
  });

  useEffect(() => {
    const board = root.current;
    board?.addEventListener("pointerdown", interrupt);
    board?.addEventListener("keydown", interrupt);
    return () => {
      board?.removeEventListener("pointerdown", interrupt);
      board?.removeEventListener("keydown", interrupt);
    };
  }, [root]);

  return { burns, clearBurns };
}
