import { readFileSync } from "node:fs";
import type { InitContext, Player } from "@turnbase/gdk";
import { act } from "react";
import { createRoot, type Root } from "react-dom/client";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import type { Layout } from "../../main/ts/core/layout.js";
import { emptySession, reduce, type Session } from "../../main/ts/core/session.js";
import type { EventView, ServerMessage, WireAction } from "../../main/ts/core/wire.js";
import { MOTION_MS } from "../../main/ts/ui/motion.js";
import { Board } from "../../main/ts/ui/Board.js";
import { QUOTE_DELAY_MS } from "../../main/ts/ui/QuoteTable.js";

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

/** Lets the card finish settling back down. */
const exited = (): Promise<void> =>
  act(async () => {
    await new Promise((done) => setTimeout(done, MOTION_MS.appear + 50));
  });

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

  /** Hovers `target` and returns the quote that floats up for it, if any. */
  const hover = (target: Element | null): Element | null => {
    vi.useFakeTimers();
    act(() => {
      target?.dispatchEvent(new Event("pointerenter"));
    });
    act(() => {
      vi.advanceTimersByTime(QUOTE_DELAY_MS);
    });
    vi.useRealTimers();
    return document.body.querySelector(".slot__quote");
  };

  it("waits for the pointer to rest before quoting", () => {
    show(age());

    vi.useFakeTimers();
    act(() => {
      host.querySelector("div.slot.is-covered")?.dispatchEvent(new Event("pointerenter"));
      vi.advanceTimersByTime(QUOTE_DELAY_MS - 1);
    });
    const early = document.body.querySelector(".slot__quote");
    vi.useRealTimers();
    expect(early).toBeNull();
  });

  it("quotes the price of a covered card on hover", () => {
    show(age());

    const covered = host.querySelector("div.slot.is-covered");
    expect(host.querySelectorAll(".slot__quote")).toHaveLength(0);
    expect(hover(covered)?.querySelector(".quote [role='row']")).not.toBeNull();
  });

  it("quotes an unbuilt Wonder on hover in its city", () => {
    show(age());

    const wonder = host.querySelector(".wonders .peekable");
    expect(hover(wonder)?.textContent).toMatch(/Total|Free by chain/);
  });

  it("lays a quote out per resource, with the card coins and the total", () => {
    const m = inAge();
    if (m.view === null) throw new Error("no view");
    const quotes = m.view.quotes.map((q) =>
      q.player === TWO.id && q.component === "palisade"
        ? {
            ...q,
            coins: 2,
            tradeCost: 3,
            total: 5,
            lines: [
              { resource: "wood", needed: 2, produced: 1, discounted: 0, bought: 1, unitPrice: 3 },
            ],
          }
        : q,
    );
    show(session(TWO, { ...m, view: { ...m.view, quotes } }));

    const table = hover(host.querySelector('button[aria-label="Palisade"]'))?.querySelector(
      ".quote",
    );
    const cells = [...(table?.querySelectorAll(".quote__line [role='cell']") ?? [])];
    expect(cells.slice(0, 5).map((c) => c.textContent)).toEqual(["2", "1", "0", "1", "3"]);
    expect(cells.slice(5).map((c) => c.textContent)).toEqual(["2", "–", "–", "–", "2"]);
    expect(table?.querySelector(".quote__line svg.icon")).not.toBeNull();
    const sums = [...(table?.querySelectorAll(".quote__sum") ?? [])].map((r) => r.textContent);
    expect(sums).toEqual(["Total5"]);
  });

  it("leaves the coin row out of a quote whose card costs no coins", () => {
    const m = inAge();
    if (m.view === null) throw new Error("no view");
    const quotes = m.view.quotes.map((q) =>
      q.player === TWO.id && q.component === "palisade"
        ? {
            ...q,
            coins: 0,
            tradeCost: 0,
            total: 0,
            lines: [
              { resource: "wood", needed: 1, produced: 1, discounted: 0, bought: 0, unitPrice: 2 },
            ],
          }
        : q,
    );
    show(session(TWO, { ...m, view: { ...m.view, quotes } }));

    const table = hover(host.querySelector('button[aria-label="Palisade"]'))?.querySelector(
      ".quote",
    );
    expect(table?.querySelectorAll(".quote__line")).toHaveLength(1);
    expect(table?.querySelector('[title="coins"]')).toBeNull();
    expect(table?.querySelector(".quote__total")?.textContent).toBe("Total0");
  });

  it("gives a coin-only quote the same table: head, coin row and total", () => {
    const m = inAge();
    if (m.view === null) throw new Error("no view");
    const quotes = m.view.quotes.map((q) =>
      q.player === TWO.id && q.component === "palisade"
        ? { ...q, coins: 2, tradeCost: 0, total: 2, lines: [] }
        : q,
    );
    show(session(TWO, { ...m, view: { ...m.view, quotes } }));

    const table = hover(host.querySelector('button[aria-label="Palisade"]'))?.querySelector(
      ".quote",
    );
    expect(table?.querySelector(".quote__head")).not.toBeNull();
    expect(table?.querySelectorAll(".quote__line")).toHaveLength(1);
    expect(table?.querySelector(".quote__total")?.textContent).toBe("Total2");
  });

  it("shows no quote for a free card", () => {
    const m = inAge();
    if (m.view === null) throw new Error("no view");
    const quotes = m.view.quotes.map((q) =>
      q.player === TWO.id && q.component === "palisade"
        ? { ...q, coins: 0, tradeCost: 0, total: 0, lines: [], chained: false }
        : q,
    );
    show(session(TWO, { ...m, view: { ...m.view, quotes } }));

    expect(hover(host.querySelector('button[aria-label="Palisade"]'))).toBeNull();
  });

  it("tells what the opponent would pay in coins only, without a table", () => {
    show(age());

    const shown = hover(host.querySelector("button.slot"));
    const rival = shown?.querySelector(".quote--brief");
    expect(rival?.textContent).toMatch(/^Opponent pays: (\d+ coins|free by chain)$/);
    expect(shown?.querySelectorAll(".quote")).toHaveLength(2);
  });

  it("shows an opponent's Wonder as its price in coins only", () => {
    show(age());

    const cell = host.querySelector(".owner-opp .wonders .peekable:not(:has(.is-built))");
    const shown = hover(cell);
    expect(shown?.textContent).toMatch(/^Opponent pays: (\d+ coins|free by chain)$/);
    expect(shown?.querySelector("[role='columnheader']")).toBeNull();
  });

  it("selects a takeable card, and again lets it go", () => {
    show(age());

    click(host.querySelector("button.slot"));
    expect(host.querySelector(".slot.is-selected")).not.toBeNull();
    click(host.querySelector("button.slot"));
    expect(host.querySelector(".slot.is-selected")).toBeNull();
    expect(send).not.toHaveBeenCalled();
  });

  describe("a pending decision", () => {
    const withPending = (decider: Player, self: Player | null, options: string[], hidden = 0) => {
      const m = inAge();
      if (m.view === null) throw new Error("no view");
      return session(self, {
        ...m,
        view: { ...m.view, pending: { decider: decider.id, options, hidden } },
      });
    };

    beforeEach(() => {
      send.mockClear();
    });

    it("offers the decider each option to choose, and sends the answer", () => {
      show(withPending(TWO, TWO, ["palisade"]));

      const prompt = host.querySelector(".prompt");
      expect(prompt?.querySelectorAll(".card")).toHaveLength(1);
      click(
        [...(prompt?.querySelectorAll("button") ?? [])].find((b) => b.textContent === "Choose"),
      );
      expect(send).toHaveBeenCalledWith({ type: "answerComponent", component: "palisade" });
    });

    it("tells the others who is choosing, without buttons, and shows hidden options as unknown", () => {
      show(withPending(TWO, ONE, [], 3));

      const prompt = host.querySelector(".prompt");
      expect(prompt?.textContent).toContain("Waiting for Two to choose");
      expect(prompt?.querySelectorAll("button")).toHaveLength(0);
      expect(prompt?.querySelectorAll(".choice--hidden")).toHaveLength(3);
    });

    it("offers seats by name when the options are players", () => {
      show(withPending(TWO, TWO, [ONE.id, TWO.id]));

      const names = [...host.querySelectorAll(".prompt button")].map((b) => b.textContent);
      expect(names).toEqual(["One", "Two"]);
    });

    describe("picking a token lying on the board", () => {
      const onBoard = (): string => {
        const m = inAge();
        const token = m.view?.visible.find((p) => p.zone === "board");
        if (!token) throw new Error("no token on the board");
        return token.component;
      };

      it("makes the board's own token the button, with no panel of copies", () => {
        const id = onBoard();
        show(withPending(TWO, TWO, [id]));

        expect(host.querySelector(".prompt")).toBeNull();
        click(host.querySelector(".board-tokens button.token-pick"));
        expect(send).toHaveBeenCalledWith({ type: "answerComponent", component: id });
      });

      it("tells the others who is choosing, and offers them nothing to press", () => {
        show(withPending(TWO, ONE, [onBoard()]));

        expect(host.querySelector(".board-tokens")?.textContent).toContain(
          "Waiting for Two to choose",
        );
        expect(host.querySelector(".board-tokens button")).toBeNull();
      });
    });

    it("keeps the pyramid in view beneath the prompt", () => {
      show(withPending(TWO, TWO, ["palisade"]));

      expect(host.querySelector(".pyramid")).not.toBeNull();
    });
  });

  it("shows each city's science symbols, lit when held", () => {
    show(age());

    const symbols = host.querySelectorAll(".city .science__symbol");
    expect(symbols.length).toBeGreaterThan(0);
    expect(symbols.length % 7).toBe(0);
  });

  describe("a finished game", () => {
    const over = (self: Player | null, winner: Player | null, reason: "MILITARY" | "POINTS") => {
      const m = inAge();
      if (m.view === null) throw new Error("no view");
      return session(self, {
        ...m,
        view: {
          ...m.view,
          phase: "OVER",
          victoryWinner: winner?.id ?? null,
          victoryReason: reason,
        },
      });
    };
    const outcome = (): string => host.querySelector(".outcome")?.textContent ?? "";

    it("says the viewer won, and how", () => {
      show(over(TWO, TWO, "MILITARY"));
      expect(outcome()).toBe("You winby Military Supremacy");
    });

    it("names the winner to the loser and to a spectator", () => {
      show(over(ONE, TWO, "POINTS"));
      expect(outcome()).toContain("Two wins");
      show(over(null, TWO, "POINTS"));
      expect(outcome()).toContain("Two wins");
    });

    it("calls it a draw without a winner, and shows nothing while the game runs", () => {
      show(over(ONE, null, "POINTS"));
      expect(outcome()).toContain("A draw");
      show(age());
      expect(host.querySelector(".outcome")).toBeNull();
    });
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

  it("opens the discard pile from its button on every layout, one drawer at a time", () => {
    const m = inAge();
    if (m.view === null) throw new Error("no view");
    const thrown = m.view.visible.find((p) => p.zone === "structure.up");
    if (thrown === undefined) throw new Error("no card to throw");
    const view = {
      ...m.view,
      visible: m.view.visible.map((p) => (p === thrown ? { ...p, zone: "discard", key: "" } : p)),
    };
    show(session(TWO, { ...m, view }), "portrait");

    expect(host.querySelector(".pilebtn")?.textContent).toContain("1");
    click(host.querySelector(".pilebtn"));
    expect(host.querySelectorAll('.drawer[aria-label="Discard pile"] .pile .card')).toHaveLength(1);
    click(host.querySelector(".drawer__close"));
    expect(host.querySelector(".drawer")).toBeNull();
  });

  it("says so when nothing has been discarded", () => {
    show(age(), "wide");

    click(host.querySelector(".pilebtn"));
    expect(host.querySelector(".pile__empty")?.textContent).toBe("Nothing discarded yet.");
    click(host.querySelector(".logbtn"));
    expect(host.querySelector(".pile")).toBeNull();
    expect(host.querySelector(".drawer .log")).not.toBeNull();
  });

  it("names the cities after the seats for a spectator", () => {
    show(session(null, inAge()), "wide");

    expect([...host.querySelectorAll(".city h3")].map((h) => h.textContent)).toEqual([
      "One",
      "Two",
    ]);
  });

  it.each(["medium", "landscape"] as const)(
    "keeps the log out of the city panel, behind its button, when %s",
    (layout) => {
      show(age(), layout);

      expect(tabs()).toEqual(["Opponent", "You"]);
      expect(host.querySelector(".log")).toBeNull();
      click(host.querySelector(".logbtn"));
      expect(host.querySelector(".drawer .log")).not.toBeNull();
    },
  );

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

  it("lays the Age's pyramid out under the starter question, since the choice rests on it", () => {
    const view = inAge();
    if (view.view === null) throw new Error("no view");
    const deciding = { ...view, view: { ...view.view, deciding: TWO.id } };

    show(session(TWO, deciding));

    expect(host.querySelector(".pyramid")).not.toBeNull();
    expect(host.querySelectorAll(".pyramid .card").length).toBeGreaterThan(0);
    expect(host.querySelectorAll(".pyramid button:not(:disabled)")).toHaveLength(0);
    expect(host.textContent).toContain("Who starts the Age?");
  });

  it("shows the Wonder draft as cards during the draft", () => {
    show(session(ONE));

    expect(host.querySelectorAll(".draft .card--wonder")).toHaveLength(4);
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

    it("builds the card and lets the selection go", async () => {
      show(age());
      click(palisade());
      click(button("Build"));

      expect(send).toHaveBeenCalledWith({ type: "build", component: "palisade" });
      expect(dialog()?.classList.contains("is-leaving")).toBe(true);
      await exited();
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

    it("closes on Escape and returns focus to the card", async () => {
      show(age());
      (palisade() as HTMLElement).focus();
      click(palisade());
      act(() => {
        dialog()?.dispatchEvent(new KeyboardEvent("keydown", { key: "Escape", bubbles: true }));
      });

      await exited();
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

    it("floats the coins a seat gained or lost off its own purse", () => {
      show(after(event({ kind: "coins", player: TWO.id, amount: -2 })), "medium");

      const loss = document.body.querySelector(".coinfx.is-loss");
      expect(loss?.getAttribute("aria-label")).toBe("−2 coins");
      expect(loss?.querySelectorAll(".coinfx__coin")).toHaveLength(2);
      expect(document.body.querySelectorAll(".coinfx")).toHaveLength(1);
    });

    it("spawns as many small coins as were gained", () => {
      show(after(event({ kind: "coins", player: ONE.id, amount: 3 })), "medium");

      const gain = document.body.querySelector(".coinfx.is-gain");
      expect(gain?.getAttribute("aria-label")).toBe("+3 coins");
      expect(gain?.querySelectorAll(".coinfx__coin")).toHaveLength(3);
    });

    it("plays them on the coin in the city when wide", () => {
      show(after(event({ kind: "coins", player: TWO.id, amount: -2 })));

      expect(
        [...document.body.querySelectorAll(".coinfx")].map((f) => f.getAttribute("aria-label")),
      ).toEqual(["−2 coins"]);
    });

    it("pulses the turn marker when the seat plays again", () => {
      show(after(event({ kind: "playAgainGranted", player: TWO.id })));

      expect(host.querySelector(".turn.is-pulse")).not.toBeNull();
    });

    it("shows no cue when nothing happened", () => {
      show(age());

      expect(document.body.querySelector(".coinfx, .is-pulse")).toBeNull();
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

    it("tucks the card that built a Wonder under it, face down", () => {
      show(late(ONE));

      const mine = host.querySelector(".city.owner-me");
      expect(mine?.querySelectorAll(".wonders .wonder__under .card--back")).toHaveLength(3);
      for (const slot of mine?.querySelectorAll(".wonders .wonder") ?? []) {
        expect(slot.querySelector(".wonder__under") !== null).toBe(
          slot.querySelector(".card--wonder")?.classList.contains("is-built"),
        );
      }
    });

    it("colours the tucked card by the back group of the card that built the Wonder", () => {
      show(late(ONE));

      const backs = [...host.querySelectorAll(".city.owner-me .wonder__under .card--back")];
      expect(backs).toHaveLength(3);
      for (const back of backs) expect(back.className).not.toContain("back-none");
    });

    it("shows a built Wonder as built when it is pointed at", () => {
      show(late(ONE));
      act(() => {
        host.querySelector<HTMLElement>(".city.owner-me .wonders .peekable")?.focus();
      });

      expect(host.querySelector(".peek .wonder__under")).not.toBeNull();
      expect(host.querySelector(".peek .card--wonder.is-built")).not.toBeNull();
    });

    it("says in a floating note what a progress token does", () => {
      show(late(ONE));

      const token = host.querySelector(".city.owner-me .tokens .token");
      const note = hover(token);
      expect(note?.querySelector(".tip__name")?.textContent).toBe(
        token?.getAttribute("aria-label"),
      );
      expect(note?.querySelector(".tip__text")?.textContent).not.toBe("");
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
