import type { Catalog } from "../core/catalog.js";
import type { DuelView } from "../core/wire.js";
import { Card, type CardSize } from "./Card.js";

/** The progress tokens lying on the board, waiting to be taken (B-SET-3). */
export function BoardTokens({
  view,
  catalog,
  size,
  titled = false,
}: {
  view: DuelView;
  catalog: Catalog;
  size: CardSize;
  /** Names the tray, where it stands apart from the rest of the board. */
  titled?: boolean;
}) {
  const tokens = view.visible.filter((p) => p.zone === "board");
  if (tokens.length === 0) return null;
  return (
    <div className="board-tokens" aria-label="Progress tokens">
      {titled && <p className="tray__title">Progress tokens to take</p>}
      <div className="tray__tokens">
        {tokens.map((p) => {
          const entry = catalog.get(p.component);
          return entry === undefined ? null : <Card key={p.component} entry={entry} size={size} />;
        })}
      </div>
    </div>
  );
}
