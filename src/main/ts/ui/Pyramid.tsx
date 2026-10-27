import { useRef } from "react";
import type { Catalog } from "../core/catalog.js";
import type { Offer } from "../core/moves.js";
import {
  CARD_H,
  CARD_W,
  centringOf,
  fit,
  COL_STEP,
  widthOf,
  pyramidOf,
  ROW_STEP,
  type Extent,
  type PyramidSlot,
} from "../core/pyramid.js";
import type { Selection } from "../core/selection.js";
import type { ComponentId, DuelView, PlayerId, QuoteView } from "../core/wire.js";
import { Card, CardBack } from "./Card.js";
import { HoverQuote } from "./QuoteTable.js";
import { useSize } from "./useSize.js";

interface Props {
  view: DuelView;
  catalog: Catalog;
  offers: Offer[];
  /** Whose price is quoted on hover; null for a spectator. */
  self: PlayerId | null;
  selected: Selection;
  onSelect: (component: ComponentId) => void;
}

export function Pyramid({ view, catalog, offers, self, selected, onSelect }: Props) {
  const frame = useRef<HTMLDivElement>(null);
  const measured = useSize(frame);
  // The frame only ever grows within an Age, so cards leaving it never move the ones that stay.
  const extent = useRef<Extent & { age: number }>({ age: view.age, rows: 0, cols: 0 });
  if (extent.current.age !== view.age) extent.current = { age: view.age, rows: 0, cols: 0 };
  const pyramid = pyramidOf(view, extent.current);
  extent.current = { age: view.age, rows: pyramid.rows, cols: pyramid.cols };

  const scale = fit(measured ?? { width: 600, height: Infinity }, pyramid);
  const shift = centringOf(pyramid.slots, pyramid.cols);
  const accessible = new Set(view.quotes.filter((q) => q.takeable).map((q) => q.component));
  const mine = new Set(offers.map((o) => o.target));
  return (
    <div className="pyramid-frame" ref={frame}>
      <div
        className="pyramid"
        style={{
          width: widthOf(pyramid.cols) * scale,
          height: ((pyramid.rows - 1) * ROW_STEP + CARD_H) * scale,
          ["--cw" as string]: `${String(CARD_W * scale)}px`,
        }}
      >
        {/* One box moves when the cards are re-centred, so every card slides together, in step. */}
        <div
          className="pyramid__cards"
          style={{ transform: `translateX(${String(shift * COL_STEP * scale)}px)` }}
        >
          {pyramid.slots.map((slot) => (
            <Slot
              key={`${String(slot.row)}:${String(slot.x)}`}
              slot={slot}
              scale={scale}
              catalog={catalog}
              state={slotState(slot, accessible, mine, selected)}
              quote={view.quotes.find((q) => q.player === self && q.component === slot.component)}
              rival={view.quotes.find((q) => q.player !== self && q.component === slot.component)}
              onSelect={onSelect}
            />
          ))}
        </div>
      </div>
    </div>
  );
}

type SlotState = "covered" | "waiting" | "takeable" | "selected";

function slotState(
  slot: PyramidSlot,
  accessible: Set<ComponentId>,
  mine: Set<ComponentId>,
  selected: Selection,
): SlotState {
  const id = slot.component;
  if (id === null || !accessible.has(id)) return "covered";
  if (!mine.has(id)) return "waiting";
  return id === selected ? "selected" : "takeable";
}

interface SlotProps {
  slot: PyramidSlot;
  scale: number;
  catalog: Catalog;
  state: SlotState;
  quote: QuoteView | undefined;
  /** What the other player would pay for the same card. */
  rival: QuoteView | undefined;
  onSelect: (component: ComponentId) => void;
}

function Slot({ slot, scale, catalog, state, quote, rival, onSelect }: SlotProps) {
  const style = { left: slot.x * COL_STEP * scale, top: slot.row * ROW_STEP * scale };
  const entry = slot.component === null ? undefined : catalog.get(slot.component);
  const face =
    entry === undefined ? (
      <CardBack group={slot.back} size="standard" />
    ) : (
      <Card entry={entry} size="standard" />
    );
  if (slot.component === null || state === "covered" || state === "waiting") {
    return (
      <div className={`slot is-${state}`} style={style} tabIndex={quote ? 0 : undefined}>
        {face}
        {quote && <HoverQuote quote={quote} rival={rival} />}
      </div>
    );
  }
  const component = slot.component;
  return (
    <button
      type="button"
      className={`slot is-${state}`}
      style={style}
      aria-pressed={state === "selected"}
      aria-label={catalog.name(component)}
      onClick={() => {
        onSelect(component);
      }}
    >
      {face}
      {quote && <HoverQuote quote={quote} rival={rival} />}
    </button>
  );
}
