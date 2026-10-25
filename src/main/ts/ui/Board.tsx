import { useReducer, useRef, useState } from "react";
import { layoutFor, type Layout } from "../core/layout.js";
import { offers as offersOf } from "../core/moves.js";
import { attention, selectedOf, selectionReducer } from "../core/selection.js";
import { seatsOf, type Seats } from "../core/seats.js";
import type { Session } from "../core/session.js";
import type { WireAction } from "../core/wire.js";
import { City, sideLabel, Tab, type Side } from "./City.js";
import { Header } from "./Header.js";
import { IconSprite } from "./icons.js";
import { Log } from "./Log.js";
import { Structure } from "./Structure.js";
import { TrackStrip } from "./TrackStrip.js";
import { useSize } from "./useSize.js";

type Pane = "structure" | "city" | "log";

interface Props {
  session: Session;
  send: (action: WireAction) => void;
  /** Fixes the arrangement instead of measuring the container (tests and the gallery). */
  layout?: Layout;
}

/** The board of design §3: header, track strip, structure and the panel with the cities and log. */
export function Board({ session, send, layout: fixed }: Props) {
  const root = useRef<HTMLDivElement>(null);
  const size = useSize(root);
  const layout = fixed ?? (size === null ? "wide" : layoutFor(size));
  const [pane, setPane] = useState<Pane>("structure");
  const [side, setSide] = useState<Side>("me");
  const [selection, select] = useReducer(selectionReducer, null);
  const { view, catalog } = session;
  const seats = seatsOf(session);
  if (view === null || catalog === null || seats === null) return null;

  const available = offersOf(session);
  const selected = selectedOf(selection, available);
  const phone = layout === "portrait";
  const compact = layout === "landscape" || phone;
  const showing = (wanted: Pane): boolean => !phone || pane === wanted;
  const goTo = (next: Pane, which?: Side): void => {
    setPane(next);
    if (which) setSide(which);
  };

  const cities = (
    <div className="cities">
      <div role="tablist" className="tabs">
        {(["opp", "me"] as const).map((s) => (
          <Tab
            key={s}
            label={sideLabel(session, seats, s)}
            tint={s}
            active={side === s}
            onPick={() => {
              setSide(s);
            }}
          />
        ))}
      </div>
      <City session={session} seats={seats} side={side} />
    </div>
  );

  return (
    <div ref={root} className={`board layout-${layout}${compact ? " is-compact" : ""}`}>
      <IconSprite />
      <Header session={session} seats={seats} />
      <TrackStrip view={view} />
      {session.rejection !== null && <div className="banner">{session.rejection}</div>}
      <div className="main">
        {showing("structure") && (
          <main className="structure">
            <Structure
              session={session}
              view={view}
              catalog={catalog}
              offers={available}
              selected={selected}
              onSelect={(component) => {
                select({ type: "toggle", component });
              }}
              send={send}
            />
          </main>
        )}
        {layout === "wide" && (
          <aside className="panel">
            {cities}
            <Log session={session} />
          </aside>
        )}
        {(layout === "medium" || layout === "landscape") && (
          <aside className="panel">
            <PanelTabs
              session={session}
              seats={seats}
              active={pane === "log" ? "log" : side}
              onPick={(next) => {
                if (next === "log") goTo("log");
                else goTo("city", next);
              }}
            />
            {pane === "log" ? (
              <Log session={session} />
            ) : (
              <City session={session} seats={seats} side={side} />
            )}
          </aside>
        )}
        {phone && pane === "city" && <aside className="panel">{cities}</aside>}
        {phone && pane === "log" && (
          <aside className="panel">
            <Log session={session} />
          </aside>
        )}
      </div>
      {phone && (
        <nav className="bottom-tabs" role="tablist">
          {(
            [
              ["structure", "Structure"],
              ["city", "Cities"],
              ["log", "Log"],
            ] as const
          ).map(([which, label]) => (
            <button
              key={which}
              type="button"
              role="tab"
              aria-selected={pane === which}
              className={`tab${attention(available) === which && pane !== which ? " needs-you" : ""}`}
              onClick={() => {
                goTo(which);
              }}
            >
              {label}
            </button>
          ))}
        </nav>
      )}
    </div>
  );
}

function PanelTabs({
  session,
  seats,
  active,
  onPick,
}: {
  session: Session;
  seats: Seats;
  active: Side | "log";
  onPick: (next: Side | "log") => void;
}) {
  return (
    <div role="tablist" className="tabs">
      {(["opp", "me"] as const).map((s) => (
        <Tab
          key={s}
          label={sideLabel(session, seats, s)}
          tint={s}
          active={active === s}
          onPick={() => {
            onPick(s);
          }}
        />
      ))}
      <Tab
        label="Log"
        active={active === "log"}
        onPick={() => {
          onPick("log");
        }}
      />
    </div>
  );
}
