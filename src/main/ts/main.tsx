import { TurnBaseClient } from "@turnbase-platform/gdk";
import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { emptySession, reduce, type SessionInput } from "./core/session.js";
import type { ServerMessage, WireAction } from "./core/wire.js";
import { App } from "./ui/App.js";
import { injectStyles } from "./ui/styles.js";

const client = new TurnBaseClient<WireAction, ServerMessage>();
const mount = document.body.appendChild(document.createElement("div"));
mount.id = "root";
const root = createRoot(mount);
let session = emptySession();

function send(action: WireAction): void {
  client.sendAction(action);
}

function dispatch(input: SessionInput): void {
  session = reduce(session, input);
  root.render(
    <StrictMode>
      <App session={session} send={send} />
    </StrictMode>,
  );
}

injectStyles();
client
  .onInit((context) => {
    dispatch({ type: "init", context });
  })
  .onGameEvent((message) => {
    dispatch({ type: "message", message });
  })
  .onPlayerLeft((player) => {
    dispatch({ type: "playerLeft", player });
  })
  .onGameOver((results) => {
    dispatch({ type: "gameOver", results });
  })
  .start();
