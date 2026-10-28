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
  const { tiles, pawn, fields } = trackOf(view, catalog, seats);
  return (
    <div className="strip" aria-label="Military track">
      <div className="track">
        <div className="trk">
          {tiles.map((t, i) => (
            <span
              key={t.position}
              className={`tile${t.capital ? ` cap ${t.capital}` : ""}${t.position <= -5 ? " defeat" : ""}`}
              style={{ ["--i" as string]: i }}
              title={t.title ?? undefined}
            >
              {t.capital ? <Icon id="fortress" /> : <small>{t.label}</small>}
            </span>
          ))}
          <i className="pawn" aria-label="Military pawn" style={{ ["--at" as string]: pawn }}>
            <svg viewBox="0 0 24 28" aria-hidden="true">
              <ellipse cx="12" cy="25" rx="9" ry="2.6" className="pawn__base" />
              <path
                className="pawn__body"
                d="M12 9.5c-2.7 0-4.3 1.7-4.3 3.7 0 1.3.7 2.3 1.6 2.900C8 17.5 6.8 20 6.3 23.3h11.4C17.2 20 16 17.500 14.7 16.100c.9-.6 1.6-1.6 1.6-2.900 0-2-1.6-3.7-4.3-3.7z"
              />
              <circle cx="12" cy="6" r="4.2" className="pawn__body" />
            </svg>
          </i>
          {fields.map((f) => (
            <span
              key={f.from}
              className={`field ${f.kind} zone-${String(f.zone)}${f.current ? " is-current" : ""}${f.taken ? " is-taken" : ""}`}
              style={{ gridColumn: `${String(f.from + 1)} / ${String(f.to + 2)}` }}
              title={f.title ?? undefined}
            >
              <span className="glyph">
                <Icon id={f.icon} />
                <b>{f.text}</b>
              </span>
            </span>
          ))}
        </div>
      </div>
    </div>
  );
}
