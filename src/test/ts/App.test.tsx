import { readFileSync } from "node:fs";
import type { InitContext, Player } from "@turnbase/gdk";
import { act } from "react";
import { createRoot, type Root } from "react-dom/client";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { emptySession, reduce, type Session } from "../../main/ts/core/session.js";
import type { ServerMessage, WireAction } from "../../main/ts/core/wire.js";
import { App } from "../../main/ts/ui/App.js";
import { MOTION_MS } from "../../main/ts/ui/motion.js";

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

  describe("the pop-up of who starts", () => {
    beforeEach(() => {
      vi.useFakeTimers();
    });
    afterEach(() => {
      vi.useRealTimers();
    });

    it("names the starter of a fresh draft, to the starter as 'You', then leaves by itself", () => {
      show(session(TWO));
      expect(host.querySelector(".starter")?.textContent).toContain("You start");

      act(() => {
        vi.advanceTimersByTime(MOTION_MS.starter);
      });
      expect(host.querySelector(".starter")).toBeNull();
    });

    it("names the starter to the other seat and to a spectator", () => {
      show(session(ONE));
      expect(host.querySelector(".starter")?.textContent).toContain("Two starts");
    });

    it("is not shown to a client that arrives after the draft began", () => {
      show(session(ONE, message("view-after-draft-pick")));
      expect(host.querySelector(".starter")).toBeNull();
    });
  });

  it("names the seat to act and every player's coins", () => {
    show(session(ONE));

    expect(host.querySelectorAll(".city .city__turn")).toHaveLength(1);
    expect(host.querySelector(".city.owner-me .purse")?.textContent).toMatch(/\d/);
    expect(host.querySelector(".city.owner-opp .purse")?.textContent).toMatch(/\d/);
  });

  describe("the Wonder draft", () => {
    const buttons = (): HTMLButtonElement[] => [...host.querySelectorAll("button")];
    const confirm = (): HTMLButtonElement | undefined =>
      buttons().find((b) => b.textContent.startsWith("Confirm"));
    const options = (): HTMLButtonElement[] => [
      ...host.querySelectorAll<HTMLButtonElement>(".draft__option"),
    ];

    it("sends nothing when a Wonder is only selected, and offers to confirm it", () => {
      show(session(ONE, message("view-after-draft-pick")));
      expect(confirm()?.disabled).toBe(true);

      act(() => {
        options()[0]?.click();
      });

      expect(options()[0]?.getAttribute("aria-pressed")).toBe("true");
      expect(confirm()?.disabled).toBe(false);
      expect(send).not.toHaveBeenCalled();
    });

    it("sends the matching action once the selection is confirmed", () => {
      show(session(ONE, message("view-after-draft-pick")));

      act(() => {
        options()[1]?.click();
      });
      act(() => {
        confirm()?.click();
      });

      expect(send).toHaveBeenCalledTimes(1);
      expect(send).toHaveBeenCalledWith(expect.objectContaining({ type: "draftWonder" }));
    });

    it("moves the selection to the Wonder clicked last, and lets Escape drop it", () => {
      show(session(ONE, message("view-after-draft-pick")));

      act(() => {
        options()[0]?.click();
      });
      act(() => {
        options()[1]?.click();
      });
      expect(options().map((o) => o.getAttribute("aria-pressed"))).toEqual([
        "false",
        "true",
        "false",
      ]);

      act(() => {
        options()[1]?.dispatchEvent(new KeyboardEvent("keydown", { key: "Escape", bubbles: true }));
      });
      expect(confirm()?.disabled).toBe(true);
    });
  });

  it("shows no buttons to a spectator, and says so", () => {
    show(session(null, message("view-after-draft-pick")));

    expect(host.querySelector(".draft__option")).toBeNull();
    expect([...host.querySelectorAll("button")].map((b) => b.textContent)).not.toContain(
      expect.stringMatching(/^Confirm/),
    );
    expect([...host.querySelectorAll(".city__head h3")].map((h) => h.textContent)).toEqual([
      "One",
      "Two",
    ]);
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
