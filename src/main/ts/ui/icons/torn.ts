// Icons for what a Wonder does to cards and tokens, drawn here rather than imported: a card torn
// along a crack, a card whose price is struck out above a pile, and a stack of progress tokens.
import type { Imported } from "./imported.js";

type Point = readonly [number, number];

const RED = "#e53935";
const EDGE = "#0009";

const polygon = (points: readonly Point[]): string =>
  `<polygon points="${points.map(([x, y]) => `${String(x)},${String(y)}`).join(" ")}"/>`;

/** A card of `currentColor` torn in two along a diagonal zig-zag that opens toward the right. */
const TORN_CARD: Imported = (() => {
  const crack: readonly (readonly [number, number, number])[] = [
    [3, 8, 0.6],
    [8, 11.5, 1],
    [12, 10, 1.3],
    [16, 14, 1.6],
    [21, 13.5, 2],
  ];
  const edge = (way: number): Point[] => crack.map(([x, y, gap]) => [x, y + way * gap]);
  return {
    viewBox: "0 0 24 24",
    body:
      `<clipPath id="torn-card-cut">${polygon([...edge(-1), [21, 0], [3, 0]])}` +
      `${polygon([...edge(1), [21, 24], [3, 24]])}</clipPath>` +
      `<rect x="4.3" y="2.3" width="15.4" height="19.4" rx="2" fill="currentColor" stroke="${EDGE}" stroke-width=".5" clip-path="url(#torn-card-cut)"/>`,
  };
})();

const card = (x: number, y: number, fill: string, turn = 0): string =>
  `<rect x="${String(x)}" y="${String(y)}" width="12" height="15" rx="1.6" fill="${fill}" stroke="${EDGE}" stroke-width=".5" transform="rotate(${String(turn)} ${String(x + 6)} ${String(y + 7.5)})"/>`;

/** Grey cards thrown into a heap; on the front one the price bar is crossed out: build it for free. */
const REBUILD: Imported = {
  viewBox: "0 0 24 24",
  body:
    card(0.5, 1.5, "#8b8e94", -16) +
    card(5, 0.5, "#9a9da3", 11) +
    card(1.5, 5.5, "#a9acb2", -5) +
    card(8.5, 8, "#c9ccd1", 4) +
    `<path d="M8.5 12h8.2a1.6 1.6 0 0 1 0 3.2H8.5z" fill="#6f6a60" stroke="${EDGE}" stroke-width=".4" transform="rotate(4 14.5 15.5)"/>` +
    `<path d="M10 10.7l5.8 5.8M15.8 10.7l-5.8 5.8" stroke="#000" stroke-width="3" stroke-linecap="round"/>` +
    `<path d="M10 10.7l5.8 5.8M15.8 10.7l-5.8 5.8" stroke="${RED}" stroke-width="1.7" stroke-linecap="round"/>`,
};

const token = (cx: number, cy: number, face: boolean): string => {
  const disc = `<circle cx="${String(cx)}" cy="${String(cy)}" r="7" fill="#446c3c" stroke="#1e3f1c" stroke-width="1.4"/>`;
  const ring = `<circle cx="${String(cx)}" cy="${String(cy)}" r="4.8" fill="none" stroke="#7fa874" stroke-width=".7"/>`;
  const emblem = `<path transform="translate(${String(cx - 3)} ${String(cy - 3)}) scale(.25)" d="M12 2l8.7 5v10L12 22l-8.7-5V7z" fill="#e8dcae" stroke="none"/>`;
  return disc + ring + (face ? emblem : "");
};

/** Three progress tokens stacked diagonally: two face down under one face up. */
const DRAW_TOKEN: Imported = {
  viewBox: "0 0 24 24",
  body: token(7.5, 7.5, false) + token(12, 12, false) + token(16.5, 16.5, true),
};

export const TORN: Readonly<Record<string, Imported>> = {
  "torn-card": TORN_CARD,
  rebuild: REBUILD,
  "draw-token": DRAW_TOKEN,
};
