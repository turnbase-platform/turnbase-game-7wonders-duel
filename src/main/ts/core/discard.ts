import type { ComponentId, DuelView } from "./wire.js";

/** The cards in the discard pile, in the order they were thrown: the pile is public. */
export function discardedOf(view: DuelView): ComponentId[] {
  return view.visible.filter((p) => p.zone === "discard").map((p) => p.component);
}
