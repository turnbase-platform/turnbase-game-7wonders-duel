// The coin broken along a crack and opened like a V, each half turned away about the crack's foot: money lost. Built from the imported coin, so the two
// always match; each half is the whole coin clipped to one side of a zig-zag.
import { IMPORTED, type Imported } from "./imported.js";

const FOOT = "59.5 119";
const CRACK = "60,0 53,34 65,60 56,90 59.5,119";
const LEFT = `0,0 ${CRACK} 0,119`;
const RIGHT = `${CRACK} 119,119 119,0`;

const coin = IMPORTED.coin?.body ?? "";
const half = (id: string, points: string, shift: string): string =>
  `<clipPath id="${id}"><polygon points="${points}"/></clipPath>` +
  `<g transform="${shift}"><g clip-path="url(#${id})">${coin}</g></g>`;

export const COIN_LOST: Imported = {
  viewBox: "0 0 119 119",
  body:
    `<g transform="translate(59.5 59.5) scale(.92) translate(-59.5 -59.5)">` +
    half("coin-lost-a", LEFT, `translate(-4 0) rotate(-5 ${FOOT})`) +
    half("coin-lost-b", RIGHT, `translate(4 0) rotate(5 ${FOOT})`) +
    `</g>`,
};
