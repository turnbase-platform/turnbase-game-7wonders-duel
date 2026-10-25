import type { Catalog } from "../core/catalog.js";
import { discardedOf } from "../core/discard.js";
import type { DuelView } from "../core/wire.js";
import { Card } from "./Card.js";

/** The cards thrown away so far, in the order they were thrown. */
export function DiscardPile({ view, catalog }: { view: DuelView; catalog: Catalog }) {
  const cards = discardedOf(view);
  return (
    <section className="pile">
      <h3>Discard pile</h3>
      {cards.length === 0 ? (
        <p className="pile__empty">Nothing discarded yet.</p>
      ) : (
        <div className="pile__cards">
          {cards.map((id) => {
            const entry = catalog.get(id);
            return entry && <Card key={id} entry={entry} size="standard" />;
          })}
        </div>
      )}
    </section>
  );
}
