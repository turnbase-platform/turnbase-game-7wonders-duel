import type { Catalog } from "../core/catalog.js";
import type { Offer } from "../core/moves.js";
import { seatName, type Session } from "../core/session.js";
import type { PendingView, WireAction } from "../core/wire.js";
import { Card } from "./Card.js";

interface Props {
  session: Session;
  pending: PendingView;
  catalog: Catalog;
  offers: Offer[];
  send: (action: WireAction) => void;
}

/** What the title asks for, read from the options: progress tokens, a player, or plain cards. */
function titleOf(pending: PendingView, catalog: Catalog): string {
  const kinds = pending.options.map((id) => catalog.get(id));
  if (kinds.length > 0 && kinds.every((e) => e === undefined)) return "Choose a player";
  if (kinds.some((e) => e?.tags.includes("progress"))) return "Choose a progress token";
  return "Choose a card";
}

/** A decision the game waits on (B-EFF-3): the options as cards, answerable only by the decider. */
export function Prompt({ session, pending, catalog, offers, send }: Props) {
  const title = titleOf(pending, catalog);
  const decides = offers.length > 0;
  return (
    <section className="phase-panel prompt" aria-label={title}>
      <h2>{decides ? title : `Waiting for ${seatName(session, pending.decider)} to choose`}</h2>
      <div className="choices">
        {pending.options.map((id) => {
          const entry = catalog.get(id);
          const answer = offers.find((o) => o.target === id);
          return (
            <div key={id} className="choice">
              {entry && <Card entry={entry} size="standard" />}
              {answer && (
                <button
                  type="button"
                  onClick={() => {
                    send(answer.action);
                  }}
                >
                  {entry ? answer.label : seatName(session, id)}
                </button>
              )}
            </div>
          );
        })}
        {Array.from({ length: pending.hidden }, (_, i) => (
          <div
            key={`hidden-${String(i)}`}
            className="choice choice--hidden"
            aria-label="Hidden option"
          >
            ?
          </div>
        ))}
      </div>
    </section>
  );
}
