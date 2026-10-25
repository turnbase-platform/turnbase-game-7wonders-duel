// The Wonders symbol: the pyramid of the Architecture token, two-toned, with its courses of bricks.
import type { Imported } from "./imported.js";

const LIGHT = "#e9a83a";
const DARK = "#aa7127";
const JOINT = "#7a4f1f";

const APEX = [50, 8] as const;
const LEFT = [6, 80] as const;
const FOOT = [58, 94] as const;
const RIGHT = [96, 66] as const;

type Point = readonly [number, number];
const along = (from: Point, to: Point, t: number): Point => [
  from[0] + (to[0] - from[0]) * t,
  from[1] + (to[1] - from[1]) * t,
];
const face = (a: Point, b: Point, fill: string): string =>
  `<polygon fill="${fill}" points="${APEX.join(",")} ${a.join(",")} ${b.join(",")}"/>`;
/** Courses of brick: lines across a face from its outer edge to the ridge. */
const courses = (edge: Point): string =>
  [0.33, 0.58, 0.8]
    .map((t) => {
      const [x1, y1] = along(APEX, edge, t);
      const [x2, y2] = along(APEX, FOOT, t);
      return `<line x1="${String(x1)}" y1="${String(y1)}" x2="${String(x2)}" y2="${String(y2)}"/>`;
    })
    .join("");

export const WONDER: Imported = {
  viewBox: "0 0 100 100",
  body:
    `<g stroke-linejoin="round" stroke-linecap="round">${face(LEFT, FOOT, LIGHT)}${face(FOOT, RIGHT, DARK)}` +
    `<g stroke="${JOINT}" stroke-width="1.8" fill="none">${courses(LEFT)}${courses(RIGHT)}</g></g>`,
};
