import type { Cue } from "./cues.js";
import type { ComponentId } from "./wire.js";

/** A rectangle on screen, as `getBoundingClientRect` reports it. */
export interface Rect {
  left: number;
  top: number;
  width: number;
  height: number;
}

/** How one component's element should play: from this offset and scale to where it now sits. */
export interface Tween {
  kind: "travel" | "appear" | "flip" | "chain";
  component: ComponentId;
  dx: number;
  dy: number;
  sx: number;
  sy: number;
}

const IN_PLACE = { dx: 0, dy: 0, sx: 1, sy: 1 };

/**
 * First, last, invert: the offset of the old rectangle from the new one. A component with no new
 * rectangle has left the screen and has nothing to animate; one with no old rectangle was not
 * drawn before and appears where it is.
 */
export function tweensOf(
  cues: Cue[],
  before: ReadonlyMap<ComponentId, Rect>,
  after: ReadonlyMap<ComponentId, Rect>,
): Tween[] {
  return cues.flatMap((cue): Tween[] => {
    switch (cue.kind) {
      case "travel": {
        const last = after.get(cue.component);
        if (last === undefined) return [];
        const first = before.get(cue.component);
        if (first === undefined) {
          return [{ kind: "appear", component: cue.component, ...IN_PLACE }];
        }
        const tween = {
          kind: "travel" as const,
          component: cue.component,
          dx: first.left - last.left,
          dy: first.top - last.top,
          // One scale for both axes: a card flying into a banner keeps its shape, it is not stretched.
          sx: first.width / last.width,
          sy: first.width / last.width,
        };
        const stays = tween.dx === 0 && tween.dy === 0 && tween.sx === 1 && tween.sy === 1;
        return stays ? [] : [tween];
      }
      case "flip":
      case "chain":
        return after.has(cue.component)
          ? [{ kind: cue.kind, component: cue.component, ...IN_PLACE }]
          : [];
      default:
        return [];
    }
  });
}

/** A card that has left the board and is shown once more, where it stood, while it burns. */
export interface Burn {
  component: ComponentId;
  rect: Rect;
}

/** The burning cards: each had a place on screen before, and has none now. */
export function burnsOf(cues: Cue[], before: ReadonlyMap<ComponentId, Rect>): Burn[] {
  return cues.flatMap((cue): Burn[] => {
    if (cue.kind !== "burn") return [];
    const rect = before.get(cue.component);
    return rect === undefined ? [] : [{ component: cue.component, rect }];
  });
}
