import { act } from "react";
import { createRoot } from "react-dom/client";
import { afterEach, describe, expect, it, vi } from "vitest";
import { App } from "../../main/ts/App.js";
import { apply, initialModel } from "../../main/ts/model.js";

(globalThis as { IS_REACT_ACT_ENVIRONMENT?: boolean }).IS_REACT_ACT_ENVIRONMENT = true;

const container = document.createElement("div");
const root = createRoot(container);

function show(selfId: string | null, turn: string, onEndTurn = vi.fn()): HTMLButtonElement {
  const model = apply(initialModel(selfId), { type: "view", turn, turnNumber: 2, over: false });
  act(() => {
    root.render(<App model={model} onEndTurn={onEndTurn} />);
  });
  const button = container.querySelector("button");
  if (button === null) throw new Error("no button rendered");
  return button;
}

afterEach(() => {
  act(() => {
    root.render(null);
  });
});

describe("App", () => {
  it("lets the player whose turn it is end it", () => {
    const onEndTurn = vi.fn();
    const button = show("a", "a", onEndTurn);
    expect(container.querySelector("[role=status]")?.textContent).toBe("Your turn.");
    act(() => {
      button.click();
    });
    expect(onEndTurn).toHaveBeenCalledOnce();
  });

  it("disables the button for the opponent and for a spectator", () => {
    expect(show("a", "b").disabled).toBe(true);
    expect(show(null, "a").disabled).toBe(true);
  });
});
