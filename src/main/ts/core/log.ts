import type { Catalog } from "./catalog.js";
import type { ComponentId, EventView, PawnView, PlaceView, PlayerId } from "./wire.js";

/** One line of the event log; an effect sits under the event that caused it. */
export interface LogLine {
  id: number;
  text: string;
  children: LogLine[];
}

type Name = (player: PlayerId) => string;

/** Turns one action's events into lines, children folded under the event that caused them. */
export function logLines(events: EventView[], catalog: Catalog, who: Name): LogLine[] {
  const lines = events.map((e) => ({ id: e.id, text: describe(e, catalog, who), children: [] }));
  const byId = new Map<number, LogLine>(lines.map((line) => [line.id, line]));
  const roots: LogLine[] = [];
  for (const [i, e] of events.entries()) {
    const line = lines[i] as LogLine;
    const parent = byId.get(e.parent);
    (parent === undefined ? roots : parent.children).push(line);
  }
  return roots;
}

function describe(e: EventView, catalog: Catalog, who: Name): string {
  const player = e.player === null ? "Someone" : who(e.player);
  const cause = e.cause === null ? "" : ` (${catalog.name(e.cause)})`;
  switch (e.kind) {
    case "moved":
      return `${componentName(e.component, catalog)} moved from ${place(e.from, who)} to ${place(e.to, who)}`;
    case "coins":
      return e.amount >= 0
        ? `${player} gains ${coins(e.amount)}${cause}`
        : `${player} loses ${coins(-e.amount)}${cause}`;
    case "trade":
      return `${player} pays ${coins(e.amount)} to the bank for resources`;
    case "chain":
      return `${player} builds ${componentName(e.component, catalog)} free by chain`;
    case "pawn":
      return pawn(e.pawnTo, who);
    case "playAgainGranted":
      return `${player} plays again${cause}`;
    case "playAgainCleared":
      return `${player} has no extra turn left`;
  }
}

function componentName(id: ComponentId | null, catalog: Catalog): string {
  return id === null ? "A card" : catalog.name(id);
}

function coins(n: number): string {
  return n === 1 ? "1 coin" : `${String(n)} coins`;
}

function pawn(to: PawnView | null, who: Name): string {
  return to?.side == null
    ? "The military pawn returns to the centre"
    : `The military pawn moves to ${who(to.side)}'s side, ${String(to.space)} from the centre`;
}

const ZONES = new Map([
  ["deck", "the deck"],
  ["structure.up", "the pyramid"],
  ["structure.down", "the pyramid"],
  ["discard", "the discard pile"],
  ["draft", "the draft"],
  ["board", "the progress board"],
  ["box", "the box"],
  ["military", "the military track"],
  ["destroyed", "the destroyed pile"],
]);

const OWNED = new Map<string, (owner: string) => string>([
  ["city", (owner) => `${owner}'s city`],
  ["underWonder", (owner) => `under ${owner}'s wonder`],
  ["wonders.unbuilt", (owner) => `${owner}'s unbuilt wonders`],
  ["wonders.built", (owner) => `${owner}'s built wonders`],
]);

function place(at: PlaceView | null, who: Name): string {
  if (at === null) {
    return "nowhere";
  }
  const owned = OWNED.get(at.zone);
  if (owned !== undefined) {
    return owned(who(at.key));
  }
  return ZONES.get(at.zone) ?? at.zone;
}
