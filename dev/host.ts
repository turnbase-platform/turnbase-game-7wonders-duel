// The dev harness host page: three iframes (seat 1, seat 2, spectator) standing in for the
// platform shell. It speaks the real postMessage protocol to the unmodified game bundle and
// relays to the in-process game behind /api (see DevServer.java).
import { VIEWERS, initFor, parseStreamEvent, streamTarget, type Viewer } from "./protocol.js";

const frames = new Map<Viewer, HTMLIFrameElement>();

function post(viewer: Viewer, message: unknown): void {
  frames.get(viewer)?.contentWindow?.postMessage(message, location.origin);
}

async function resync(viewer: Viewer): Promise<void> {
  const response = await fetch(streamTarget("resync", viewer));
  post(viewer, { type: "game_event", payload: (await response.json()) as unknown });
}

function listen(viewer: Viewer): void {
  const source = new EventSource(streamTarget("stream", viewer));
  source.onmessage = (event: MessageEvent<string>) => {
    const parsed = parseStreamEvent(event.data);
    if (parsed === "reset") void resync(viewer);
    else post(viewer, parsed);
  };
}

window.addEventListener("message", (event: MessageEvent<{ type?: string; payload?: unknown }>) => {
  const viewer = VIEWERS.find((v) => frames.get(v)?.contentWindow === event.source);
  if (!viewer) return;
  if (event.data.type === "ready") {
    post(viewer, initFor(viewer));
    void resync(viewer);
  } else if (event.data.type === "action") {
    void fetch(streamTarget("action", viewer), {
      method: "POST",
      body: JSON.stringify(event.data.payload),
    });
  }
});

async function status(): Promise<void> {
  const state = (await (await fetch("/api/state")).json()) as { seed: number; moves: number };
  const label = document.getElementById("status");
  if (label) label.textContent = `seed ${String(state.seed)} · ${String(state.moves)} moves`;
}

function control(id: string, run: () => Promise<unknown>): void {
  document.getElementById(id)?.addEventListener("click", () => {
    void run().then(status);
  });
}

function input(id: string): string {
  return (document.getElementById(id) as HTMLInputElement).value;
}

for (const viewer of VIEWERS) {
  const frame = document.getElementById(`frame-${viewer}`) as HTMLIFrameElement;
  frames.set(viewer, frame);
  frame.src = "/dev/game.html";
  listen(viewer);
}
control("new", () =>
  fetch(`/api/new?seed=${encodeURIComponent(input("seed"))}`, { method: "POST" }),
);
control("advance", () =>
  fetch(`/api/advance?steps=${encodeURIComponent(input("steps"))}`, { method: "POST" }),
);
control("reload", () => {
  for (const frame of frames.values()) frame.contentWindow?.location.reload();
  return Promise.resolve();
});
void status();
setInterval(() => void status(), 2000);
