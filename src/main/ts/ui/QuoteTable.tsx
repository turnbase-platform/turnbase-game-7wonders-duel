import { useEffect, useLayoutEffect, useRef, useState } from "react";
import { createPortal } from "react-dom";
import type { QuoteView } from "../core/wire.js";
import { Icon } from "./icons.js";

/**
 * The whole price of a component as one table: per resource, what the cost asks, what the city
 * produces, what a discount waives, what is bought, and the coins that buys (B-COST, B-TRADE). The
 * coins row, only when the card has a coin cost, is that cost; the coins column adds up to the
 * total. Spans in a grid rather than a table, because it sits inside a button.
 */
export function QuoteTable({ quote }: { quote: QuoteView }) {
  if (quote.chained) {
    return (
      <span className="quote quote--brief" role="table" aria-label="Price">
        <span role="row">
          <span role="cell">Free by chain</span>
        </span>
      </span>
    );
  }
  return (
    <span className="quote" role="table" aria-label="Price">
      {quote.lines.length > 0 && (
        <span role="row" className="quote__head">
          <span role="columnheader" aria-label="Resource" />
          <span role="columnheader">Needed</span>
          <span role="columnheader">City</span>
          <span role="columnheader">Discount</span>
          <span role="columnheader">Buy</span>
          <span role="columnheader">Coins</span>
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
          <span role="cell">{l.bought * l.unitPrice}</span>
        </span>
      ))}
      {quote.coins > 0 && (
        <span role="row" className="quote__line">
          <span role="rowheader" title="coins">
            <Icon id="coin" />
          </span>
          <span role="cell">{quote.coins}</span>
          <span role="cell">–</span>
          <span role="cell">–</span>
          <span role="cell">–</span>
          <span role="cell">{quote.coins}</span>
        </span>
      )}
      {(quote.lines.length > 0 || quote.coins === 0) && (
        <span role="row" className="quote__sum quote__total">
          <span role="rowheader">Total</span>
          <span role="cell">{quote.total}</span>
        </span>
      )}
    </span>
  );
}

/** Just the price in coins, for what another player pays. */
export function QuoteCoins({ quote, who }: { quote: QuoteView; who: string }) {
  return (
    <span className="quote quote--brief" role="status">
      {who}: {quote.chained ? "free by chain" : `${String(quote.total)} coins`}
    </span>
  );
}

/** Nothing to explain: no resources, no coins, no chain. */
const isFree = (quote: QuoteView): boolean =>
  !quote.chained && quote.total === 0 && quote.lines.length === 0;

/** How long the pointer rests on the parent before its quote shows. */
export const QUOTE_DELAY_MS = 500;

const GAP = 6;

/**
 * A quote that shows while its parent is hovered or focused, floating above it, or below when there
 * is no room above, and kept inside the viewport wherever the parent sits. It is portalled to the
 * body: a filtered or clipped ancestor would otherwise trap it.
 */
export function HoverQuote({
  quote,
  rival,
  rivalName = "Opponent pays",
  brief = false,
}: {
  quote: QuoteView;
  /** What the other player would pay, shown as coins only. */
  rival?: QuoteView;
  rivalName?: string;
  /** Only the coins, without the table. */
  brief?: boolean;
}) {
  const marker = useRef<HTMLSpanElement>(null);
  const box = useRef<HTMLSpanElement>(null);
  const [anchor, setAnchor] = useState<HTMLElement | null>(null);
  const [open, setOpen] = useState(false);
  useEffect(() => {
    const parent = marker.current?.parentElement ?? null;
    if (!parent) return;
    setAnchor(parent);
    let timer: ReturnType<typeof setTimeout> | undefined;
    const show = (): void => {
      clearTimeout(timer);
      timer = setTimeout(() => {
        setOpen(true);
      }, QUOTE_DELAY_MS);
    };
    const hide = (): void => {
      clearTimeout(timer);
      setOpen(false);
    };
    // Focus the page moved itself (a dialog opening) is not the player asking: only keyboard focus counts.
    const focus = (): void => {
      if (parent.matches(":focus-visible")) show();
    };
    parent.addEventListener("pointerenter", show);
    parent.addEventListener("pointerleave", hide);
    parent.addEventListener("focusin", focus);
    parent.addEventListener("focusout", hide);
    return () => {
      clearTimeout(timer);
      parent.removeEventListener("pointerenter", show);
      parent.removeEventListener("pointerleave", hide);
      parent.removeEventListener("focusin", focus);
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
        !isFree(quote) &&
        createPortal(
          <span ref={box} className="slot__quote">
            {brief ? (
              <QuoteCoins quote={quote} who="Opponent pays" />
            ) : (
              <QuoteTable quote={quote} />
            )}
            {rival && <QuoteCoins quote={rival} who={rivalName} />}
          </span>,
          document.body,
        )}
    </>
  );
}
