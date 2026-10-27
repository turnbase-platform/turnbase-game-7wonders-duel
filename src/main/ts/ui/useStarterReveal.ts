import { useEffect, useRef, useState } from "react";
import { watchStarts, type Round } from "../core/starter.js";
import type { DuelView } from "../core/wire.js";
import { MOTION_MS } from "./motion.js";

/** The round whose start is being announced right now, if any; it clears itself after a moment. */
export function useStarterReveal(view: DuelView | null): Round | null {
  const seen = useRef<string | null | undefined>(undefined);
  const [shown, setShown] = useState<Round | null>(null);

  useEffect(() => {
    if (view === null) return;
    const next = watchStarts(seen.current, view);
    seen.current = next.seen;
    if (next.reveal !== null) setShown(next.reveal);
  }, [view]);

  useEffect(() => {
    if (shown === null) return;
    const timer = setTimeout(() => {
      setShown(null);
    }, MOTION_MS.starter);
    return () => {
      clearTimeout(timer);
    };
  }, [shown]);

  return shown;
}
