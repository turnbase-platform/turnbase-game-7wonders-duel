// The seven science symbols, redrawn in the colours of the rulebook (p. 12) and in the style of the
// imported sprite: bronze and gold on a transparent ground, 100×100.
import type { Imported } from "./imported.js";

const BRONZE = "#b8863a";
const GOLD = "#e0ad52";
const DARK = "#7a4f1f";

const group = (body: string): Imported => ({
  viewBox: "0 0 100 100",
  body: `<g fill="none" stroke-linecap="round" stroke-linejoin="round">${body}</g>`,
});

/** Full-colour science symbols, keyed by the ids the catalog uses. */
export const SCIENCE: Readonly<Record<string, Imported>> = {
  // Armillary sphere on its stand.
  astrolabe: group(
    `<path d="M32 70q18 20 36 0" stroke="${DARK}" stroke-width="5"/>` +
      `<path d="M50 74v15" stroke="${DARK}" stroke-width="7"/>` +
      `<ellipse cx="50" cy="91" rx="17" ry="5" fill="${DARK}"/>` +
      `<circle cx="50" cy="42" r="30" stroke="${GOLD}" stroke-width="5"/>` +
      `<ellipse cx="50" cy="42" rx="12" ry="30" transform="rotate(25 50 42)" stroke="${BRONZE}" stroke-width="4"/>` +
      `<ellipse cx="50" cy="42" rx="30" ry="9" stroke="${BRONZE}" stroke-width="4"/>` +
      `<path d="M31 14l38 56" stroke="${DARK}" stroke-width="3"/>`,
  ),
  // Balance with two pans.
  scales: group(
    `<ellipse cx="50" cy="91" rx="16" ry="5" fill="${DARK}"/>` +
      `<path d="M50 14v74" stroke="${BRONZE}" stroke-width="7"/>` +
      `<circle cx="50" cy="12" r="5" fill="${GOLD}"/>` +
      `<path d="M16 28q34-12 68 0" stroke="${GOLD}" stroke-width="5"/>` +
      `<path d="M20 29L8 58M20 29l12 29M80 29L68 58M80 29l12 29" stroke="${DARK}" stroke-width="2"/>` +
      `<path d="M6 58h28q-14 16-28 0zM66 58h28q-14 16-28 0z" fill="${BRONZE}" stroke="${DARK}" stroke-width="2"/>`,
  ),
  // Flat dial with its upright gnomon, on a slab walled on both front sides.
  sundial: group(
    `<path d="M8 72l42 12v6L8 78z" fill="${DARK}"/>` +
      `<path d="M50 84l42-18v6L50 90z" fill="#5c3a15"/>` +
      `<path d="M8 72l44-20 40 14-42 18z" fill="${BRONZE}" stroke="${DARK}" stroke-width="2"/>` +
      `<path d="M38 16h8l4 44-12 6z" fill="${GOLD}" stroke="${DARK}" stroke-width="2"/>` +
      `<path d="M50 60l30 8" stroke="${DARK}" stroke-width="2"/>`,
  ),
  "mortar-and-pestle": group(
    `<path d="M12 50h76c0 24-16 40-38 40S12 74 12 50z" fill="${BRONZE}" stroke="${DARK}" stroke-width="3"/>` +
      `<ellipse cx="50" cy="50" rx="38" ry="8" fill="${GOLD}" stroke="${DARK}" stroke-width="3"/>` +
      `<path d="M46 52L78 14" stroke="${DARK}" stroke-width="15"/>` +
      `<path d="M46 52L78 14" stroke="${GOLD}" stroke-width="9"/>`,
  ),
  // The set square carpenters hang a plumb bob from.
  "plumb-bob": group(
    `<path d="M50 12L90 88H72L50 46L28 88H10z" fill="${BRONZE}" stroke="${DARK}" stroke-width="3"/>` +
      `<path d="M50 44v20" stroke="${DARK}" stroke-width="2"/>` +
      `<path d="M50 62l-7 9 7 10 7-10z" fill="${GOLD}" stroke="${DARK}" stroke-width="2"/>`,
  ),
  quill: group(
    `<path d="M20 84C12 50 40 20 84 12C82 50 54 78 20 84z" fill="${GOLD}" stroke="${DARK}" stroke-width="3"/>` +
      `<path d="M20 84L76 22" stroke="${DARK}" stroke-width="3"/>` +
      `<path d="M23 87l-5-6L9 96z" fill="${DARK}" stroke="${DARK}" stroke-width="2"/>` +
      `<path d="M58 64h26v22H58z" fill="${BRONZE}" stroke="${DARK}" stroke-width="2"/>` +
      `<path d="M62 56h18v8H62z" fill="${DARK}"/>`,
  ),
  wheel: group(
    `<circle cx="50" cy="50" r="38" stroke="${BRONZE}" stroke-width="8"/>` +
      `<circle cx="50" cy="50" r="31" stroke="${DARK}" stroke-width="2"/>` +
      `<path d="M50 50V19M50 50v31M50 50H19M50 50h31M50 50L28 28M50 50l22 22M50 50L72 28M50 50L28 72" stroke="${GOLD}" stroke-width="4"/>` +
      `<circle cx="50" cy="50" r="7" fill="${DARK}"/>`,
  ),
};
