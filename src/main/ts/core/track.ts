import type { Catalog } from "./catalog.js";
import type { Seats } from "./seats.js";
import type { DuelView, PlayerId } from "./wire.js";

/** Spaces from the centre to a capital (B-MIL-1); the track has one tile for each, both ways. */
const CAPITAL = 9;
/** The first space of zones 1 to 3 (B-MIL-1); a capital starts zone 4. */
const ZONE_STARTS = [1, 3, 6];
/** Victory points of a pawn standing in zone 1 to 3 at the end (B-MIL-5). */
const ZONE_VP = [2, 5, 10];
/** Zones that hold a military token (B-SET-2): zone 1 holds none. */
const LOOT_ZONES = [3, 2];

export interface Tile {
  /** −9 at the viewer's capital to +9 at the opponent's; the centre is 0. */
  position: number;
  label: string;
  capital: "me" | "opp" | null;
  pawn: boolean;
  /** Shields the pawn needs to get here, shown on hover; null under the pawn. */
  title: string | null;
}

export interface Field {
  kind: "vp" | "loot";
  /** The zone, 1 to 4: how deep into a side's part of the track this field lies. */
  zone: number;
  text: string;
  /** The tiles it sits under, as indexes into `tiles`, both included. */
  from: number;
  to: number;
  /** The pawn stands in this field's zone. */
  current: boolean;
  /** A loot token that is no longer there. */
  taken: boolean;
  title: string | null;
}

export interface Track {
  tiles: Tile[];
  fields: Field[];
}

const signed = (n: number): string => (n < 0 ? `−${String(-n)}` : `+${String(n)}`);
/** The tile index of a position. */
const at = (position: number): number => position + CAPITAL;

function pawnPosition(view: DuelView, seats: Seats): number {
  if (view.pawnSide === null) return 0;
  return view.pawnSide === seats.opp ? view.pawnSpace : -view.pawnSpace;
}

/** Zone of a signed position's distance from the centre: 0 centre, 1 to 3, 4 a capital. */
function zoneOf(position: number): number {
  const space = Math.abs(position);
  return ZONE_STARTS.filter((start) => space >= start).length + (space === CAPITAL ? 1 : 0);
}

function titleOf(position: number, distance: number): string {
  const shields = `${signed(distance)} shields`;
  return Math.abs(position) === CAPITAL
    ? `${shields} · Capital: Military Supremacy for the other player`
    : shields;
}

export function trackOf(view: DuelView, catalog: Catalog, seats: Seats): Track {
  const pawn = pawnPosition(view, seats);
  const tiles: Tile[] = [];
  for (let position = -CAPITAL; position <= CAPITAL; position++) {
    const distance = position - pawn;
    tiles.push({
      position,
      label: String(Math.abs(position)),
      capital: Math.abs(position) === CAPITAL ? (position < 0 ? "me" : "opp") : null,
      pawn: position === pawn,
      title: distance === 0 ? null : titleOf(position, distance),
    });
  }
  return { tiles, fields: fieldsOf(view, catalog, seats, zoneOf(pawn), pawn) };
}

function fieldsOf(
  view: DuelView,
  catalog: Catalog,
  seats: Seats,
  zone: number,
  pawn: number,
): Field[] {
  const here = (side: "me" | "opp", z: number): boolean =>
    pawn !== 0 && zone === z && pawn < 0 === (side === "me");
  const vp = (side: "me" | "opp"): Field => {
    const sign = side === "me" ? -1 : 1;
    return {
      kind: "vp",
      zone: 1,
      text: `${String(ZONE_VP[0])} VP`,
      from: at(sign === 1 ? 1 : -2),
      to: at(sign === 1 ? 2 : -1),
      current: here(side, 1),
      taken: false,
      title: null,
    };
  };
  const loot = (side: "me" | "opp", z: number): Field => {
    const sign = side === "me" ? -1 : 1;
    const start = ZONE_STARTS[z - 1] ?? 0;
    const last = start + 2;
    const player: PlayerId = seats[side];
    const coins = lootCoins(view, catalog, player, z);
    const there = view.visible.some(
      (p) => p.zone === "military" && p.key === `${player}:${String(z)}`,
    );
    return {
      kind: "loot",
      zone: z,
      text: String(coins),
      from: at(sign === 1 ? start : -last),
      to: at(sign === 1 ? last : -start),
      current: here(side, z),
      taken: !there,
      title: `Zone worth ${String(ZONE_VP[z - 1] ?? 0)} VP; loot ${String(coins)} coins`,
    };
  };
  return [
    ...LOOT_ZONES.map((z) => loot("me", z)),
    vp("me"),
    vp("opp"),
    ...[...LOOT_ZONES].reverse().map((z) => loot("opp", z)),
  ];
}

/** What the token of a zone on a player's side costs the player: from the catalog, so a taken token is still known. */
function lootCoins(view: DuelView, catalog: Catalog, player: PlayerId, zone: number): number {
  const seat = view.players.findIndex((p) => p.id === player);
  const token = catalog
    .all()
    .find(
      (e) =>
        e.type === "TOKEN" &&
        e.tags.includes(`military-zone:${String(zone)}`) &&
        e.tags.includes(`military-seat:${String(seat)}`),
    );
  return token?.badges.find((b) => b.kind === "loseCoins")?.amount ?? 0;
}
