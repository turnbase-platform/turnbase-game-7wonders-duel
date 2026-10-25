import { logLines, type LogLine } from "../core/log.js";
import { offers, type Offer } from "../core/moves.js";
import { seatName, type Session } from "../core/session.js";
import type { WireAction } from "../core/wire.js";
import { groupZones, type ZoneGroup } from "../core/zones.js";

interface Props {
  session: Session;
  send: (action: WireAction) => void;
}

export function App({ session, send }: Props) {
  const { view, catalog } = session;
  if (view === null || catalog === null) return <p>Waiting for the game…</p>;
  const available = offers(session);
  const starters = available.filter((o) => o.action.type === "chooseStarter");
  return (
    <main>
      <Header session={session} />
      {session.rejection !== null && <div className="banner">{session.rejection}</div>}
      {starters.length > 0 && (
        <p>
          Who starts the Age?{" "}
          {starters.map((o) => (
            <OfferButton key={o.target} offer={o} send={send} />
          ))}
        </p>
      )}
      {groupZones(view).map((group) => (
        <Zone
          key={`${group.zone}|${group.key}`}
          group={group}
          session={session}
          offers={available}
          send={send}
        />
      ))}
      <Log session={session} />
    </main>
  );
}

function Header({ session }: { session: Session }) {
  const view = session.view;
  if (view === null) return null;
  return (
    <div className="header">
      <span>{view.phase}</span>
      {view.phase === "AGE" && <span>Age {view.age}</span>}
      <span className="turn">
        {view.phase === "OVER" ? "Game over" : `${seatName(session, view.active)} to act`}
      </span>
      {view.players.map((p) => (
        <span key={p.id}>
          {seatName(session, p.id)}: {p.coins} coins
        </span>
      ))}
      {session.role === "spectator" && <span>(spectating)</span>}
    </div>
  );
}

/** A place's key with seat ids swapped for names, so `…0001:2` reads `One:2`. */
function placeLabel(session: Session, group: ZoneGroup): string {
  let key = group.key;
  for (const p of session.roster) key = key.replaceAll(p.id, p.displayName);
  return key === "" ? group.zone : `${group.zone} · ${key}`;
}

interface ZoneProps {
  group: ZoneGroup;
  session: Session;
  offers: Offer[];
  send: (action: WireAction) => void;
}

function Zone({ group, session, offers: available, send }: ZoneProps) {
  const catalog = session.catalog;
  if (catalog === null) return null;
  return (
    <section className="zone">
      <h3>{placeLabel(session, group)}</h3>
      <ul>
        {group.components.map((id) => (
          <li key={id} className={`chip ${catalog.tag(id, "color") ?? ""}`}>
            {catalog.name(id)}
            {available
              .filter((o) => o.target === id)
              .map((o) => (
                <OfferButton key={o.label} offer={o} send={send} />
              ))}
          </li>
        ))}
        {group.hidden > 0 && <li className="chip hidden">▮ ×{group.hidden}</li>}
      </ul>
    </section>
  );
}

function OfferButton({ offer, send }: { offer: Offer; send: (action: WireAction) => void }) {
  return (
    <button
      type="button"
      onClick={() => {
        send(offer.action);
      }}
    >
      {offer.label}
    </button>
  );
}

function Log({ session }: { session: Session }) {
  const catalog = session.catalog;
  if (catalog === null || session.history.length === 0) return null;
  const who = (id: string) => seatName(session, id);
  return (
    <section className="log">
      <h3>Log</h3>
      {[...session.history].reverse().map((step) => (
        <div key={step.number}>
          <span className="step">#{step.number}</span>
          <Lines lines={logLines(step.events, catalog, who)} />
        </div>
      ))}
    </section>
  );
}

function Lines({ lines }: { lines: LogLine[] }) {
  return (
    <ol>
      {lines.map((line) => (
        <li key={line.id}>
          {line.text}
          {line.children.length > 0 && <Lines lines={line.children} />}
        </li>
      ))}
    </ol>
  );
}
