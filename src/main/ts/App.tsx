import { canAct, statusLine, type Model } from "./model.js";

/**
 * The whole screen. Skeleton: a status line and an "End turn" button. The board, card pyramid,
 * wonders and resource tracks replace the placeholder `<section>`.
 */
export function App({ model, onEndTurn }: { model: Model; onEndTurn: () => void }) {
  return (
    <main>
      <h1>7 Wonders Duel</h1>
      <p role="status">{statusLine(model)}</p>
      <section aria-label="Table">
        <p>Turn {model.turnNumber}</p>
      </section>
      <button type="button" disabled={!canAct(model)} onClick={onEndTurn}>
        End turn
      </button>
    </main>
  );
}
