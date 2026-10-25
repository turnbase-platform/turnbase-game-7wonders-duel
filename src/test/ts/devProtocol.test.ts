import { describe, expect, it } from "vitest";
import { VIEWERS, initFor, parseStreamEvent, streamTarget } from "../../../dev/protocol.js";

describe("dev harness protocol", () => {
  it("greets a seat as a player who knows the whole roster", () => {
    const init = initFor("1");

    expect(init.type).toBe("init");
    expect(init.role).toBe("player");
    expect(init.self?.displayName).toBe("One");
    expect(init.roster.map((p) => p.displayName)).toEqual(["One", "Two"]);
    expect(init.teams).toHaveLength(2);
  });

  it("greets the spectator with no identity", () => {
    const init = initFor("spectator");

    expect(init.role).toBe("spectator");
    expect(init.self).toBeNull();
    expect(init.roster).toHaveLength(2);
  });

  it("names the three viewers the server knows", () => {
    expect(VIEWERS).toEqual(["1", "2", "spectator"]);
  });

  it("turns a server-sent delivery into the matching inbound message", () => {
    const sent = parseStreamEvent('{"channel":"game_event","payload":{"kind":"view"}}');

    expect(sent).toEqual({ type: "game_event", payload: { kind: "view" } });
  });

  it("turns a game over delivery into a game_over message", () => {
    const sent = parseStreamEvent('{"channel":"game_over","payload":{"winner":"1"}}');

    expect(sent).toEqual({ type: "game_over", results: { winner: "1" } });
  });

  it("reports a reset as such, since the host answers it with a fresh resync", () => {
    expect(parseStreamEvent('{"channel":"reset"}')).toBe("reset");
  });

  it("builds the endpoint an action goes to", () => {
    expect(streamTarget("action", "2")).toBe("/api/action/2");
    expect(streamTarget("resync", "spectator")).toBe("/api/resync/spectator");
  });
});
