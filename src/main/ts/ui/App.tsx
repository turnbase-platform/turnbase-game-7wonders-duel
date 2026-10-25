import type { Session } from "../core/session.js";
import type { WireAction } from "../core/wire.js";
import { Board } from "./Board.js";

interface Props {
  session: Session;
  send: (action: WireAction) => void;
}

export function App({ session, send }: Props) {
  if (session.view === null || session.catalog === null) return <p>Waiting for the game…</p>;
  return <Board session={session} send={send} />;
}
