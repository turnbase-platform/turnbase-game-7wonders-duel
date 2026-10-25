import { useEffect, useLayoutEffect, useReducer, useRef, useState, type RefObject } from "react";
import { layoutFor, type Layout, type Size } from "../core/layout.js";
import { offers as offersOf } from "../core/moves.js";
import { attention, selectedOf, selectionReducer } from "../core/selection.js";
import { seatsOf, type Seats } from "../core/seats.js";
import type { Session } from "../core/session.js";
import type { WireAction } from "../core/wire.js";
import { discardedOf } from "../core/discard.js";
import { BoardTokens } from "./BoardTokens.js";
import { Actions, type Placement } from "./Actions.js";
import { City, sideLabel, Tab, type Side } from "./City.js";
import { Burns } from "./Burns.js";
import { DiscardPile } from "./DiscardPile.js";
import { Header } from "./Header.js";
import { Icon, IconSprite } from "./icons.js";
import { Outcome } from "./Outcome.js";
import { Log } from "./Log.js";
import { Structure } from "./Structure.js";
import { TrackStrip } from "./TrackStrip.js";
import { MOTION_MS, prefersReducedMotion } from "./motion.js";
import { useChoreography } from "./useChoreography.js";
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
  const [drawer, setDrawer] = useState<"log" | "discard" | null>(null);
  const [selection, select] = useReducer(selectionReducer, null);
  const { view, catalog } = session;
  const available = view === null ? [] : offersOf(session);
  const selected = selectedOf(selection, available);
  const placement = usePlacement(root, selected, layout, size);
  const shown = useLingering(selected, placement);
  const { burns, clearBurns } = useChoreography(root, session.history.at(-1) ?? null);
  const seats = seatsOf(session);
  if (view === null || catalog === null || seats === null) return null;

  const phone = layout === "portrait";
  const wide = layout === "wide";
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
      <Burns burns={burns} catalog={catalog} onDone={clearBurns} />
      {!wide && <Header session={session} seats={seats} />}
      {session.rejection !== null && <div className="banner">{session.rejection}</div>}
      <div className="main">
        {wide && (
          <aside className="city-col">
            <City
              session={session}
              seats={seats}
              side="me"
              title={sideLabel(session, seats, "me")}
              coins
            />
          </aside>
        )}
        <div className="stage">
          <Outcome session={session} view={view} seats={seats} />
          <TrackStrip view={view} catalog={catalog} seats={seats} />
          {showing("structure") && (
            <main className="structure">
              <BoardTokens view={view} catalog={catalog} size={compact ? "small" : "standard"} />
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
              {shown !== null && session.self !== null && (
                <Actions
                  key={shown.component}
                  leaving={selected === null}
                  view={view}
                  catalog={catalog}
                  self={session.self}
                  component={shown.component}
                  offers={available}
                  placement={shown.placement}
                  send={send}
                  onClose={() => {
                    select({ type: "clear" });
                  }}
                />
              )}
            </main>
          )}
          {showing("structure") && (
            <div className="stagebar">
              <button
                type="button"
                className="pilebtn"
                aria-expanded={drawer === "discard"}
                aria-label="Discard pile"
                onClick={() => {
                  setDrawer(drawer === "discard" ? null : "discard");
                }}
              >
                <Icon id="discard-pile" /> {discardedOf(view).length}
              </button>
              {!phone && (
                <button
                  type="button"
                  className="logbtn"
                  aria-expanded={drawer === "log"}
                  onClick={() => {
                    setDrawer(drawer === "log" ? null : "log");
                  }}
                >
                  Log
                </button>
              )}
            </div>
          )}
        </div>
        {wide && (
          <aside className="city-col">
            <City
              session={session}
              seats={seats}
              side="opp"
              title={sideLabel(session, seats, "opp")}
              coins
            />
          </aside>
        )}
        {drawer !== null && (!phone || drawer === "discard") && (
          <aside
            className="drawer"
            role="dialog"
            aria-label={drawer === "log" ? "Log" : "Discard pile"}
          >
            <button
              type="button"
              className="drawer__close"
              aria-label={drawer === "log" ? "Close the log" : "Close the discard pile"}
              onClick={() => {
                setDrawer(null);
              }}
            >
              ×
            </button>
            {drawer === "log" ? (
              <Log session={session} />
            ) : (
              <DiscardPile view={view} catalog={catalog} />
            )}
          </aside>
        )}
        {(layout === "medium" || layout === "landscape") && (
          <aside className="panel">
            <PanelTabs
              session={session}
              seats={seats}
              active={side}
              onPick={(next) => {
                goTo("city", next);
              }}
            />
            <City session={session} seats={seats} side={side} />
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

/** Where the selected card sits in the structure area, which its lifted copy and actions follow. */
function usePlacement(
  root: RefObject<HTMLDivElement | null>,
  selected: string | null,
  layout: Layout,
  size: Size | null,
): Placement | null {
  const [placement, setPlacement] = useState<Placement | null>(null);
  useLayoutEffect(() => {
    const area = root.current?.querySelector(".structure");
    const slot = selected === null ? null : area?.querySelector(".slot.is-selected .card");
    if (!area || !slot) {
      setPlacement(null);
      return;
    }
    const box = area.getBoundingClientRect();
    const rect = slot.getBoundingClientRect();
    const frame = area.querySelector(".pyramid-frame")?.getBoundingClientRect();
    setPlacement({
      top: frame === undefined ? 0 : frame.top - box.top + area.scrollTop,
      card: {
        left: rect.left - box.left + area.scrollLeft,
        top: rect.top - box.top + area.scrollTop,
        width: rect.width,
        height: rect.height,
      },
      width: area.clientWidth,
      height: area.clientHeight,
    });
  }, [root, selected, layout, size]);
  return placement;
}

/**
 * The selection and its placement, held for the length of the exit animation after it is cleared,
 * so the card can settle back instead of vanishing.
 */
function useLingering(
  selected: string | null,
  placement: Placement | null,
): { component: string; placement: Placement | null } | null {
  const [held, setHeld] = useState<{ component: string; placement: Placement | null } | null>(null);
  const last = useRef<{ component: string; placement: Placement | null } | null>(null);
  useEffect(() => {
    if (selected !== null) {
      last.current = { component: selected, placement };
      setHeld(null);
    } else if (last.current !== null) {
      if (!prefersReducedMotion()) setHeld(last.current);
      last.current = null;
    }
  }, [selected, placement]);
  useEffect(() => {
    if (held === null) return;
    const timer = setTimeout(() => {
      setHeld(null);
    }, MOTION_MS.appear);
    return () => {
      clearTimeout(timer);
    };
  }, [held]);
  return selected === null ? held : { component: selected, placement };
}

function PanelTabs({
  session,
  seats,
  active,
  onPick,
}: {
  session: Session;
  seats: Seats;
  active: Side;
  onPick: (next: Side) => void;
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
    </div>
  );
}
