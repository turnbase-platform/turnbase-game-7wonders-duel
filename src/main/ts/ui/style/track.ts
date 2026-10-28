// The military track and the tokens beside it, from the agreed `track-v6` mockup: tiles shaded
// from defeat red through limestone to victory gold, capitals in the seat colours, a pawn, and the
// shields-to-go badges.
import { EASE, MOTION_MS } from "../motion.js";

export const TRACK = `
.strip { display: block; gap: 12px; }
.track { flex: 1; min-width: 0; }
.trk { --tw: calc((100cqw - 54px) / 19); container-type: inline-size; display: grid; grid-template-columns: repeat(19, minmax(0, 1fr)); gap: 3px; text-align: center; max-width: min(680px, 100%); margin: 0 auto; position: relative; }
.tile { --i: 0; position: relative; aspect-ratio: 2 / 3; display: flex; align-items: center; justify-content: center; border-radius: 3px; font-size: calc(var(--tw) * .7); color: #fff; border: 1px solid #0003;
  background-image: linear-gradient(to right, #a8191a 0%, #c8503f 20%, #f4e8da 50%, #e8c97a 80%, #d49a1c 100%);
  background-size: 1900% 100%; background-position: calc(var(--i) / 18 * 100%) 0; }
.tile.cap { --wall: var(--c); border: 0; border-radius: 2px 2px 3px 3px; background: linear-gradient(to bottom, color-mix(in srgb, var(--wall) 78%, #fff), var(--wall) 40%, color-mix(in srgb, var(--wall) 70%, #000));
  clip-path: polygon(0 0, 24% 0, 24% 11%, 38% 11%, 38% 0, 62% 0, 62% 11%, 76% 11%, 76% 0, 100% 0, 100% 100%, 0 100%); }
.tile.cap::before { content: ""; position: absolute; inset: 0; background: repeating-linear-gradient(to bottom, transparent 0 17%, #0002 17% 19%); }
.tile.cap::after { content: ""; position: absolute; bottom: 0; width: 46%; height: 42%; border-radius: 999px 999px 0 0; background: linear-gradient(to bottom, #0007, #000b); box-shadow: 0 0 0 max(1px, .08em) #fff5; }
.tile.cap.me { --c: var(--me); } .tile.cap.opp { --c: var(--opp); }
.pawn { --at: 9; position: absolute; top: 0; left: 0; width: var(--tw); aspect-ratio: 2 / 3; font-size: var(--tw); display: flex; align-items: center; justify-content: center; pointer-events: none; transform: translateX(calc(var(--at) * (100% + 3px))); transition: transform ${String(MOTION_MS.travel)}ms ${EASE}; }
.pawn::after { content: ""; width: .8em; height: .8em; margin-top: -.15em; border-radius: 50%; box-sizing: border-box; border: max(2px, .07em) solid #fff; background: radial-gradient(ellipse at 35% 30%, #ee6a62, var(--c-red) 55%, #9a2222); box-shadow: 0 .14em 0 -.02em #8a1c1c, 0 .14em 0 max(1px, .03em) #fff, 0 .3em .2em #0007; }
@media (prefers-reduced-motion: reduce) { .pawn { transition: none; } }
.field { --from: 0; --to: 0; grid-column: calc(var(--from) + 1) / calc(var(--to) + 2); height: calc(var(--tw) * 1.05); box-sizing: border-box; display: flex; align-items: center; justify-content: center; border: 1px solid var(--line); border-radius: 3px; font-size: calc(var(--tw) * .4); color: var(--muted); }
.field.is-current { color: var(--ink); font-weight: 700; background: color-mix(in srgb, var(--c-yellow) 28%, var(--surface)); border: 2px solid var(--ink); box-shadow: 0 0 0 2px var(--c-yellow), 0 2px 6px #0004; }
.field.loot.is-current { color: #fff; background: var(--c-red); }
.field.loot { color: #fff; background: var(--c-red); border-color: #0004; }
.field .glyph { font-size: calc(var(--tw) * .85); }
.field .icon-vp { color: #b8860b; }
.field .icon-vp + b { text-shadow: 0 0 2px #000, 0 0 2px #000, 0 0 1px #000; }
.field .icon-coin-lost + b { color: #e53935; }
.field.loot.is-taken { background: none; color: var(--muted); border: 1px solid var(--line); }
/* Standing: the same track turned a quarter, tiles in the first column and the fields beside them. */
.is-rail .strip { grid-area: strip; display: flex; width: 58px; padding: 4px; box-sizing: border-box; }
.is-rail .track { height: 100%; }
.is-rail .trk { --tw: 18px; height: 100%; max-width: none; grid-template-columns: var(--tw) minmax(0, 1fr); grid-template-rows: repeat(19, minmax(0, 1fr)); }
.is-rail .tile { grid-column: 1; aspect-ratio: auto; }
.is-rail .field { grid-column: 2; grid-row: calc(var(--from) + 1) / calc(var(--to) + 2); height: auto; font-size: 10px; }
.is-rail .field .glyph { font-size: 10px; flex-direction: column; gap: 0; }
.is-rail .pawn { height: calc((100% - 54px) / 19); aspect-ratio: auto; font-size: 14px; transform: translateY(calc(var(--at) * (100% + 3px))); }
.is-compact .strip { padding: 4px 6px; gap: 6px; }
/* The track is context, not the board: it never grows past what it needs. */
@media (min-width: 700px) { .trk { max-width: min(560px, 100%); } }
`;
