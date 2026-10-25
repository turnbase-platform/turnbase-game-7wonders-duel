import type { InitContext, Player, Role } from "@turnbase/gdk";
import { makeCatalog, type Catalog } from "./catalog.js";
import type { DuelView, EventView, PlayerId, ServerMessage } from "./wire.js";

/** How many past actions' events the session keeps. */
export const HISTORY_LIMIT = 50;

/** The events one accepted action brought, numbered in the order this client received them. */
export interface Step {
  number: number;
  events: EventView[];
}

/** Everything the UI draws, as a value. The view is the truth; the history only explains it. */
export interface Session {
  role: Role | null;
  self: PlayerId | null;
  roster: Player[];
  view: DuelView | null;
  catalog: Catalog | null;
  history: Step[];
  /** Why the last action was refused, until the next view arrives. */
  rejection: string | null;
  left: Player[];
  over: boolean;
  /** How many steps were ever filed, so numbers keep rising when old steps are dropped. */
  filed: number;
}

export type SessionInput =
  | { type: "init"; context: InitContext }
  | { type: "message"; message: ServerMessage }
  | { type: "playerLeft"; player: Player }
  | { type: "gameOver"; results: unknown };

export function emptySession(): Session {
  return {
    role: null,
    self: null,
    roster: [],
    view: null,
    catalog: null,
    history: [],
    rejection: null,
    left: [],
    over: false,
    filed: 0,
  };
}

export function reduce(session: Session, input: SessionInput): Session {
  switch (input.type) {
    case "init":
      return {
        ...emptySession(),
        role: input.context.role,
        self: input.context.self?.id ?? null,
        roster: input.context.roster,
      };
    case "message":
      return received(session, input.message);
    case "playerLeft":
      return { ...session, left: [...session.left, input.player] };
    case "gameOver":
      return { ...session, over: true };
  }
}

function received(session: Session, message: ServerMessage): Session {
  if (message.kind === "rejected" || message.view === null) {
    return { ...session, rejection: message.reason ?? "The action was refused." };
  }
  if (message.catalog !== null) {
    // A resync describes the whole game again: nothing earlier is worth keeping.
    return {
      ...session,
      view: message.view,
      catalog: makeCatalog(message.catalog),
      history: [],
      rejection: null,
    };
  }
  if (message.events.length === 0) {
    return { ...session, view: message.view, rejection: null };
  }
  const filed = session.filed + 1;
  const history = [...session.history, { number: filed, events: message.events }];
  return {
    ...session,
    view: message.view,
    history: history.slice(-HISTORY_LIMIT),
    rejection: null,
    filed,
  };
}

/** A seat's display name from the roster, or the start of its id when it is not listed. */
export function seatName(session: Session, id: PlayerId): string {
  return session.roster.find((p) => p.id === id)?.displayName ?? id.slice(0, 8);
}
