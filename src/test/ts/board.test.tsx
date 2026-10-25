import { readFileSync } from "node:fs";
import type { InitContext, Player } from "@turnbase/gdk";
import { act } from "react";
import { createRoot, type Root } from "react-dom/client";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import type { Layout } from "../../main/ts/core/layout.js";
import { emptySession, reduce, type Session } from "../../main/ts/core/session.js";
import type { ServerMessage, WireAction } from "../../main/ts/core/wire.js";
import { Board } from "../../main/ts/ui/Board.js";

(globalThis as { IS_REACT_ACT_ENVIRONMENT?: boolean }).IS_REACT_ACT_ENVIRONMENT = true;

const ONE: Player = { id: "00000000-0000-0000-0000-000000000001", displayName: "One", teamId: "a" };
const TWO: Player = { id: "00000000-0000-0000-0000-000000000002", displayName: "Two", teamId: "b" };

function message(name: string): ServerMessage {
  return JSON.parse(readFileSync(`src/test/resources/wire/${name}.json`, "utf8")) as ServerMessage;
}

function session(self: Player | null, ...messages: ServerMessage[]): Session {
  const context: InitContext = {
    role: self ? "player" : "spectator",
    self,
    roster: [ONE, TWO],
    teams: [],
    sessionId: "s",
  };
  return [message("resync"), ...messages].reduce(
    (s, m) => reduce(s, { type: "message", message: m }),
    reduce(emptySession(), { type: "init", context }),
  );
}

describe("Board", () => {
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

  function show(s: Session, layout: Layout = "wide"): void {
    act(() => {
      root.render(<Board session={s} send={send} layout={layout} />);
    });
  }

  const tabs = (): string[] => [...host.querySelectorAll('[role="tab"]')].map((t) => t.textContent);
  const click = (el: Element | null | undefined): void => {
    act(() => {
      (el as HTMLElement).click();
    });
  };

  /** Age I in play, the starter already chosen. Seat 2 acts, so the viewer is seat 2. */
  function inAge(): ServerMessage {
    const m = message("view-in-age");
    if (m.view === null) throw new Error("no view");
    return { ...m, view: { ...m.view, deciding: null } };
  }
  const age = (): Session => session(TWO, inAge());

  it("makes only the cards that have a quote for the viewer interactive", () => {
    show(age());

    const buttons = host.querySelectorAll("button.slot");
    expect(buttons.length).toBeGreaterThan(0);
    expect(buttons.length).toBeLessThan(12);
    expect(host.querySelectorAll(".slot.is-covered").length).toBeGreaterThan(0);
  });

  it("selects a takeable card, and again lets it go", () => {
    show(age());

    click(host.querySelector("button.slot"));
    expect(host.querySelector(".slot.is-selected")).not.toBeNull();
    click(host.querySelector("button.slot"));
    expect(host.querySelector(".slot.is-selected")).toBeNull();
    expect(send).not.toHaveBeenCalled();
  });

  it("gives a spectator the same pyramid with nothing to click", () => {
    show(session(null, inAge()));

    expect(host.querySelectorAll("button.slot")).toHaveLength(0);
    expect(host.querySelectorAll(".pyramid .card")).toHaveLength(20);
  });

  it("shows both cities beside the structure and the log under them when wide", () => {
    show(age(), "wide");

    expect(tabs()).toEqual(["Opponent", "You"]);
    expect(host.querySelector(".structure")).not.toBeNull();
    expect(host.querySelector(".log")).not.toBeNull();
  });

  it("names the cities after the seats for a spectator", () => {
    show(session(null, inAge()), "wide");

    expect(tabs()).toEqual(["Two", "One"]);
  });

  it.each(["medium", "landscape"] as const)("puts the log among the tabs when %s", (layout) => {
    show(age(), layout);

    expect(tabs()).toEqual(["Opponent", "You", "Log"]);
    expect(host.querySelector(".log")).toBeNull();
    click([...host.querySelectorAll('[role="tab"]')][2]);
    expect(host.querySelector(".log")).not.toBeNull();
  });

  describe("on a phone in portrait", () => {
    it("shows one pane at a time behind Structure · Cities · Log, header and strip always", () => {
      show(age(), "portrait");
      expect(tabs()).toEqual(["Structure", "Cities", "Log"]);
      expect(host.querySelector(".structure")).not.toBeNull();
      expect(host.querySelector(".city")).toBeNull();

      click([...host.querySelectorAll(".bottom-tabs .tab")][1]);
      expect(host.querySelector(".structure")).toBeNull();
      expect(host.querySelector(".city")).not.toBeNull();
      expect(host.querySelector(".header")).not.toBeNull();
      expect(host.querySelector(".strip")).not.toBeNull();
    });

    it("highlights the Structure tab when the viewer must act there, and never switches to it", () => {
      show(age(), "portrait");
      click([...host.querySelectorAll(".bottom-tabs .tab")][1]);

      const structure = host.querySelectorAll(".bottom-tabs .tab")[0];
      expect(structure.classList.contains("needs-you")).toBe(true);
      expect(host.querySelector(".city")).not.toBeNull();
    });

    it("does not highlight anything when it is the other seat's turn", () => {
      show(session(ONE, inAge()), "portrait");
      click([...host.querySelectorAll(".bottom-tabs .tab")][1]);

      expect(host.querySelector(".needs-you")).toBeNull();
    });
  });

  it("asks the deciding seat for the starter and tells the other to wait", () => {
    const view = inAge();
    if (view.view === null) throw new Error("no view");
    const deciding = { ...view, view: { ...view.view, deciding: TWO.id } };

    show(session(TWO, deciding));
    expect(host.textContent).toContain("Who starts the Age?");
    expect([...host.querySelectorAll(".choices button")].map((b) => b.textContent)).toEqual([
      "One starts",
      "Two starts",
    ]);

    show(session(ONE, deciding));
    expect(host.textContent).toContain("Waiting for Two to choose");
  });

  it("shows the Wonder draft as cards during the draft", () => {
    show(session(ONE));

    expect(host.querySelectorAll(".choice .card--wonder")).toHaveLength(4);
    expect(host.querySelector(".pyramid")).toBeNull();
  });
});
