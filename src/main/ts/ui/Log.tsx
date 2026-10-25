import { logLines, type LogLine } from "../core/log.js";
import { seatName, type Session } from "../core/session.js";

export function Log({ session }: { session: Session }) {
  const catalog = session.catalog;
  if (catalog === null) return null;
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
