import { useState } from "react";
import { isBoardPick, type Offer } from "../core/moves.js";
import { seatName, type Session } from "../core/session.js";
import { selectedOf, type Selection } from "../core/selection.js";
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
    return <Draft session={session} view={view} catalog={catalog} offers={offers} send={send} />;
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
  if (view.pending === null || isBoardPick(view)) return pyramid;
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
  session,
  view,
  catalog,
  offers,
  send,
}: {
  session: Session;
  view: DuelView;
  catalog: Catalog;
  offers: Offer[];
  send: (action: WireAction) => void;
}) {
  const [picked, setPicked] = useState<ComponentId | null>(null);
  const offered = view.visible.filter((p) => p.zone === "draft");
  const choice = selectedOf(picked, offers);
  const chosen = offers.find((o) => o.target === choice);
  const mine = offers.length > 0;
  return (
    <div
      className="phase-panel draft"
      onKeyDown={(e) => {
        if (e.key === "Escape") setPicked(null);
      }}
    >
      <h2>Choose a Wonder</h2>
      <div className="choices">
        {offered.map((p) => {
          const entry = catalog.get(p.component);
          if (!entry) return null;
          if (!mine) return <Card key={p.component} entry={entry} size="large" />;
          const on = choice === p.component;
          return (
            <button
              key={p.component}
              type="button"
              className={`draft__option${on ? " is-picked" : ""}`}
              aria-pressed={on}
              aria-label={entry.name}
              onClick={() => {
                setPicked(on ? null : p.component);
              }}
            >
              <Card entry={entry} size="large" />
            </button>
          );
        })}
      </div>
      {!mine && <p>Waiting for {seatName(session, view.active)} to choose.</p>}
      {mine && (
        <div className="draft__confirm">
          <button
            type="button"
            disabled={chosen === undefined}
            onClick={() => {
              if (chosen) send(chosen.action);
            }}
          >
            {choice === null ? "Confirm your choice" : `Confirm ${catalog.name(choice)}`}
          </button>
        </div>
      )}
    </div>
  );
}
