import { useEffect, useLayoutEffect, useRef, useState } from "react";
import { createPortal } from "react-dom";
import type { QuoteView } from "../core/wire.js";
import { Icon } from "./icons.js";

/**
 * The whole price of a component, resource by resource: what the cost asks, what the city covers,
 * what a discount waives, what is bought and at which unit price, then the coins (B-COST, B-TRADE).
 * Spans in a grid rather than a table, because it sits inside a button.
 */
export function QuoteTable({ quote }: { quote: QuoteView }) {
  if (quote.chained) {
    return (
      <span className="quote" role="table" aria-label="Price">
        <span role="row" className="quote__total">
          <span role="cell">Free by chain</span>
        </span>
      </span>
    );
  }
  return (
    <span className="quote" role="table" aria-label="Price">
      {quote.lines.length > 0 && (
        <span role="row" className="quote__head">
          <span role="columnheader" />
          <span role="columnheader">Cost</span>
          <span role="columnheader">City</span>
          <span role="columnheader">Discount</span>
          <span role="columnheader">Buy</span>
          <span role="columnheader">Each</span>
        </span>
      )}
      {quote.lines.map((l) => (
        <span role="row" key={l.resource} className="quote__line">
          <span role="rowheader" title={l.resource}>
            <Icon id={l.resource} />
          </span>
          <span role="cell">{l.needed}</span>
          <span role="cell">{l.produced}</span>
          <span role="cell">{l.discounted}</span>
          <span role="cell">{l.bought}</span>
          <span role="cell">{l.unitPrice}</span>
        </span>
      ))}
      <span role="row" className="quote__sum">
        <span role="cell">Printed coins</span>
        <span role="cell">{quote.coins}</span>
      </span>
      {quote.lines.length > 0 && (
        <span role="row" className="quote__sum">
          <span role="cell">Bought resources</span>
          <span role="cell">{quote.tradeCost}</span>
        </span>
      )}
      <span role="row" className="quote__sum quote__total">
        <span role="cell">Total</span>
        <span role="cell">{quote.total}</span>
      </span>
    </span>
  );
}

const GAP = 6;

/**
 * A quote that shows while its parent is hovered or focused, floating above it, or below when there
 * is no room above, and kept inside the viewport wherever the parent sits. It is portalled to the
 * body: a filtered or clipped ancestor would otherwise trap it.
 */
export function HoverQuote({ quote }: { quote: QuoteView }) {
  const marker = useRef<HTMLSpanElement>(null);
  const box = useRef<HTMLSpanElement>(null);
  const [anchor, setAnchor] = useState<HTMLElement | null>(null);
  const [open, setOpen] = useState(false);
  useEffect(() => {
    const parent = marker.current?.parentElement ?? null;
    if (!parent) return;
    setAnchor(parent);
    const show = (): void => {
      setOpen(true);
    };
    const hide = (): void => {
      setOpen(false);
    };
    parent.addEventListener("pointerenter", show);
    parent.addEventListener("pointerleave", hide);
    parent.addEventListener("focusin", show);
    parent.addEventListener("focusout", hide);
    return () => {
      parent.removeEventListener("pointerenter", show);
      parent.removeEventListener("pointerleave", hide);
      parent.removeEventListener("focusin", show);
      parent.removeEventListener("focusout", hide);
    };
  }, []);
  useLayoutEffect(() => {
    const el = box.current;
    if (!open || !el || !anchor) return;
    const at = anchor.getBoundingClientRect();
    const size = el.getBoundingClientRect();
    const above = at.top - size.height - GAP;
    const top = above >= GAP ? above : at.bottom + GAP;
    const left = at.left + at.width / 2 - size.width / 2;
    el.style.top = `${String(Math.max(GAP, Math.min(top, window.innerHeight - size.height - GAP)))}px`;
    el.style.left = `${String(Math.max(GAP, Math.min(left, window.innerWidth - size.width - GAP)))}px`;
    el.style.opacity = "1";
  }, [open, anchor]);
  return (
    <>
      <span ref={marker} hidden />
      {open &&
        createPortal(
          <span ref={box} className="slot__quote">
            <QuoteTable quote={quote} />
          </span>,
          document.body,
        )}
    </>
  );
}
