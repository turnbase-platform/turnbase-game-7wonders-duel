import { present } from "./badges.js";
import type { CatalogEntry } from "./wire.js";

/** The badge a reader would name a card by, most telling first. */
const PRIORITY = [
  "science",
  "shields",
  "playAgain",
  "produces",
  "producesOneOf",
  "fixedPrice",
  "drawProgress",
  "buildFromDiscard",
  "discardOpponent",
  "coinsPer",
  "coins",
  "pointsPer",
  "tradeRevenue",
  "reduction",
  "points",
];

/**
 * The icon drawn large in a component's art area: a progress token is its own picture, anything else is the
 * glyph of its most telling badge.
 */
export function motifOf(entry: CatalogEntry): string {
  if (entry.tags.includes("progress")) {
    return entry.id;
  }
  for (const kind of PRIORITY) {
    const badge = entry.badges.find((b) => b.kind === kind);
    // What a per-card reward counts is a corner of its glyph; the card is named by the coins.
    const first = badge && (kind === "coinsPer" ? { icon: "coin" } : present(badge)[0]);
    if (first) {
      return first.icon;
    }
  }
  return "unknown";
}
