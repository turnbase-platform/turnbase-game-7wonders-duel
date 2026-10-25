import { readFileSync } from "node:fs";
import type { InitContext, Player } from "@turnbase/gdk";
import { act } from "react";
import { createRoot, type Root } from "react-dom/client";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import type { Layout } from "../../main/ts/core/layout.js";
import { emptySession, reduce, type Session } from "../../main/ts/core/session.js";
import type { EventView, ServerMessage, WireAction } from "../../main/ts/core/wire.js";
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

  it("flanks the structure with both cities and keeps the log behind a button when wide", () => {
    show(age(), "wide");

    expect(tabs()).toEqual([]);
    expect([...host.querySelectorAll(".city h3")].map((h) => h.textContent)).toEqual([
      "You",
      "Opponent",
    ]);
    expect(host.querySelector(".structure")).not.toBeNull();
    expect(host.querySelector(".header")).toBeNull();
    expect(host.querySelector(".log")).toBeNull();
    click(host.querySelector(".logbtn"));
    expect(host.querySelector(".drawer .log")).not.toBeNull();
    click(host.querySelector(".drawer__close"));
    expect(host.querySelector(".drawer")).toBeNull();
  });

  it("names the cities after the seats for a spectator", () => {
    show(session(null, inAge()), "wide");

    expect([...host.querySelectorAll(".city h3")].map((h) => h.textContent)).toEqual([
      "One",
      "Two",
    ]);
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

  describe("acting on a card", () => {
    const palisade = (): Element | null => host.querySelector('button.slot[aria-label="Palisade"]');
    const dialog = (): HTMLElement | null => host.querySelector('[role="dialog"]');
    const choices = (): string[] =>
      [...(dialog()?.querySelectorAll("button") ?? [])].map((b) => b.textContent);
    const button = (label: string): Element | undefined =>
      [...(dialog()?.querySelectorAll("button") ?? [])].find((b) =>
        b.textContent.startsWith(label),
      );

    /** Seat 2 can pay for these Wonders as well as the cards its quotes already cover. */
    function withAffordable(...wonders: string[]): Session {
      const m = inAge();
      if (m.view === null) throw new Error("no view");
      const quotes = m.view.quotes.map((q) =>
        wonders.includes(q.component) ? { ...q, affordable: true } : q,
      );
      return session(TWO, { ...m, view: { ...m.view, quotes } });
    }

    beforeEach(() => {
      send.mockClear();
    });

    it("opens the actions with prices when a card is selected", () => {
      show(age());
      click(palisade());

      expect(dialog()).not.toBeNull();
      expect(choices()).toHaveLength(3);
      expect(button("Build")?.textContent).toContain("2 coins");
    });

    it("builds the card and lets the selection go", () => {
      show(age());
      click(palisade());
      click(button("Build"));

      expect(send).toHaveBeenCalledWith({ type: "build", component: "palisade" });
      expect(dialog()).toBeNull();
    });

    it("discards the card", () => {
      show(age());
      click(palisade());
      click(button("Discard"));

      expect(send).toHaveBeenCalledWith({ type: "discard", component: "palisade" });
    });

    it("disables Build with the reason when the card cannot be paid for", () => {
      const m = inAge();
      if (m.view === null) throw new Error("no view");
      const quotes = m.view.quotes.map((q) => ({ ...q, affordable: false }));
      show(session(TWO, { ...m, view: { ...m.view, quotes } }));
      click(palisade());

      const build = button("Build") as HTMLButtonElement;
      expect(build.disabled).toBe(true);
      expect(build.textContent).toContain("you have");
      click(build);
      expect(send).not.toHaveBeenCalled();
    });

    it("chooses the Wonder in a second step, and only affordable ones can be picked", () => {
      show(withAffordable("piraeus"));
      click(palisade());
      click(button("Wonder"));

      const wonders = [...(dialog()?.querySelectorAll(".actions__wonders button") ?? [])];
      expect(wonders).toHaveLength(4);
      expect(wonders.filter((b) => !(b as HTMLButtonElement).disabled)).toHaveLength(1);
      click(wonders.find((b) => !(b as HTMLButtonElement).disabled));
      expect(send).toHaveBeenCalledWith({
        type: "buildWonder",
        component: "palisade",
        wonder: "piraeus",
      });
    });

    it("goes back from the Wonder step to the actions", () => {
      show(withAffordable("piraeus"));
      click(palisade());
      click(button("Wonder"));
      click(button("Back"));

      expect(button("Build")).toBeDefined();
    });

    it("disables Wonder when none can be paid for", () => {
      show(age());
      click(palisade());

      expect((button("Wonder") as HTMLButtonElement).disabled).toBe(true);
    });

    it("closes on Escape and returns focus to the card", () => {
      show(age());
      (palisade() as HTMLElement).focus();
      click(palisade());
      act(() => {
        dialog()?.dispatchEvent(new KeyboardEvent("keydown", { key: "Escape", bubbles: true }));
      });

      expect(dialog()).toBeNull();
      expect(document.activeElement).toBe(palisade());
    });

    it("lifts the card the same way on a phone", () => {
      show(age(), "portrait");
      click(palisade());

      expect(dialog()?.classList.contains("spotlight")).toBe(true);
    });
  });

  describe("cues in the header", () => {
    const event = (over: Partial<EventView> & Pick<EventView, "kind">): EventView => ({
      id: 1,
      parent: -1,
      cause: null,
      component: null,
      from: null,
      to: null,
      player: null,
      amount: 0,
      pawnFrom: null,
      pawnTo: null,
      ...over,
    });
    /** The Age in play, then one more accepted action that brought these events. */
    const after = (...events: EventView[]): Session =>
      reduce(age(), { type: "message", message: { ...inAge(), events } });
    const purse = (p: Player): Element | undefined =>
      [...host.querySelectorAll(".purse")].find((e) => e.textContent.startsWith(p.displayName));

    it("floats the coins a seat gained or lost off its own purse", () => {
      show(after(event({ kind: "coins", player: TWO.id, amount: -2 })), "medium");

      expect(purse(TWO)?.querySelector(".float.is-loss")?.textContent).toBe("−2");
      expect(purse(ONE)?.querySelector(".float")).toBeNull();
    });

    it("shows a gain with a plus", () => {
      show(after(event({ kind: "coins", player: ONE.id, amount: 3 })), "medium");

      expect(purse(ONE)?.querySelector(".float.is-gain")?.textContent).toBe("+3");
    });

    it("floats them off the coins in the city when wide", () => {
      show(after(event({ kind: "coins", player: TWO.id, amount: -2 })));

      expect([...host.querySelectorAll(".city .purse .float")].map((f) => f.textContent)).toEqual([
        "−2",
      ]);
    });

    it("pulses the turn marker when the seat plays again", () => {
      show(after(event({ kind: "playAgainGranted", player: TWO.id })));

      expect(host.querySelector(".turn.is-pulse")).not.toBeNull();
    });

    it("shows no cue when nothing happened", () => {
      show(age());

      expect(host.querySelector(".float, .is-pulse")).toBeNull();
    });
  });

  describe("the city", () => {
    const late = (self: Player): Session => session(self, message("view-with-decision"));

    it("draws one stack per colour the city has, with the Wonders and the coins", () => {
      show(late(ONE));

      const mine = host.querySelector(".city.owner-me");
      expect(mine?.querySelectorAll(".stack")).toHaveLength(7);
      expect(mine?.querySelectorAll(".wonders .card--wonder.is-built")).toHaveLength(3);
      expect(mine?.querySelector(".purse")?.textContent).toContain("3");
    });

    it("shows the other city on its tab", () => {
      show(late(ONE), "medium");
      expect(host.querySelector(".city.owner-opp")).toBeNull();
      click([...host.querySelectorAll('[role="tab"]')][0]);

      expect(host.querySelector(".city.owner-opp")).not.toBeNull();
    });

    it("shows the large card for a stacked card the viewer points at", () => {
      show(late(ONE));
      act(() => {
        host.querySelector<HTMLElement>(".stack .peekable")?.focus();
      });

      expect(host.querySelector(".peek .card--large")).not.toBeNull();
    });
  });
});
