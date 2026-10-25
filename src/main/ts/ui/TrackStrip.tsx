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

/** The military track (design §3, `track-v6`). */
export function TrackStrip({ view, catalog, seats }: Props) {
  const { tiles, fields } = trackOf(view, catalog, seats);
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
            </span>
          ))}
          {fields.map((f) => (
            <span
              key={f.from}
              className={`field ${f.kind} zone-${String(f.zone)}${f.current ? " is-current" : ""}${f.taken ? " is-taken" : ""}`}
              style={{ gridColumn: `${String(f.from + 1)} / ${String(f.to + 2)}` }}
              title={f.title ?? undefined}
            >
              {f.kind === "loot" ? (
                <span className="glyph">
                  <Icon id="coin-lost" />
                  <b>{f.text}</b>
                </span>
              ) : (
                f.text
              )}
            </span>
          ))}
        </div>
      </div>
    </div>
  );
}
