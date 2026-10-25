// The Wonders symbol: the pyramid of the Architecture token, two-toned, with its courses of bricks.
// It is a real square pyramid seen from a corner, a little from above, so every edge follows from one
// projection: the courses are the cuts of horizontal planes and stay parallel to the base.
import type { Imported } from "./imported.js";

const LIGHT = "#e9a83a";
const DARK = "#aa7127";
const JOINT = "#7a4f1f";

type Point = readonly [number, number];
type Point3 = readonly [number, number, number];

const YAW = (35 * Math.PI) / 180;
const PITCH = (34 * Math.PI) / 180;
const HEIGHT = 1.7;
const COURSES = [0.3, 0.55, 0.78];

const BASE: readonly Point3[] = [
  [-1, 0, -1],
  [1, 0, -1],
  [1, 0, 1],
  [-1, 0, 1],
];
const TOP: Point3 = [0, HEIGHT, 0];

/** Depth away from the viewer, and the point on the screen (x right, y up). */
const view = ([x, y, z]: Point3): { depth: number; at: Point } => {
  const across = x * Math.cos(YAW) + z * Math.sin(YAW);
  const depth = -x * Math.sin(YAW) + z * Math.cos(YAW);
  return { depth, at: [across, y * Math.cos(PITCH) + depth * Math.sin(PITCH)] };
};

const nearest = BASE.reduce((a, b) => (view(b).depth < view(a).depth ? b : a));
const [left, right] = BASE.filter(
  (corner) => Math.abs(corner[0] - nearest[0]) + Math.abs(corner[2] - nearest[2]) === 2,
).sort((a, b) => view(a).at[0] - view(b).at[0]);

// Fit the drawing to the 100×100 box, y down.
const outline = [TOP, nearest, left, right].map((p) => view(p).at);
const xs = outline.map((p) => p[0]);
const ys = outline.map((p) => p[1]);
const scale = 92 / Math.max(Math.max(...xs) - Math.min(...xs), Math.max(...ys) - Math.min(...ys));
const px = (p: Point3): string => {
  const [x, y] = view(p).at;
  const sx = 50 + (x - (Math.max(...xs) + Math.min(...xs)) / 2) * scale;
  const sy = 50 - (y - (Math.max(...ys) + Math.min(...ys)) / 2) * scale;
  return `${sx.toFixed(1)},${sy.toFixed(1)}`;
};

/** The point a fraction `t` of the way down an edge from the top: a course of bricks is one `t`. */
const down = (corner: Point3, t: number): Point3 => [
  TOP[0] + (corner[0] - TOP[0]) * t,
  TOP[1] + (corner[1] - TOP[1]) * t,
  TOP[2] + (corner[2] - TOP[2]) * t,
];

const face = (a: Point3, b: Point3, fill: string): string =>
  `<polygon fill="${fill}" points="${px(TOP)} ${px(a)} ${px(b)}"/>`;
const course = (a: Point3, b: Point3, t: number): string => {
  const [x1, y1] = px(down(a, t)).split(",");
  const [x2, y2] = px(down(b, t)).split(",");
  return `<line x1="${x1}" y1="${y1}" x2="${x2}" y2="${y2}"/>`;
};
const courses = (a: Point3, b: Point3): string => COURSES.map((t) => course(a, b, t)).join("");

export const WONDER: Imported = {
  viewBox: "0 0 100 100",
  body:
    `<g stroke-linejoin="round" stroke-linecap="round">${face(left, nearest, LIGHT)}${face(nearest, right, DARK)}` +
    `<g stroke="${JOINT}" stroke-width="1.8" fill="none">${courses(left, nearest)}${courses(nearest, right)}</g></g>`,
};
