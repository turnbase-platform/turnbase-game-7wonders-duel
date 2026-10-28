// The military track and the tokens beside it, from the agreed `track-v6` mockup: tiles shaded
// from defeat red through limestone to victory gold, capitals in the seat colours, a pawn, and the
// shields-to-go badges.
import { EASE, MOTION_MS } from "../motion.js";

export const TRACK = `
.strip { display: block; gap: 12px; }
.track { flex: 1; min-width: 0; }
.trk { --tw: calc((100cqw - 54px) / 19); container-type: inline-size; display: grid; grid-template-columns: repeat(19, minmax(0, 1fr)); gap: 3px; text-align: center; max-width: 680px; margin: 0 auto; position: relative; }
.tile { --i: 0; --ink-fixed: #3b3226; position: relative; aspect-ratio: 2 / 3; display: flex; align-items: center; justify-content: center; border-radius: 50%; font-size: calc(var(--tw) * .52); line-height: 1; font-weight: 700; color: var(--ink-fixed); border: 1px solid #0003;
  background-image: linear-gradient(to right, #a8191a 0%, #c8503f 20%, #f4e8da 50%, #e8c97a 80%, #d49a1c 100%);
  background-size: 1900% 100%; background-position: calc(var(--i) / 18 * 100%) 0; }
.tile.defeat { color: #fff; text-shadow: 0 1px 1px #0008; }
.tile small { font-size: 1em; }
.tile.cap { font-size: calc(var(--tw) * .7); background: var(--c); background-image: none; color: #fff; }
.tile.cap.me { --c: var(--me); } .tile.cap.opp { --c: var(--opp); }
.pawn { --at: 9; position: absolute; top: 0; left: 0; width: var(--tw); aspect-ratio: 2 / 3; font-size: var(--tw); display: flex; align-items: center; justify-content: center; pointer-events: none; transform: translateX(calc(var(--at) * (100% + 3px))); transition: transform ${String(MOTION_MS.travel)}ms ${EASE}; }
.pawn::after { content: ""; width: .8em; height: .8em; margin-top: -.15em; border-radius: 50%; box-sizing: border-box; border: max(2px, .07em) solid #fff; background: radial-gradient(ellipse at 35% 30%, #ee6a62, var(--c-red) 55%, #9a2222); box-shadow: 0 .14em 0 -.02em #8a1c1c, 0 .14em 0 max(1px, .03em) #fff, 0 .3em .2em #0007; }
@media (prefers-reduced-motion: reduce) { .pawn { transition: none; } }
.field { height: calc(var(--tw) * 1.05); box-sizing: border-box; display: flex; align-items: center; justify-content: center; border: 1px solid var(--line); border-radius: 3px; font-size: calc(var(--tw) * .4); color: var(--muted); }
.field.is-current { color: var(--ink); font-weight: 700; background: color-mix(in srgb, var(--line) 60%, var(--surface)); border: 2px solid var(--ink); }
.field.loot { color: #fff; background: var(--c-red); border-color: #0004; }
.field .glyph { font-size: calc(var(--tw) * .85); }
.field .icon-vp { color: #b8860b; }
.field .icon-vp + b { text-shadow: 0 0 2px #000, 0 0 2px #000, 0 0 1px #000; }
.field .icon-coin-lost + b { color: #fff; -webkit-text-stroke: .16em #000; }
.field.loot.is-taken { background: none; color: var(--muted); border: 1px solid var(--line); }
.is-compact .strip { padding: 4px 6px; gap: 6px; }
`;
