import type { Badge } from "./wire.js";

/** One icon, with the text laid over it and the words a screen reader hears. */
export interface Glyph {
  /** An id in the icon sprite; `unknown` for a badge kind this client has not been taught. */
  icon: string;
  text: string | null;
  title: string;
  /** The card colour a `card` glyph stands for, otherwise null. */
  tint: string | null;
  /** Card colours drawn as cards piled diagonally, the last on top; empty for a single card. */
  pile: string[];
  /** What a per-card reward pays, shown on small icons in the glyph's bottom corners. */
  corners: Corner[];
}

/** An icon with its number, in one bottom corner of a glyph. */
export interface Corner {
  icon: "coin" | "vp";
  text: string;
  side: "left" | "right";
}

function glyph(
  icon: string,
  text: string | null,
  title: string,
  tint: string | null = null,
  corners: Corner[] = [],
): Glyph {
  return { icon, text, title, tint, pile: [], corners };
}

/** What a `per`/`of` entry counts or applies to: a card colour, progress tokens or Wonders. */
function target(thing: string, per = 1): Glyph {
  if (thing === "coins") {
    return glyph("coin", per > 1 ? String(per) : null, `${String(per)} coins`);
  }
  if (thing.startsWith("color:")) {
    const colour = thing.slice("color:".length);
    return glyph("card", null, `${colour} cards`, colour);
  }
  return glyph(thing, null, thing === "progress" ? "progress tokens" : `${thing}s`);
}

/**
 * What a reward per counted thing looks like: the things, with the reward on a corner of the icon.
 * Card colours counted together are one pile of cards; anything else is an icon of its own.
 */
function reward(
  things: string[],
  per: number,
  icon: Corner["icon"],
  amount: string,
  what: string,
): Glyph[] {
  const colours = things.filter((t) => t.startsWith("color:"));
  const rest = things.filter((t) => !t.startsWith("color:"));
  const corners: Corner[] = [{ icon, text: amount, side: "right" }];
  const make = (g: Glyph): Glyph => ({
    ...g,
    title: `${amount} ${what} for each ${g.title}`,
    corners,
  });
  const pile =
    colours.length > 1
      ? [
          make({
            ...glyph("card", null, `${colours.map((c) => c.slice(6)).join(" or ")} cards`),
            pile: colours.map((c) => c.slice("color:".length)),
          }),
        ]
      : colours.map((c) => make(target(c, per)));
  return [...pile, ...rest.map((t) => make(target(t, per)))];
}

/** How far each card of a pile peeks out from the one behind it, in icon widths. */
export const PILE_STEP = 0.25;

/** How many icon widths a glyph takes in the banner, which the banner fits its icons to. */
export function span(g: Glyph): number {
  if (g.icon === "or") {
    return 0.3;
  }
  const cards = Math.max(g.pile.length, 1);
  return 1 + (cards - 1) * PILE_STEP + (g.corners.length > 0 ? 0.2 : 0);
}

const sameThings = (a: Badge, b: Badge): boolean => a.of.join() === b.of.join();

/**
 * The glyphs of a card's badges, in order. A guild pays coins and points for the same cards: those
 * are one icon, the points on its right corner and the coins on its left.
 */
export function presentAll(badges: Badge[]): Glyph[] {
  return badges
    .filter((b) => b.kind !== "chain")
    .flatMap((b) => {
      const points = badges.find((o) => o.kind === "pointsPer" && sameThings(o, b));
      if (b.kind === "coinsPer" && points) {
        return [];
      }
      const coins = badges.find((o) => o.kind === "coinsPer" && sameThings(o, b));
      const glyphs = present(b);
      if (b.kind !== "pointsPer" || !coins) {
        return glyphs;
      }
      const left: Corner = { icon: "coin", text: String(coins.amount), side: "left" };
      return glyphs.map((g) =>
        g.corners.length > 0 ? { ...g, corners: [...g.corners, left] } : g,
      );
    });
}

/**
 * The one place that knows what each badge kind looks like. Components draw the glyphs and never
 * look at `kind`, so a kind added by an expansion degrades to a generic glyph instead of throwing.
 */
export function present(badge: Badge): Glyph[] {
  const { kind, of } = badge;
  const amount = String(badge.amount);
  const some = badge.amount > 0;
  switch (kind) {
    case "produces":
      return of.flatMap((r) =>
        Array.from({ length: badge.amount }, () => glyph(r, null, `${amount} ${r}`)),
      );
    case "producesOneOf":
      return of.flatMap((r, i) => [
        ...(i > 0 ? [glyph("or", null, "or")] : []),
        glyph(r, null, `one of ${of.join(", ")}`),
      ]);
    case "shields":
      return Array.from({ length: badge.amount }, () => glyph("shield", null, `${amount} shields`));
    case "shieldBonus":
      return [glyph("shield", `+${amount}`, `${amount} more shields from ${of.join(", ")}`)];
    case "points":
      return [glyph("vp", amount, `${amount} victory points`)];
    case "pointsPer":
      return [
        ...(badge.larger ? [glyph("better", null, "in the city that has more")] : []),
        ...reward(of, badge.per, "vp", amount, "victory points"),
      ];
    case "coins":
      return [glyph("coin", amount, `${amount} coins`)];
    case "coinsPer":
      return reward(of, 1, "coin", amount, "coins");
    case "loseCoins":
      return [glyph("coin-lost", amount, `the opponent loses ${amount} coins`)];
    case "science":
      return of.map((s) => glyph(s, null, `science symbol ${s}`));
    case "chain":
      return of.map((s) => glyph(s, null, `chain symbol ${s}`));
    case "chainBonus":
      return [glyph("chain", amount, `${amount} coins when built by chain`)];
    case "tradeRevenue":
      return [glyph("trade", null, "coins from trading")];
    case "reduction":
      return [
        glyph("discount", `−${amount}`, `${amount} resources cheaper`),
        ...of.map((t) => target(t)),
      ];
    case "fixedPrice":
      return of.map((r) => glyph(`priced-${r}`, null, `${r} costs 1 coin`));
    case "playAgain":
      return [glyph("play-again", null, "play again")];
    case "playAgainOnWonder":
      return [glyph("play-again", null, "play again"), glyph("wonder", null, "after a Wonder")];
    case "drawProgress":
      return [glyph("draw-token", null, `draw ${amount} progress tokens`)];
    case "buildFromDiscard":
      return [glyph("rebuild", null, "build a discarded card for free")];
    case "discardOpponent":
      return of.map((t) => {
        const colour = t.slice("color:".length);
        return glyph("torn-card", null, `discard an opponent's ${colour} card`, colour);
      });
    default:
      return [glyph("unknown", some ? amount : null, kind)];
  }
}
