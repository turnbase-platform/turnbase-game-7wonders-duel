import type { ScienceCount } from "../core/city.js";
import { Icon } from "./icons.js";
import { HoverFloat } from "./QuoteTable.js";

/** The science symbols of one city: the ones it holds in colour, the rest desaturated (B-SCI-2). */
export function Science({ symbols }: { symbols: ScienceCount[] }) {
  return (
    <ul className="science" aria-label="Science">
      {symbols.map(({ symbol, count }) => (
        <li key={symbol} className={`science__symbol${count > 0 ? " is-held" : ""}`}>
          <Icon id={symbol} />
          <HoverFloat>{count > 0 ? `${symbol}: held` : `${symbol}: not held`}</HoverFloat>
        </li>
      ))}
    </ul>
  );
}
