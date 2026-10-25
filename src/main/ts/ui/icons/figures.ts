import type { Imported } from "./imported.js";

/** A number the artwork drew as an outline, to be set in the board's own figures instead. */
interface Figure {
  /** The start of each outline's path data: the figure is drawn as one or two of them. */
  outlines: string[];
  text: string;
  /** Centre of the figure, in the icon's own units. */
  x: number;
  y: number;
  /** Font size in the icon's units; a digit stands about .71 of it tall. */
  size: number;
  fill: string;
  /** Victory-point numbers sit on laurel and wear a dark edge. */
  edge?: boolean;
}

const DIGIT_HEIGHT = 0.71;

const FIGURES: Readonly<Partial<Record<string, readonly Figure[]>>> = {
  agriculture: [
    { outlines: ["M43.7 84.4"], text: "6", x: 44.5, y: 70, size: 36, fill: "#3b3b3b" },
    {
      outlines: ["M83.5 76.7"],
      text: "4",
      x: 94.6,
      y: 67.6,
      size: 42,
      fill: "#d2b146",
      edge: true,
    },
  ],
  economy: [{ outlines: ["M38.5 50.5"], text: "X", x: 43.2, y: 61.8, size: 34, fill: "#3b3b3b" }],
  mathematics: [
    {
      outlines: ["M90.3 107.4", "M84.3 106.2"],
      text: "3",
      x: 90.3,
      y: 89.8,
      size: 46,
      fill: "#d2b146",
      edge: true,
    },
  ],
  philosophy: [
    {
      outlines: ["M59.7 92 80.8", "M59.7 92l"],
      text: "7",
      x: 71.7,
      y: 68.3,
      size: 60,
      fill: "#d2b146",
      edge: true,
    },
  ],
  urbanism: [
    { outlines: ["M69.6 43.2"], text: "6", x: 69.7, y: 33.4, size: 25, fill: "#3b3b3b" },
    { outlines: ["M99.2 99v"], text: "4", x: 104.7, y: 95.1, size: 20, fill: "#3b3b3b" },
  ],
};

const escape = (s: string): string => s.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");

function typeset(f: Figure): string {
  const edge = f.edge
    ? ` stroke="#000" stroke-width="2.4" stroke-linejoin="round" paint-order="stroke"`
    : "";
  const y = f.y + (f.size * DIGIT_HEIGHT) / 2;
  return (
    `<text x="${String(f.x)}" y="${y.toFixed(1)}" text-anchor="middle" font-size="${String(f.size)}" ` +
    `font-weight="800" fill="${f.fill}"${edge} style="font-family:var(--figures,sans-serif);font-variant-numeric:lining-nums">${f.text}</text>`
  );
}

/**
 * The imported progress tokens with their numbers redrawn as text: the artwork's outlines are
 * thin and sit differently from the figures on the cards, so they are cut out and typeset.
 */
export function withFigures(
  icons: Readonly<Partial<Record<string, Imported>>>,
): Record<string, Imported> {
  const out: Record<string, Imported> = {};
  for (const [id, icon] of Object.entries(icons)) {
    if (icon === undefined) continue;
    const figures = FIGURES[id];
    if (figures === undefined) {
      out[id] = icon;
      continue;
    }
    let body = icon.body;
    for (const f of figures) {
      for (const prefix of f.outlines) {
        body = body.replace(new RegExp(`<path[^>]*? d="${escape(prefix)}[^"]*"[^>]*/>`, "g"), "");
      }
    }
    out[id] = { ...icon, body: body + figures.map(typeset).join("") };
  }
  return out;
}
