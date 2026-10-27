import { readFileSync } from "node:fs";
import { describe, expect, it } from "vitest";
import { roundOf, watchStarts } from "../../main/ts/core/starter.js";
import type { DuelView, ServerMessage } from "../../main/ts/core/wire.js";

function view(name: string, change: Partial<DuelView> = {}): DuelView {
  const message = JSON.parse(
    readFileSync(`src/test/resources/wire/${name}.json`, "utf8"),
  ) as ServerMessage;
  if (message.view === null) throw new Error(`${name} has no view`);
  return { ...message.view, ...change };
}

const A = "00000000-0000-0000-0000-000000000001";
const B = "00000000-0000-0000-0000-000000000002";

describe("roundOf", () => {
  it("names the draft and each Age once its starter is chosen", () => {
    expect(roundOf(view("resync"))).toEqual({ key: "draft", kind: "draft", age: 0, starter: B });
    expect(roundOf(view("view-with-decision", { active: A }))).toEqual({
      key: "age-3",
      kind: "age",
      age: 3,
      starter: A,
    });
  });

  it("is none while an Age waits for its starter to be chosen, and after the game", () => {
    expect(roundOf(view("view-in-age"))).toBeNull();
    expect(roundOf(view("view-game-over"))).toBeNull();
  });
});

describe("watchStarts", () => {
  it("announces a draft nobody has picked from, as the first thing a client sees", () => {
    const { reveal, seen } = watchStarts(undefined, view("resync"));
    expect(reveal?.key).toBe("draft");
    expect(seen).toBe("draft");
  });

  it("does not announce a round that was already under way when the client arrived", () => {
    expect(watchStarts(undefined, view("view-after-draft-pick")).reveal).toBeNull();
    expect(watchStarts(undefined, view("view-with-decision")).reveal).toBeNull();
    expect(watchStarts(undefined, view("view-with-decision")).seen).toBe("age-3");
  });

  it("announces the next round when the client has seen the one before", () => {
    const inAge = view("view-with-decision", { age: 2, active: B });
    expect(watchStarts("draft", inAge).reveal).toMatchObject({ key: "age-2", starter: B });
  });

  it("announces a round once, however many views show it", () => {
    const first = view("view-with-decision", { age: 2 });
    const { seen } = watchStarts("draft", first);
    expect(watchStarts(seen, { ...first, active: A }).reveal).toBeNull();
  });

  it("keeps what it has seen across a view that belongs to no round", () => {
    const waiting = view("view-in-age");
    expect(watchStarts("draft", waiting)).toEqual({ seen: "draft", reveal: null });
  });

  it("announces the draft of a rematch", () => {
    expect(watchStarts("age-3", view("resync")).reveal?.key).toBe("draft");
  });
});
