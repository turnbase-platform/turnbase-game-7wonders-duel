// "This resource costs 1 coin": the resource a little smaller, the coin with its 1 at the bottom right.
// Built from the imported artwork, so a redrawn resource or coin carries over.
import { IMPORTED, type Imported } from "./imported.js";

const RESOURCES = ["wood", "stone", "clay", "glass", "papyrus"];

const nested = (icon: Imported, x: number, y: number, size: number): string =>
  `<svg x="${String(x)}" y="${String(y)}" width="${String(size)}" height="${String(size)}" viewBox="${icon.viewBox}">${icon.body}</svg>`;

function priced(resource: Imported, coin: Imported): Imported {
  return {
    viewBox: "0 0 120 120",
    body:
      nested(resource, 2, 2, 100) +
      nested(coin, 74, 74, 44) +
      `<text x="96" y="97" text-anchor="middle" dominant-baseline="central" font-family="serif" font-weight="800" font-size="30" fill="#1b1a17">1</text>`,
  };
}

/** `priced-<resource>` icons for the resources the imported set has. */
export const PRICED: Readonly<Record<string, Imported>> = Object.fromEntries(
  RESOURCES.flatMap((r) => {
    const resource = IMPORTED[r];
    const coin = IMPORTED.coin;
    return resource && coin ? [[`priced-${r}`, priced(resource, coin)]] : [];
  }),
);
