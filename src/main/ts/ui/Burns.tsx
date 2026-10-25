import type { Catalog } from "../core/catalog.js";
import type { Burn } from "../core/flip.js";
import { Card } from "./Card.js";

interface Props {
  burns: Burn[];
  catalog: Catalog;
  onDone: () => void;
}

/** A discarded card, drawn once more where it stood and burnt away from the bottom up. */
export function Burns({ burns, catalog, onDone }: Props) {
  return burns.map(({ component, rect }) => {
    const entry = catalog.get(component);
    return (
      entry && (
        <div
          key={component}
          className="burn"
          aria-hidden="true"
          style={{
            left: rect.left,
            top: rect.top,
            width: rect.width,
            height: rect.height,
            ["--bw" as string]: `${String(rect.width)}px`,
          }}
          onAnimationEnd={(e) => {
            if (e.target === e.currentTarget) onDone();
          }}
        >
          <div className="burn__card">
            <Card entry={entry} size="standard" />
          </div>
          <i className="burn__fire" />
        </div>
      )
    );
  });
}
