import type { Badge } from "./wire.js";

/** One icon, with the text laid over it and the words a screen reader hears. */
export interface Glyph {
  /** An id in the icon sprite; `unknown` for a badge kind this client has not been taught. */
  icon: string;
  text: string | null;
  title: string;
  /** The card colour a `card` glyph stands for, otherwise null. */
  tint: string | null;
}

function glyph(
  icon: string,
  text: string | null,
  title: string,
  tint: string | null = null,
): Glyph {
  return { icon, text, title, tint };
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

function amountText(amount: number): string | null {
  return amount > 1 ? String(amount) : null;
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
      return of.map((r) => glyph(r, amountText(badge.amount), `${amount} ${r}`));
    case "producesOneOf":
      return of.map((r) => glyph(r, null, `one of ${of.join(", ")}`));
    case "shields":
      return [glyph("shield", amount, `${amount} shields`)];
    case "shieldBonus":
      return [glyph("shield", `+${amount}`, `${amount} more shields from ${of.join(", ")}`)];
    case "points":
      return [glyph("vp", amount, `${amount} victory points`)];
    case "pointsPer":
      return [
        glyph("vp", amount, `${amount} victory points for each`),
        ...of.map((t) => target(t, badge.per)),
        ...(badge.larger ? [glyph("larger", null, "in the city that has more")] : []),
      ];
    case "coins":
      return [glyph("coin", amount, `${amount} coins`)];
    case "coinsPer":
      return [glyph("coin", amount, `${amount} coins for each`), ...of.map((t) => target(t))];
    case "loseCoins":
      return [glyph("coin", `−${amount}`, `the opponent loses ${amount} coins`)];
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
      return [
        ...of.map((r) => glyph(r, null, `${r} costs 1 coin`)),
        glyph("coin", "1", "1 coin each"),
      ];
    case "playAgain":
      return [glyph("play-again", null, "play again")];
    case "playAgainOnWonder":
      return [glyph("play-again", null, "play again"), glyph("wonder", null, "after a Wonder")];
    case "drawProgress":
      return [glyph("draw-token", amount, `draw ${amount} progress tokens`)];
    case "buildFromDiscard":
      return [glyph("rebuild", null, "build a discarded card")];
    case "discardOpponent":
      return [glyph("destroy", null, "discard an opponent's card"), ...of.map((t) => target(t))];
    default:
      return [glyph("unknown", some ? amount : null, kind)];
  }
}
