/** The action this game sends. Mirrors the backend's `Action`. */
export interface Move {
  type: "END_TURN";
}

/** The events the backend sends. `type` discriminates them (backend `GameMessage`). */
export type GameEvent =
  | { type: "view"; turn: string; turnNumber: number; over: boolean }
  | { type: "turn"; by: string; next: string; turnNumber: number }
  | { type: "rejected"; reason: string };

/** The final result, sent with the game-over effect (backend `GameMessage.Result`). */
export interface GameResult {
  type: "result";
  /** The winner's player id, or null for a draw. */
  winner: string | null;
  /** "draw" or "forfeit". */
  reason: string;
}

/** What the screen shows. */
export interface Model {
  turn: string | null;
  turnNumber: number;
  over: boolean;
  /** The player being addressed by this client; null for a spectator. */
  selfId: string | null;
  message: string;
  /** Set once the game-over results arrive. */
  result: GameResult | null;
}

export function initialModel(selfId: string | null): Model {
  return {
    turn: null,
    turnNumber: 0,
    over: false,
    selfId,
    message: "Waiting for the game…",
    result: null,
  };
}

/** Pure reducer: the first event after `init` is always a full `view`, then deltas. */
export function apply(model: Model, event: GameEvent): Model {
  switch (event.type) {
    case "view":
      return {
        ...model,
        turn: event.turn,
        turnNumber: event.turnNumber,
        over: event.over,
        message: "",
      };
    case "turn":
      return { ...model, turn: event.next, turnNumber: event.turnNumber, message: "" };
    case "rejected":
      return { ...model, message: event.reason };
  }
}

function isResult(value: unknown): value is GameResult {
  if (typeof value !== "object" || value === null) return false;
  const r = value as Record<string, unknown>;
  return (
    r.type === "result" &&
    (r.winner === null || typeof r.winner === "string") &&
    typeof r.reason === "string"
  );
}

/** Pure reducer for the opaque game-over payload; one that is not a `result` only ends the game. */
export function applyResult(model: Model, results: unknown): Model {
  return { ...model, over: true, message: "", result: isResult(results) ? results : null };
}

/** True when this client may act right now. */
export function canAct(model: Model): boolean {
  return !model.over && model.selfId !== null && model.turn === model.selfId;
}

export function statusLine(model: Model): string {
  if (model.over && model.result !== null) {
    if (model.result.winner === null) return "Draw.";
    if (model.selfId === null) return "The game is over.";
    return model.result.winner === model.selfId ? "You won." : "You lost.";
  }
  if (model.message !== "") return model.message;
  if (model.turn === null) return "Waiting for the game…";
  if (model.over) return "Game over.";
  if (model.selfId === null) return "Spectating.";
  return model.turn === model.selfId ? "Your turn." : "Opponent's turn.";
}
