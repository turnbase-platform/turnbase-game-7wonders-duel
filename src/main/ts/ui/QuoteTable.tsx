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
