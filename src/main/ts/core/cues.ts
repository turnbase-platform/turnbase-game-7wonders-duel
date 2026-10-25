import type { ComponentId, EventView, PlaceView, PlayerId } from "./wire.js";

interface Base {
  /** The event this cue explains, and the event that caused it (-1 for a root). */
  id: number;
  parent: number;
}

/** What the screen shows for an event; the view has already changed, the cue explains how. */
export type Cue =
  | (Base & {
      kind: "travel";
      component: ComponentId;
      from: PlaceView | null;
      to: PlaceView | null;
    })
  | (Base & { kind: "flip"; component: ComponentId })
  | (Base & { kind: "coins"; player: PlayerId | null; amount: number })
  | (Base & { kind: "chain"; component: ComponentId })
  | (Base & { kind: "turn"; player: PlayerId | null; granted: boolean });

const isUncover = (e: EventView): boolean =>
  e.from?.zone === "structure.down" && e.to?.zone === "structure.up";

/**
 * Cues in event order. An event this client cannot see a component for has none, nor has the
 * military pawn (drawn with the track) or a trade payment (the coins it took already float).
 */
export function cuesOf(events: EventView[]): Cue[] {
  return events.flatMap((e): Cue[] => {
    const base = { id: e.id, parent: e.parent };
    switch (e.kind) {
      case "moved":
        if (e.component === null) return [];
        return isUncover(e)
          ? [{ ...base, kind: "flip", component: e.component }]
          : [{ ...base, kind: "travel", component: e.component, from: e.from, to: e.to }];
      case "coins":
        return [{ ...base, kind: "coins", player: e.player, amount: e.amount }];
      case "chain":
        return e.component === null ? [] : [{ ...base, kind: "chain", component: e.component }];
      case "playAgainGranted":
        return [{ ...base, kind: "turn", player: e.player, granted: true }];
      case "playAgainCleared":
        return [{ ...base, kind: "turn", player: e.player, granted: false }];
      case "trade":
      case "pawn":
        return [];
    }
  });
}
