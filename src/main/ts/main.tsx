import { TurnBaseClient } from "@turnbase/gdk";
import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { App } from "./App.js";
import {
  apply,
  applyResult,
  initialModel,
  type GameEvent,
  type Model,
  type Move,
} from "./model.js";

const client = new TurnBaseClient<Move, GameEvent>();
const root = createRoot(document.body.appendChild(document.createElement("div")));
let model: Model = initialModel(null);

function draw(): void {
  root.render(
    <StrictMode>
      <App
        model={model}
        onEndTurn={() => {
          client.sendAction({ type: "END_TURN" });
        }}
      />
    </StrictMode>,
  );
}

client
  .onInit((ctx) => {
    model = initialModel(ctx.self?.id ?? null);
    draw();
  })
  .onGameEvent((event) => {
    model = apply(model, event);
    draw();
  })
  .onPlayerLeft((player) => {
    model = { ...model, message: `${player.displayName} left the game.` };
    draw();
  })
  .onGameOver((results) => {
    model = applyResult(model, results);
    draw();
  })
  .start();
