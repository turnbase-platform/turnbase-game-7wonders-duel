import type { Offer } from "../core/moves.js";
import { seatName, type Session } from "../core/session.js";
import type { Selection } from "../core/selection.js";
import type { ComponentId, DuelView, WireAction } from "../core/wire.js";
import type { Catalog } from "../core/catalog.js";
import { Card } from "./Card.js";
import { Prompt } from "./Prompt.js";
import { Pyramid } from "./Pyramid.js";

interface Props {
  session: Session;
  view: DuelView;
  catalog: Catalog;
  offers: Offer[];
  selected: Selection;
  onSelect: (component: ComponentId) => void;
  send: (action: WireAction) => void;
}

/** What fills the structure area: the starter choice, the Wonder draft, or the Age's pyramid. */
export function Structure({ session, view, catalog, offers, selected, onSelect, send }: Props) {
  if (view.deciding !== null) {
    return <StarterChoice session={session} deciding={view.deciding} offers={offers} send={send} />;
  }
  if (view.phase === "WONDER_DRAFT") {
    return <Draft view={view} catalog={catalog} offers={offers} send={send} />;
  }
  const pyramid = (
    <Pyramid
      view={view}
      catalog={catalog}
      offers={offers}
      self={session.self}
      selected={selected}
      onSelect={onSelect}
    />
  );
  if (view.pending === null) return pyramid;
  return (
    <>
      <Prompt
        session={session}
        pending={view.pending}
        catalog={catalog}
        offers={offers}
        send={send}
      />
      {pyramid}
    </>
  );
}

function StarterChoice({
  session,
  deciding,
  offers,
  send,
}: {
  session: Session;
  deciding: string;
  offers: Offer[];
  send: (action: WireAction) => void;
}) {
  const starters = offers.filter((o) => o.action.type === "chooseStarter");
  return (
    <div className="phase-panel">
      <h2>Who starts the Age?</h2>
      {starters.length === 0 ? (
        <p>Waiting for {seatName(session, deciding)} to choose.</p>
      ) : (
        <div className="choices">
          {starters.map((o) => (
            <button
              key={o.target}
              type="button"
              onClick={() => {
                send(o.action);
              }}
            >
              {o.label}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}

function Draft({
  view,
  catalog,
  offers,
  send,
}: {
  view: DuelView;
  catalog: Catalog;
  offers: Offer[];
  send: (action: WireAction) => void;
}) {
  const offered = view.visible.filter((p) => p.zone === "draft");
  return (
    <div className="phase-panel">
      <h2>Choose a Wonder</h2>
      <div className="choices">
        {offered.map((p) => {
          const entry = catalog.get(p.component);
          const take = offers.find((o) => o.target === p.component);
          return (
            <div key={p.component} className="choice">
              {entry && <Card entry={entry} size="standard" />}
              {take && (
                <button
                  type="button"
                  onClick={() => {
                    send(take.action);
                  }}
                >
                  {take.label}
                </button>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}
