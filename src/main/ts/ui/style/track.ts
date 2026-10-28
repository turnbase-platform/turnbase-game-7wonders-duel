// The military track and the tokens beside it, from the agreed `track-v6` mockup: tiles shaded
// from defeat red through limestone to victory gold, capitals in the seat colours, a pawn, and the
// shields-to-go badges.
import { EASE, MOTION_MS } from "../motion.js";

export const TRACK = `
.strip { display: block; gap: 12px; }
.track { flex: 1; min-width: 0; }
.trk { display: grid; grid-template-columns: repeat(19, minmax(0, 1fr)); gap: 3px; text-align: center; max-width: 680px; margin: 0 auto; }
.tile { --i: 0; --ink-fixed: #3b3226; position: relative; aspect-ratio: 1; display: flex; align-items: center; justify-content: center; border-radius: 3px; font-size: 10px; font-weight: 700; color: var(--ink-fixed); border: 1px solid #0003;
  background-image: linear-gradient(to right, #a8191a 0%, #c8503f 20%, #f4e8da 50%, #e8c97a 80%, #d49a1c 100%);
  background-size: 1900% 100%; background-position: calc(var(--i) / 18 * 100%) 0; }
.tile.defeat { color: #fff; text-shadow: 0 1px 1px #0008; }
.tile small { opacity: 1; }
.tile.cap { font-size: 18px; background: var(--c); background-image: none; }
.tile.cap.me { --c: var(--me); } .tile.cap.opp { --c: var(--opp); }
.trk { position: relative; }
.pawn { --at: 9; position: absolute; top: 0; left: 0; width: calc((100% - 54px) / 19); aspect-ratio: 1; display: flex; align-items: center; justify-content: center; pointer-events: none; transform: translateX(calc(var(--at) * (100% + 3px))); transition: transform ${String(MOTION_MS.travel)}ms ${EASE}; }
.pawn svg { height: 124%; aspect-ratio: 24 / 28; overflow: visible; transform: translateY(-10%); filter: drop-shadow(0 2px 2px #0008); }
.pawn__body { fill: var(--c-red); stroke: #fff; stroke-width: 1.6; paint-order: stroke; }
.pawn__base { fill: #7d1c1c; stroke: #fff; stroke-width: 1.4; }
@media (prefers-reduced-motion: reduce) { .pawn { transition: none; } }
.field { height: 26px; box-sizing: border-box; display: flex; align-items: center; justify-content: center; gap: 3px; border: 1px solid var(--line); border-radius: 3px; font-size: 10px; color: var(--muted); }
.field.is-current { color: var(--ink); font-weight: 700; background: color-mix(in srgb, var(--line) 60%, var(--surface)); border: 2px solid var(--ink); }
.field.loot { color: #fff; background: var(--c-red); border-color: #0004; }
.field .glyph { font-size: 20px; }
.field .icon-vp { color: #b8860b; }
.field .icon-vp + b { text-shadow: 0 0 2px #000, 0 0 2px #000, 0 0 1px #000; }
.field .icon-coin-lost + b { color: #fff; -webkit-text-stroke: .16em #000; }
.field.loot.is-taken { background: none; color: var(--muted); border: 1px solid var(--line); }
.is-compact .strip { padding: 4px 6px; gap: 6px; }
.is-compact .tile.cap svg { display: none; }
.is-compact .tile { font-size: 8px; }
.is-compact .field { height: 16px; font-size: 8px; }
`;
