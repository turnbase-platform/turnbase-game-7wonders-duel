import type { Session } from "./session.js";
import type { DuelView, PlayerId, WireAction } from "./wire.js";

/** One thing the viewer may do, and what it is about: a component, or a player for a choice of seat. */
export interface Offer {
  target: string;
  label: string;
  action: WireAction;
}

/**
 * Everything the viewer may send right now, derived from the view alone: the server stays the
 * judge, so an offer is a convenience (it hides what is certainly refused), not a rule.
 */
export function offers(session: Session): Offer[] {
  const { view, self, role } = session;
  if (view === null || self === null || role !== "player" || view.phase === "OVER") return [];
  if (view.pending !== null) return view.pending.decider === self ? answers(session, view) : [];
  if (view.deciding !== null) return view.deciding === self ? starters(session) : [];
  if (view.active !== self) return [];
  return view.phase === "WONDER_DRAFT" ? draft(view) : plays(view, self);
}

function answers(session: Session, view: DuelView): Offer[] {
  const seats = new Set(session.roster.map((p) => p.id));
  return (view.pending?.options ?? []).map((option) =>
    seats.has(option)
      ? { target: option, label: "Choose", action: { type: "answerPlayer", player: option } }
      : { target: option, label: "Choose", action: { type: "answerComponent", component: option } },
  );
}

function starters(session: Session): Offer[] {
  return session.roster.map((p) => ({
    target: p.id,
    label: `${p.displayName} starts`,
    action: { type: "chooseStarter", player: p.id },
  }));
}

function draft(view: DuelView): Offer[] {
  return view.visible
    .filter((placed) => placed.zone === "draft")
    .map((placed) => ({
      target: placed.component,
      label: "Take",
      action: { type: "draftWonder", component: placed.component },
    }));
}

/**
 * A quote exists exactly for the cards that may be taken now; the rest of the pyramid is covered.
 * A Wonder is quoted for its owner, so any exposed card can be spent on any affordable Wonder.
 */
function plays(view: DuelView, self: PlayerId): Offer[] {
  const quoted = new Map(view.quotes.filter((q) => q.player === self).map((q) => [q.component, q]));
  const payable = view.visible
    .filter((placed) => placed.zone === "wonders.unbuilt" && placed.key === self)
    .map((placed) => placed.component)
    .filter((wonder) => quoted.get(wonder)?.affordable === true);
  return view.visible
    .filter(
      (placed) => placed.zone === "structure.up" && quoted.get(placed.component)?.takeable === true,
    )
    .flatMap((placed): Offer[] => [
      ...(quoted.get(placed.component)?.affordable === true
        ? [
            {
              target: placed.component,
              label: "Build",
              action: { type: "build", component: placed.component } as const,
            },
          ]
        : []),
      ...payable.map((wonder): Offer => ({
        target: placed.component,
        label: "Wonder",
        action: { type: "buildWonder", component: placed.component, wonder },
      })),
      {
        target: placed.component,
        label: "Discard",
        action: { type: "discard", component: placed.component },
      },
    ]);
}
