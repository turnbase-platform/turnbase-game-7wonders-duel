import { act } from "react";
import { createRoot, type Root } from "react-dom/client";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import type { Step } from "../../main/ts/core/session.js";
import type { EventView } from "../../main/ts/core/wire.js";
import { useChoreography } from "../../main/ts/ui/useChoreography.js";

(globalThis as { IS_REACT_ACT_ENVIRONMENT?: boolean }).IS_REACT_ACT_ENVIRONMENT = true;

function moved(component: string): EventView {
  return {
    id: 1,
    parent: -1,
    kind: "moved",
    cause: null,
    component,
    from: { zone: "structure.up", key: "0:0" },
    to: { zone: "city", key: "p" },
    player: null,
    amount: 0,
    pawnFrom: null,
    pawnTo: null,
  };
}

const stepOf = (number: number, ...events: EventView[]): Step => ({ number, events });

/** Two cards; `where` is where the test says each one is, since a test DOM has no layout. */
function Probe({
  step,
  inDialog = false,
  gone = false,
}: {
  step: Step | null;
  inDialog?: boolean;
  gone?: boolean;
}) {
  const ref = { current: null as HTMLDivElement | null };
  const { burns } = useChoreography(ref, step);
  return (
    <div
      ref={(el) => {
        ref.current = el;
      }}
    >
      {!gone && <div data-component="palisade" />}
      {burns.map((b) => (
        <i key={b.component} data-burn={b.component} />
      ))}
      {inDialog && (
        <div role="dialog">
          <div data-component="palisade" />
        </div>
      )}
    </div>
  );
}

describe("useChoreography", () => {
  let host: HTMLElement;
  let root: Root;
  let at: { left: number; top: number };
  const animate = vi.fn();
  const finish = vi.fn();

  beforeEach(() => {
    host = document.body.appendChild(document.createElement("div"));
    root = createRoot(host);
    at = { left: 100, top: 100 };
    animate.mockReset();
    animate.mockReturnValue({ finish });
    finish.mockReset();
    Element.prototype.animate = animate;
    vi.spyOn(Element.prototype, "getBoundingClientRect").mockImplementation(function (
      this: Element,
    ) {
      const live = this.getAttribute("data-component") !== null;
      return { ...(live ? at : { left: 0, top: 0 }), width: 50, height: 80 } as DOMRect;
    });
  });

  afterEach(() => {
    act(() => {
      root.unmount();
    });
    host.remove();
    vi.restoreAllMocks();
  });

  const show = (step: Step | null, inDialog = false, gone = false): void => {
    act(() => {
      root.render(<Probe step={step} inDialog={inDialog} gone={gone} />);
    });
  };

  const discarded = (component: string): EventView => ({
    ...moved(component),
    to: { zone: "discard", key: "" },
  });

  it("leaves a discarded card burning where it stood, once it has left the board", () => {
    show(null);
    show(stepOf(1, discarded("palisade")), false, true);

    expect(host.querySelector('[data-burn="palisade"]')).not.toBeNull();
    expect(animate).not.toHaveBeenCalled();
  });

  it("stops the burn at a click, like every other animation", () => {
    show(null);
    show(stepOf(1, discarded("palisade")), false, true);
    act(() => {
      host.firstElementChild?.dispatchEvent(new Event("pointerdown", { bubbles: true }));
    });

    expect(host.querySelector("[data-burn]")).toBeNull();
  });

  it("animates a moved card from where it was to where it is", () => {
    show(null);
    at = { left: 300, top: 40 };
    show(stepOf(1, moved("palisade")));

    expect(animate).toHaveBeenCalledTimes(1);
    const [frames] = animate.mock.calls[0] as [Keyframe[]];
    expect(frames[0]?.transform).toBe("translate(-200px, 60px) scale(1, 1)");
    expect(frames[1]?.transform).toBe("none");
  });

  it("flies a copy over the board and keeps the real card hidden until it lands", () => {
    show(null);
    at = { left: 300, top: 40 };
    show(stepOf(1, moved("palisade")));

    const real = host.querySelector<HTMLElement>('[data-component="palisade"]');
    const copy = host.querySelector<HTMLElement>("[data-flight]");
    expect(real?.style.visibility).toBe("hidden");
    expect(copy?.style.position).toBe("fixed");
    expect(copy?.style.left).toBe("300px");
    expect(animate.mock.contexts[0]).toBe(copy);

    act(() => {
      show(stepOf(2));
    });
    expect(real?.style.visibility).toBe("");
    expect(host.querySelector("[data-flight]")).toBeNull();
  });

  it("plays a step once, however often the board draws again", () => {
    show(null);
    at = { left: 300, top: 40 };
    const step = stepOf(1, moved("palisade"));
    show(step);
    show(step);

    expect(animate).toHaveBeenCalledTimes(1);
  });

  it("does not replay the step that was already there when the board appeared", () => {
    at = { left: 300, top: 40 };
    show(stepOf(1, moved("palisade")));

    expect(animate).not.toHaveBeenCalled();
  });

  it("finishes the running animation at once when a new step arrives", () => {
    show(null);
    at = { left: 300, top: 40 };
    show(stepOf(1, moved("palisade")));
    at = { left: 10, top: 10 };
    show(stepOf(2, moved("palisade")));

    expect(finish).toHaveBeenCalledTimes(1);
    expect(animate).toHaveBeenCalledTimes(2);
  });

  it("finishes the running animation on a click", () => {
    show(null);
    at = { left: 300, top: 40 };
    show(stepOf(1, moved("palisade")));
    act(() => {
      host.firstElementChild?.dispatchEvent(new Event("pointerdown", { bubbles: true }));
    });

    expect(finish).toHaveBeenCalledTimes(1);
  });

  it("plays nothing when the viewer prefers reduced motion", () => {
    vi.stubGlobal("matchMedia", () => ({ matches: true }));
    show(null);
    at = { left: 300, top: 40 };
    show(stepOf(1, moved("palisade")));
    vi.unstubAllGlobals();

    expect(animate).not.toHaveBeenCalled();
  });

  it("animates the card on the board, never its copy in a dialog", () => {
    show(null, true);
    at = { left: 300, top: 40 };
    show(stepOf(1, moved("palisade")), true);

    const target = animate.mock.contexts[0] as Element;
    expect(target.closest('[role="dialog"]')).toBeNull();
  });
});
