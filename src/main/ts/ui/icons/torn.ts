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
  const shape = `<rect x="4.3" y="2.3" width="15.4" height="19.4" rx="2"`;
  const tear = (way: number): string =>
    `<polyline points="${edge(way)
      .map(([x, y]) => `${String(x)},${String(y)}`)
      .join(" ")}"/>`;
  return {
    viewBox: "0 0 24 24",
    body:
      `<clipPath id="torn-card-cut">${polygon([...edge(-1), [21, 0], [3, 0]])}` +
      `${polygon([...edge(1), [21, 24], [3, 24]])}</clipPath>` +
      `<clipPath id="torn-card-shape">${shape}/></clipPath>` +
      `${shape} fill="currentColor" stroke="${EDGE}" stroke-width=".5" clip-path="url(#torn-card-cut)"/>` +
      `<g fill="none" stroke="${EDGE}" stroke-width=".5" stroke-linejoin="round" clip-path="url(#torn-card-shape)">${tear(-1)}${tear(1)}</g>`,
  };
})();

const card = (cx: number, cy: number, fill: string, turn = 0, w = 11, h = 14): string =>
  `<rect x="${String(cx - w / 2)}" y="${String(cy - h / 2)}" width="${String(w)}" height="${String(h)}" rx="1.6" fill="${fill}" stroke="${EDGE}" stroke-width=".5" transform="rotate(${String(turn)} ${String(cx)} ${String(cy)})"/>`;

/** Grey cards thrown into a heap, filling the icon. */
const DISCARD_PILE =
  card(9.5, 10.5, "#8b8e94", -16) + card(14.5, 9.5, "#9a9da3", 11) + card(11.5, 14, "#a9acb2", -5);

/** A card, the size of the `card` icon, whose price bar is crossed out: it costs nothing. */
const FREE_CARD =
  card(12, 12, "#c9ccd1", 0, 15.4, 19.4) +
  `<path d="M4.3 9.5h12a1.9 1.9 0 0 1 0 3.8h-12z" fill="#6f6a60" stroke="${EDGE}" stroke-width=".4"/>` +
  `<path d="M7.5 8.4l6 6M13.5 8.4l-6 6" stroke="#000" stroke-width="3.2" stroke-linecap="round"/>` +
  `<path d="M7.5 8.4l6 6M13.5 8.4l-6 6" stroke="${RED}" stroke-width="1.8" stroke-linecap="round"/>`;

const nested = (icon: string, x: number, y: number, size: number): string =>
  `<svg x="${String(x)}" y="${String(y)}" width="${String(size)}" height="${String(size)}" viewBox="0 0 24 24">${icon}</svg>`;

/** A progress token of radius `r`; face up it shows its emblem, face down only the ring. */
const token = (cx: number, cy: number, face: boolean, r = 7): string => {
  const at = `cx="${String(cx)}" cy="${String(cy)}"`;
  return (
    `<circle ${at} r="${String(r)}" fill="#446c3c" stroke="#1e3f1c" stroke-width="${String(r / 5)}"/>` +
    `<circle ${at} r="${String(r * 0.69)}" fill="none" stroke="#7fa874" stroke-width="${String(r / 10)}"/>` +
    (face ? `<circle ${at} r="${String(r * 0.52)}" fill="#e8dcae"/>` : "")
  );
};

/** Three progress tokens stacked diagonally: two face down under one face up. */
const DRAW_TOKEN: Imported = {
  viewBox: "0 0 24 24",
  body: token(7.5, 7.5, false) + token(12, 12, false) + token(16.5, 16.5, true),
};

export const TORN: Readonly<Record<string, Imported>> = {
  "torn-card": TORN_CARD,
  "discard-pile": { viewBox: "0 0 24 24", body: DISCARD_PILE },
  "free-card": { viewBox: "0 0 24 24", body: FREE_CARD },
  rebuild: {
    viewBox: "0 0 24 24",
    body: nested(DISCARD_PILE, 0, 0, 17) + nested(FREE_CARD, 6, 5, 18),
  },
  progress: { viewBox: "0 0 24 24", body: token(12, 12, true, 10) },
  "draw-token": DRAW_TOKEN,
};
