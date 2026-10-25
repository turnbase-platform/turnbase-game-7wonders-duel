// The pure half of the dev harness host page: what it says to a game iframe, and how it reads
// the dev server's stream. Kept apart from the DOM so it is tested like any other module.
import type { InitContext, Player } from "@turnbase/gdk";

export const VIEWERS = ["1", "2", "spectator"] as const;
export type Viewer = (typeof VIEWERS)[number];

/** What the platform shell sends first: `init`, with no game state (frontend-gdk-api.md §4). */
export type Init = { type: "init" } & InitContext;

export type Inbound =
  { type: "game_event"; payload: unknown } | { type: "game_over"; results: unknown };

// Same ids and names the backend's DevGame seats use (GdkDoubles.ONE / TWO).
const PLAYERS: Player[] = [
  { id: "00000000-0000-0000-0000-000000000001", displayName: "One", teamId: "team-1" },
  { id: "00000000-0000-0000-0000-000000000002", displayName: "Two", teamId: "team-2" },
];

export function initFor(viewer: Viewer): Init {
  const [one, two] = PLAYERS;
  const self = viewer === "1" ? one : viewer === "2" ? two : null;
  return {
    type: "init",
    role: self ? "player" : "spectator",
    self: self ?? null,
    roster: PLAYERS,
    teams: PLAYERS.map((p) => ({ id: p.teamId, name: p.displayName, players: [p] })),
    sessionId: "dev",
  };
}

/** One server-sent delivery: a message for the iframe, or `reset` (the position was replaced). */
export function parseStreamEvent(data: string): Inbound | "reset" {
  const event = JSON.parse(data) as { channel: string; payload?: unknown };
  switch (event.channel) {
    case "game_event":
      return { type: "game_event", payload: event.payload };
    case "game_over":
      return { type: "game_over", results: event.payload };
    case "reset":
      return "reset";
    default:
      throw new Error(`unknown channel ${event.channel}`);
  }
}

export function streamTarget(kind: "action" | "resync" | "stream", viewer: Viewer): string {
  return `/api/${kind}/${viewer}`;
}
