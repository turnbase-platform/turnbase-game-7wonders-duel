import type { Catalog } from "../core/catalog.js";
import type { Seats } from "../core/seats.js";
import { trackOf } from "../core/track.js";
import type { DuelView } from "../core/wire.js";
import { Icon } from "./icons.js";

interface Props {
  view: DuelView;
  catalog: Catalog;
  seats: Seats;
}

/** The military track (design §3, `track-v6`) with the board's progress tokens beside it. */
export function TrackStrip({ view, catalog, seats }: Props) {
  const { tiles, fields } = trackOf(view, catalog, seats);
  const tokens = view.visible.filter((p) => p.zone === "board");
  return (
    <div className="strip" aria-label="Military track">
      <div className="track">
        <div className="trk">
          {tiles.map((t, i) => (
            <span
              key={t.position}
              className={`tile${t.capital ? ` cap ${t.capital}` : ""}${Math.abs(t.position) < 5 ? " near" : ""}`}
              style={{ ["--i" as string]: i }}
              title={t.title ?? undefined}
            >
              {t.pawn ? (
                <i className="pawn" aria-label="Military pawn" />
              ) : t.capital ? (
                <Icon id="shield" />
              ) : (
                <small>{t.label}</small>
              )}
              {t.badge !== null && <i className="badge">{t.badge}</i>}
            </span>
          ))}
          {fields.map((f) => (
            <span
              key={f.from}
              className={`field ${f.kind} zone-${String(f.zone)}${f.current ? " is-current" : ""}${f.taken ? " is-taken" : ""}${f.kind === "defeat" ? " me" : f.kind === "win" ? " opp" : ""}`}
              style={{ gridColumn: `${String(f.from + 1)} / ${String(f.to + 2)}` }}
              title={f.title ?? undefined}
            >
              {f.text}
              {f.kind === "loot" && <Icon id="coin" />}
            </span>
          ))}
        </div>
      </div>
      <div className="strip__tokens" aria-label="Progress tokens">
        <small>Tokens</small>
        <div className="row">
          {tokens.map((p) => (
            <span key={p.component} className="tok" title={catalog.name(p.component)} />
          ))}
        </div>
      </div>
    </div>
  );
}
