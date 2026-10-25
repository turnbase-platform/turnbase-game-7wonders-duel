import type { ScienceCount } from "../core/city.js";
import { Icon } from "./icons.js";

/** The science symbols of one city: held ones lit with their count, a pair marked (B-SCI-2). */
export function Science({ symbols }: { symbols: ScienceCount[] }) {
  return (
    <ul className="science" aria-label="Science">
      {symbols.map(({ symbol, count }) => (
        <li
          key={symbol}
          className={`science__symbol${count > 0 ? " is-held" : ""}${count >= 2 ? " is-pair" : ""}`}
          title={`${symbol}: ${String(count)}`}
        >
          <Icon id={symbol} />
          {count > 1 && <b>{count}</b>}
        </li>
      ))}
    </ul>
  );
}
