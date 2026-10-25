import type { Offer } from "./moves.js";
import type { ComponentId } from "./wire.js";

/** The card the viewer has picked out on the structure, if any. */
export type Selection = ComponentId | null;

export type SelectionAction = { type: "toggle"; component: ComponentId } | { type: "clear" };

export function selectionReducer(selection: Selection, action: SelectionAction): Selection {
  if (action.type === "clear") return null;
  return selection === action.component ? null : action.component;
}

/** The selection, unless the view has moved on and no longer offers anything for it. */
export function selectedOf(selection: Selection, offers: Offer[]): Selection {
  return selection !== null && offers.some((o) => o.target === selection) ? selection : null;
}

/** The pane of a small screen the viewer must act on. Every offer today is on the structure. */
export function attention(offers: Offer[]): "structure" | null {
  return offers.length > 0 ? "structure" : null;
}
