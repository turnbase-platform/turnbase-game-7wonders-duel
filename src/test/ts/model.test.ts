import { describe, expect, it } from "vitest";
import { apply, applyResult, canAct, initialModel, statusLine } from "../../main/ts/model.js";

const view = (turn: string) => ({ type: "view", turn, turnNumber: 0, over: false }) as const;

describe("model", () => {
  it("starts waiting, with nobody's turn", () => {
    expect(statusLine(initialModel("a"))).toBe("Waiting for the game…");
  });

  it("takes the full picture from the first view, then applies deltas", () => {
    let m = apply(initialModel("a"), view("a"));
    expect(statusLine(m)).toBe("Your turn.");
    m = apply(m, { type: "turn", by: "a", next: "b", turnNumber: 1 });
    expect(m.turnNumber).toBe(1);
    expect(statusLine(m)).toBe("Opponent's turn.");
  });

  it("lets only the player whose turn it is act", () => {
    expect(canAct(apply(initialModel("a"), view("a")))).toBe(true);
    expect(canAct(apply(initialModel("a"), view("b")))).toBe(false);
    expect(canAct(apply(initialModel(null), view("a")))).toBe(false);
    expect(statusLine(apply(initialModel(null), view("a")))).toBe("Spectating.");
  });

  it("shows a refusal", () => {
    const m = apply(apply(initialModel("a"), view("a")), {
      type: "rejected",
      reason: "It is not your turn.",
    });
    expect(statusLine(m)).toBe("It is not your turn.");
  });

  it("reports the result from each side's point of view", () => {
    const draw = applyResult(initialModel("a"), { type: "result", winner: null, reason: "draw" });
    expect(statusLine(draw)).toBe("Draw.");
    const won = applyResult(initialModel("a"), { type: "result", winner: "a", reason: "forfeit" });
    expect(statusLine(won)).toBe("You won.");
    const lost = applyResult(initialModel("b"), { type: "result", winner: "a", reason: "forfeit" });
    expect(statusLine(lost)).toBe("You lost.");
    const watched = applyResult(initialModel(null), { type: "result", winner: "a", reason: "x" });
    expect(statusLine(watched)).toBe("The game is over.");
  });

  it("ends the game without a result for a payload it does not know", () => {
    const m = applyResult(apply(initialModel("a"), view("a")), { nonsense: true });
    expect(m.over).toBe(true);
    expect(m.result).toBeNull();
    expect(statusLine(m)).toBe("Game over.");
    expect(canAct(m)).toBe(false);
  });
});
