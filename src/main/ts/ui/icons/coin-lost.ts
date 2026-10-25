// The coin split by a crack and drawn apart: money lost. Built from the imported coin, so the two
// always match; each half is the whole coin clipped to one side of a zig-zag.
import { IMPORTED, type Imported } from "./imported.js";

const CRACK = "66,0 50,30 70,55 48,85 58,119";
const LEFT = `0,0 ${CRACK} 0,119`;
const RIGHT = `${CRACK} 119,119 119,0`;

const coin = IMPORTED.coin?.body ?? "";
const half = (id: string, points: string, shift: string): string =>
  `<clipPath id="${id}"><polygon points="${points}"/></clipPath>` +
  `<g transform="${shift}"><g clip-path="url(#${id})">${coin}</g></g>`;

export const COIN_LOST: Imported = {
  viewBox: "0 0 119 119",
  body:
    `<g transform="translate(59.5 59.5) scale(.88) translate(-59.5 -59.5)">` +
    half("coin-lost-a", LEFT, "translate(-6 3)") +
    half("coin-lost-b", RIGHT, "translate(6 -3)") +
    `</g>`,
};
