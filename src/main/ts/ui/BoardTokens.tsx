import type { Catalog } from "../core/catalog.js";
import { isBoardPick, type Offer } from "../core/moves.js";
import { seatName, type Session } from "../core/session.js";
import type { WireAction } from "../core/wire.js";
import { Card, type CardSize } from "./Card.js";

/**
 * The progress tokens lying on the board, waiting to be taken (B-SET-3). When a decision picks one
 * of them, the tokens themselves are the buttons.
 */
export function BoardTokens({
  session,
  catalog,
  size,
  offers,
  send,
}: {
  session: Session;
  catalog: Catalog;
  size: CardSize;
  offers: Offer[];
  send: (action: WireAction) => void;
}) {
  const { view } = session;
  if (view === null) return null;
  const tokens = view.visible.filter((p) => p.zone === "board");
  if (tokens.length === 0) return null;
  const picking = isBoardPick(view) ? view.pending : null;
  return (
    <div className="board-tokens" aria-label="Progress tokens">
      {picking && (
        <p className="board-tokens__ask" role="status">
          {offers.length > 0
            ? "Choose a progress token"
            : `Waiting for ${seatName(session, picking.decider)} to choose`}
        </p>
      )}
      <div className="tray__tokens">
        {tokens.map((p) => {
          const entry = catalog.get(p.component);
          if (entry === undefined) return null;
          const card = <Card entry={entry} size={size} />;
          const answer = offers.find((o) => o.target === p.component);
          return answer ? (
            <button
              key={p.component}
              type="button"
              className="token-pick"
              aria-label={`Take ${entry.name}`}
              onClick={() => {
                send(answer.action);
              }}
            >
              {card}
            </button>
          ) : (
            <span key={p.component}>{card}</span>
          );
        })}
      </div>
    </div>
  );
}
