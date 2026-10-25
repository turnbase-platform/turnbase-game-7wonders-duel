import { readFileSync } from "node:fs";
import type { InitContext, Player } from "@turnbase/gdk";
import { act } from "react";
import { createRoot, type Root } from "react-dom/client";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { emptySession, reduce, type Session } from "../../main/ts/core/session.js";
import type { ServerMessage, WireAction } from "../../main/ts/core/wire.js";
import { App } from "../../main/ts/ui/App.js";

(globalThis as { IS_REACT_ACT_ENVIRONMENT?: boolean }).IS_REACT_ACT_ENVIRONMENT = true;

const ONE: Player = { id: "00000000-0000-0000-0000-000000000001", displayName: "One", teamId: "a" };
const TWO: Player = { id: "00000000-0000-0000-0000-000000000002", displayName: "Two", teamId: "b" };

function message(name: string): ServerMessage {
  return JSON.parse(readFileSync(`src/test/resources/wire/${name}.json`, "utf8")) as ServerMessage;
}

const INIT: InitContext = {
  role: "player",
  self: ONE,
  roster: [ONE, TWO],
  teams: [],
  sessionId: "s",
};

/** A seat that has had its resync (the catalog comes only with it) and then these messages. */
function session(self: Player | null, ...messages: ServerMessage[]): Session {
  const context: InitContext = { ...INIT, role: self ? "player" : "spectator", self };
  return [message("resync"), ...messages].reduce(
    (s, m) => reduce(s, { type: "message", message: m }),
    reduce(emptySession(), { type: "init", context }),
  );
}

describe("App", () => {
  let host: HTMLElement;
  let root: Root;
  const send = vi.fn<(action: WireAction) => void>();

  beforeEach(() => {
    host = document.body.appendChild(document.createElement("div"));
    root = createRoot(host);
  });

  afterEach(() => {
    act(() => {
      root.unmount();
    });
    host.remove();
  });

  function show(s: Session): void {
    act(() => {
      root.render(<App session={s} send={send} />);
    });
  }

  it("waits until the first picture arrives", () => {
    show(reduce(emptySession(), { type: "init", context: INIT }));

    expect(host.textContent).toContain("Waiting for the game");
  });

  it("names the seat to act and every player's coins", () => {
    show(session(ONE));

    expect(host.querySelector(".turn")?.textContent).toMatch(/your turn$|to act$/);
    expect(host.querySelector(".header")?.textContent).toContain("One");
  });

  it("sends the matching action when a draft wonder is taken", () => {
    show(session(ONE, message("view-after-draft-pick")));

    const take = [...host.querySelectorAll("button")].find((b) => b.textContent === "Take");
    act(() => {
      take?.click();
    });

    expect(send).toHaveBeenCalledWith(expect.objectContaining({ type: "draftWonder" }));
  });

  it("shows no buttons to a spectator, and says so", () => {
    show(session(null, message("view-after-draft-pick")));

    expect([...host.querySelectorAll("button")].map((b) => b.textContent)).not.toContain("Take");
    expect(host.textContent).toContain("(spectating)");
  });

  it("shows a hidden card as a back, never a name", () => {
    const m = message("view-in-age");
    if (m.view === null) throw new Error("no view");
    show(session(null, { ...m, view: { ...m.view, deciding: null } }));

    expect(host.querySelectorAll(".pyramid .card--back")).toHaveLength(8);
    expect(host.querySelectorAll(".pyramid .card:not(.card--back)")).toHaveLength(12);
  });

  it("shows why an action was refused", () => {
    show(session(ONE, message("view-after-draft-pick"), message("rejected")));

    expect(host.querySelector(".banner")?.textContent).not.toBe("");
  });
});
