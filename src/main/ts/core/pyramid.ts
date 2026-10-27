import type { Size } from "./layout.js";
import type { ComponentId, DuelView } from "./wire.js";

/** Half a standard card's width in px: the unit a slot's `x` is measured in (design §7). */
export const HALF_CARD = 37.5;
export const CARD_W = 75;
export const CARD_H = 100;
/** The air between two neighbours in a row, in px at scale 1. */
export const GAP = 2;
/** What one half-card of `x` is worth in px: half a card and half a gap. */
export const COL_STEP = HALF_CARD + GAP / 2;
/** Rows overlap by about 30% of a card's height (design §7). */
export const ROW_STEP = 70;

const UP = "structure.up";
const DOWN = "structure.down";

/** One place of the structure: `row` counts from the top, `x` in half-cards from the left. */
export interface PyramidSlot {
  row: number;
  x: number;
  state: "up" | "down";
  /** The card, when it is face up. */
  component: ComponentId | null;
  /** The back group of a face-down card. */
  back: string | null;
}

export interface Pyramid {
  slots: PyramidSlot[];
  /** Rows from the top to the lowest slot, inclusive. */
  rows: number;
  /** Width in half-cards: the rightmost slot plus the two a card takes up. */
  cols: number;
}

export interface Extent {
  rows: number;
  cols: number;
}

function parse(key: string): { row: number; x: number } | null {
  const match = /^(\d+):(\d+)$/.exec(key);
  return match ? { row: Number(match[1]), x: Number(match[2]) } : null;
}

/**
 * The structure as geometry, read from the keys alone. A taken card is simply absent, so pass the
 * extent of earlier views to keep the frame from shrinking as the pyramid empties.
 */
export function pyramidOf(view: DuelView, floor: Extent = { rows: 0, cols: 0 }): Pyramid {
  const slots: PyramidSlot[] = [];
  for (const placed of view.visible) {
    const at = placed.zone === UP ? parse(placed.key) : null;
    if (at) slots.push({ ...at, state: "up", component: placed.component, back: null });
  }
  for (const hidden of view.hidden) {
    const at = hidden.zone === DOWN ? parse(hidden.key) : null;
    if (at) slots.push({ ...at, state: "down", component: null, back: hidden.back });
  }
  slots.sort((a, b) => a.row - b.row || a.x - b.x);
  const rows = slots.length === 0 ? 0 : Math.max(...slots.map((s) => s.row)) + 1;
  const cols = slots.length === 0 ? 0 : Math.max(...slots.map((s) => s.x)) + 2;
  return { slots, rows: Math.max(rows, floor.rows), cols: Math.max(cols, floor.cols) };
}

/**
 * How many half-cards to move every slot so the cards that remain sit in the middle of the extent,
 * not where the cards already taken left them.
 */
export function centringOf(slots: PyramidSlot[], cols: number): number {
  if (slots.length === 0) return 0;
  const xs = slots.map((s) => s.x);
  const min = Math.min(...xs);
  const span = Math.max(...xs) + 2 - min;
  return (cols - span) / 2 - min;
}

/** The width in px at scale 1 of a pyramid `cols` half-cards across. */
export function widthOf(cols: number): number {
  return (cols - 2) * COL_STEP + CARD_W;
}

const MIN_SCALE = 0.3;
const MAX_SCALE = 1.5;

/** The factor that makes a pyramid of this extent fit the box, in both directions. */
export function fit(box: Size, { rows, cols }: Extent): number {
  if (rows === 0 || cols === 0) return 1;
  const byWidth = box.width / widthOf(cols);
  const byHeight = box.height / ((rows - 1) * ROW_STEP + CARD_H);
  return Math.min(MAX_SCALE, Math.max(MIN_SCALE, Math.min(byWidth, byHeight)));
}
