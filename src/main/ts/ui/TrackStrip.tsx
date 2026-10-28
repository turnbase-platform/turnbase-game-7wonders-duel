import type { Catalog } from "../core/catalog.js";
import type { Seats } from "../core/seats.js";
import { trackOf } from "../core/track.js";
import type { DuelView } from "../core/wire.js";
import { Icon } from "./icons.js";
import { HoverFloat } from "./QuoteTable.js";

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
              className={`tile${t.capital ? ` cap ${t.capital}` : ""}`}
              style={{ ["--i" as string]: i }}
            >
              <HoverFloat enabled={t.title !== null}>{t.title}</HoverFloat>
            </span>
          ))}
          <i className="pawn" aria-label="Military pawn" style={{ ["--at" as string]: pawn }} />
          {fields.map((f) => (
            <span
              key={f.from}
              className={`field ${f.kind} zone-${String(f.zone)}${f.current ? " is-current" : ""}${f.taken ? " is-taken" : ""}`}
              style={{ gridColumn: `${String(f.from + 1)} / ${String(f.to + 2)}` }}
            >
              <HoverFloat enabled={f.title !== null}>{f.title}</HoverFloat>
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
