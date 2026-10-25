import { useLayoutEffect, useState, type RefObject } from "react";
import type { Size } from "../core/layout.js";

/**
 * The size of an element, kept current while it is resized. The game lives in a frame of unknown
 * size (and the dev harness shows three), so layout follows the element, never `window`. Null until
 * there is something to measure, which is also all a test DOM ever has.
 */
export function useSize(ref: RefObject<HTMLElement | null>): Size | null {
  const [size, setSize] = useState<Size | null>(null);
  useLayoutEffect(() => {
    const element = ref.current;
    if (element === null) return;
    const measure = (): void => {
      const { width, height } = element.getBoundingClientRect();
      setSize((old) =>
        width === 0 || height === 0 || (old?.width === width && old.height === height)
          ? old
          : { width, height },
      );
    };
    measure();
    if (typeof ResizeObserver === "undefined") return;
    const observer = new ResizeObserver(measure);
    observer.observe(element);
    return () => {
      observer.disconnect();
    };
  }, [ref]);
  return size;
}
