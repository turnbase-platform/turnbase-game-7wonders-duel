import type { DuelView, PlayerId } from "./wire.js";

/** A stretch of play that someone starts: the Wonder draft (B-WND-1) or an Age (B-AGE-0). */
export interface Round {
  /** Names the round, so it is announced once however many views show it. */
  key: string;
  kind: "draft" | "age";
  /** The Age's number; 0 for the draft. */
  age: number;
  starter: PlayerId;
}

/**
 * The round the view shows the start of, or null while there is none: an Age whose starter is
 * still to be chosen, and a finished game. The starter is the player to move in the view's first
 * moments of the round, which is why this is only read when the round changes.
 */
export function roundOf(view: DuelView): Round | null {
  if (view.phase === "WONDER_DRAFT") {
    return { key: "draft", kind: "draft", age: 0, starter: view.active };
  }
  if (view.phase === "AGE" && view.deciding === null) {
    return { key: `age-${String(view.age)}`, kind: "age", age: view.age, starter: view.active };
  }
  return null;
}

/** No Wonder has been drafted: the game has just begun. */
const untouched = (view: DuelView): boolean =>
  view.phase === "WONDER_DRAFT" && !view.visible.some((p) => p.zone === "wonders.unbuilt");

/**
 * Watching a game's views one after another: which round was last seen, and whether this view starts
 * a new one worth announcing. A client that arrives mid-round did not see it start, so nothing is
 * announced, with one exception: a draft nobody has picked from is a start, whoever looks at it.
 *
 * @param seen the key of the last round seen, or undefined before the first view
 */
export function watchStarts(
  seen: string | null | undefined,
  view: DuelView,
): { seen: string | null; reveal: Round | null } {
  const round = roundOf(view);
  if (round === null || round.key === seen) return { seen: seen ?? null, reveal: null };
  const witnessed = seen !== undefined || untouched(view);
  return { seen: round.key, reveal: witnessed ? round : null };
}
