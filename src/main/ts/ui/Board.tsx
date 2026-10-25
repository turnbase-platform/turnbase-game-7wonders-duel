import { useLayoutEffect, useReducer, useRef, useState, type RefObject } from "react";
import { layoutFor, type Layout, type Size } from "../core/layout.js";
import { offers as offersOf } from "../core/moves.js";
import { attention, selectedOf, selectionReducer } from "../core/selection.js";
import { seatsOf, type Seats } from "../core/seats.js";
import type { Session } from "../core/session.js";
import type { WireAction } from "../core/wire.js";
import { Actions, type Anchor } from "./Actions.js";
import { City, sideLabel, Tab, type Side } from "./City.js";
import { Header } from "./Header.js";
import { IconSprite } from "./icons.js";
import { Log } from "./Log.js";
import { Structure } from "./Structure.js";
import { TrackStrip } from "./TrackStrip.js";
import { useChoreography } from "./useChoreography.js";
import { useSize } from "./useSize.js";

const POPOVER_WIDTH = 290;

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
  const available = view === null ? [] : offersOf(session);
  const selected = selectedOf(selection, available);
  const anchor = useAnchor(root, selected, layout, size);
  useChoreography(root, session.history.at(-1) ?? null);
  const seats = seatsOf(session);
  if (view === null || catalog === null || seats === null) return null;

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
      <TrackStrip view={view} catalog={catalog} seats={seats} />
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
      {selected !== null && session.self !== null && (
        <Actions
          key={selected}
          view={view}
          catalog={catalog}
          self={session.self}
          component={selected}
          offers={available}
          anchor={layout === "wide" || layout === "medium" ? anchor : null}
          send={send}
          onClose={() => {
            select({ type: "clear" });
          }}
        />
      )}
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

/** Where the selected card is on the board, for the popover to sit beside; null while none is. */
function useAnchor(
  root: RefObject<HTMLDivElement | null>,
  selected: string | null,
  layout: Layout,
  size: Size | null,
): Anchor | null {
  const [anchor, setAnchor] = useState<Anchor | null>(null);
  useLayoutEffect(() => {
    const board = root.current;
    const slot = selected === null ? null : board?.querySelector(".slot.is-selected");
    if (!board || !slot) {
      setAnchor(null);
      return;
    }
    const box = board.getBoundingClientRect();
    const rect = slot.getBoundingClientRect();
    const right = rect.right - box.left + 8;
    const left =
      right + POPOVER_WIDTH > box.width ? rect.left - box.left - POPOVER_WIDTH - 8 : right;
    setAnchor({
      left: Math.max(4, left),
      top: Math.max(4, rect.top - box.top),
    });
  }, [root, selected, layout, size]);
  return anchor;
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
