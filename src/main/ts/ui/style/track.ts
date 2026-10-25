// The military track and the tokens beside it, from the agreed `track-v6` mockup: tiles shaded
// from defeat red through limestone to victory gold, capitals in the seat colours, a pawn, and the
// shields-to-go badges.
export const TRACK = `
.strip { display: block; gap: 12px; }
.track { flex: 1; min-width: 0; }
.trk { display: grid; grid-template-columns: repeat(19, minmax(0, 1fr)); gap: 3px; text-align: center; max-width: 680px; margin: 0 auto; }
.tile { --i: 0; position: relative; aspect-ratio: 1; display: flex; align-items: center; justify-content: center; border-radius: 3px; font-size: 9px; color: #fff; text-shadow: 0 1px 1px #0006; border: 1px solid #0003;
  background-image: linear-gradient(to right, #a8191a 0%, #c8503f 20%, #f4e8da 50%, #e8c97a 80%, #d49a1c 100%);
  background-size: 1900% 100%; background-position: calc(var(--i) / 18 * 100%) 0; }
.tile.near { color: var(--ink); text-shadow: none; }
.tile small { opacity: .75; }
.tile.cap { font-size: 18px; background: var(--c); background-image: none; }
.tile.cap.me { --c: var(--me); } .tile.cap.opp { --c: var(--opp); }
.pawn { width: 70%; aspect-ratio: 1; border-radius: 50%; background: var(--c-red); border: 3px solid #fff; box-shadow: 0 0 0 2px var(--ink), 0 2px 4px #0008; box-sizing: border-box; }
.field { height: 26px; box-sizing: border-box; display: flex; align-items: center; justify-content: center; gap: 3px; border: 1px solid var(--line); border-radius: 3px; font-size: 10px; color: var(--muted); }
.field.is-current { color: var(--ink); font-weight: 700; background: color-mix(in srgb, var(--line) 60%, var(--surface)); border: 2px solid var(--ink); }
.field.loot { color: #fff; border-color: #0004; }
.field .glyph { font-size: 20px; }
.field.loot.zone-2 { background: #b0793a; } .field.loot.zone-3 { background: #8c4a2a; }
.field.loot.is-taken { background: none; color: var(--muted); border: 1px dashed var(--muted); }
.is-compact .strip { padding: 4px 6px; gap: 6px; }
.is-compact .tile small, .is-compact .tile.cap svg { display: none; }
.is-compact .field { height: 16px; font-size: 8px; }
`;
