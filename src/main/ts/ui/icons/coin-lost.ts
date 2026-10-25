// The coin with a strip cut out along a zig-zag crack, its round shape kept: money lost. Built from
// the imported coin, so the two always match; the coin is clipped to the two sides of the crack,
// each held back from it.
import { IMPORTED, type Imported } from "./imported.js";

const GAP = 5;
const SIZE = 119;
const CRACK: readonly (readonly [number, number])[] = [
  [60, 0],
  [53, 34],
  [65, 60],
  [56, 90],
  [59.5, SIZE],
];

/** The coin's side of the crack that reaches `corner`, held back `back` units from the crack. */
const side = (back: number, corner: number): string =>
  [...CRACK.map(([x, y]) => [x + back, y]), [corner, SIZE], [corner, 0]]
    .map(([x, y]) => `${String(x)},${String(y)}`)
    .join(" ");

const coin = IMPORTED.coin?.body ?? "";

export const COIN_LOST: Imported = {
  viewBox: `0 0 ${String(SIZE)} ${String(SIZE)}`,
  body:
    `<clipPath id="coin-lost-cut"><polygon points="${side(-GAP, 0)}"/>` +
    `<polygon points="${side(GAP, SIZE)}"/></clipPath>` +
    `<g clip-path="url(#coin-lost-cut)">${coin}</g>`,
};
