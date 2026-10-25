// The board and its arrangements (design §3). Layout classes come from `layoutFor`, measured on the
// board itself, so a narrow frame on a wide screen gets the narrow arrangement.
export const BOARD = `
html, body, #root { height: 100%; }
body { margin: 0; background: var(--ground); color: var(--ink); font: 14px var(--sans); }
.board { position: relative; box-sizing: border-box; height: 100%; display: flex; flex-direction: column; gap: 8px; padding: 8px; overflow: hidden; }
.board button { font: inherit; color: inherit; }
.header { display: flex; flex-wrap: wrap; gap: 4px 16px; align-items: baseline; }
.header .phase { font: 700 1.1em var(--serif); }
.header .turn { font-weight: 600; }
.owner-me { --seat: var(--me); } .owner-opp { --seat: var(--opp); }
.purse { border-bottom: 2px solid var(--seat); }
.strip { border: 1px solid var(--line); border-radius: var(--radius); background: var(--surface); padding: 10px; text-align: center; color: var(--muted); }
.is-compact .strip { padding: 3px; font-size: 11px; }
.is-compact .header { font-size: 12px; }
.banner { padding: 6px 10px; border-radius: var(--radius); background: #fdd; color: #600; }
.main { flex: 1; min-height: 0; display: flex; gap: 8px; }
.layout-medium .main, .layout-portrait .main { flex-direction: column; }
.structure { flex: 1; min-width: 0; min-height: 0; overflow: auto; display: flex; justify-content: center; align-items: flex-start; }
.panel { box-sizing: border-box; min-height: 0; overflow: auto; background: var(--surface); border: 1px solid var(--line); border-radius: var(--radius); padding: 8px; }
.layout-wide .panel { width: 280px; flex: none; }
.layout-landscape .panel { width: 240px; flex: none; }
.layout-medium .panel { max-height: 40%; flex: none; }
.layout-portrait .panel { flex: 1; }
.tabs { display: flex; gap: 4px; margin-bottom: 6px; }
.tab { flex: 1; padding: 6px 8px; background: none; border: 1px solid var(--line); border-radius: var(--radius); cursor: pointer; }
.tab[aria-selected="true"] { background: var(--ground); font-weight: 700; }
.tab.owner-me[aria-selected="true"], .tab.owner-opp[aria-selected="true"] { border-color: var(--seat); }
.bottom-tabs { display: flex; gap: 4px; }
.bottom-tabs .tab { padding: 10px 8px; }
.tab.needs-you { border-color: var(--me); box-shadow: 0 0 0 2px var(--me); }
.city h3, .log h3 { margin: 0 0 4px; font-size: 12px; opacity: .7; font-weight: 600; }
.city { border-left: 3px solid var(--seat); padding-left: 8px; }
.city__line { display: flex; gap: 10px; align-items: center; margin: 0 0 6px; }
.city__line .glyph { display: inline-flex; align-items: center; gap: 1px; }
.stacks, .wonders, .tokens { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 8px; align-items: flex-start; }
.stack { display: flex; flex-direction: column; gap: 2px; }
.city .card--wonder.card--standard { --w: 100px; }
.peekable { padding: 0; border: 0; background: none; line-height: 0; cursor: pointer; border-radius: var(--radius); }
.peekable:focus-visible { outline: 2px solid var(--ink); outline-offset: 1px; }
.peek { position: sticky; bottom: 0; display: flex; justify-content: center; padding-top: 6px; pointer-events: none; }
.log { margin-top: 8px; font-size: 12px; }
.log ol { padding-left: 18px; margin: 2px 0; }
.log .step { opacity: .6; }
.phase-panel { text-align: center; padding: 8px; }
.phase-panel h2 { font: 700 1.2em var(--serif); margin: 4px 0 12px; }
.choices { display: flex; flex-wrap: wrap; gap: 12px; justify-content: center; }
.choice { display: flex; flex-direction: column; align-items: center; gap: 6px; }
.phase-panel button { padding: 6px 14px; background: var(--surface); border: 1px solid var(--ink); border-radius: var(--radius); cursor: pointer; }
.pyramid-frame { width: 100%; height: 100%; display: flex; justify-content: center; align-items: flex-start; }
.pyramid { --cw: 75px; position: relative; flex: none; }
.pyramid .card { --w: var(--cw); }
.slot { position: absolute; padding: 0; border: 0; background: none; line-height: 0; }
.slot.is-covered { opacity: .45; filter: saturate(.6); }
button.slot { cursor: pointer; border-radius: var(--radius); }
.slot.is-takeable { box-shadow: 0 0 0 2px var(--me), 0 0 10px 2px color-mix(in srgb, var(--me) 55%, transparent); z-index: 1; }
.slot.is-selected { box-shadow: 0 0 0 3px var(--ink), 0 0 12px 3px var(--me); z-index: 2; transform: translateY(-3px); }
.actions { z-index: 10; box-sizing: border-box; display: flex; flex-direction: column; align-items: center; gap: 8px; padding: 12px; background: var(--surface); border: 1px solid var(--ink); border-radius: var(--radius); box-shadow: 0 6px 24px rgba(0, 0, 0, .35); }
.actions.is-popover { position: absolute; min-width: 200px; max-width: 290px; }
.actions .card--wonder.card--standard { --w: 100px; }
.actions.is-sheet { position: absolute; left: 0; right: 0; bottom: 0; flex-direction: row; justify-content: center; flex-wrap: wrap; border-radius: var(--radius) var(--radius) 0 0; padding-bottom: calc(12px + env(safe-area-inset-bottom, 0px)); }
.actions h4 { margin: 0; width: 100%; text-align: center; }
.actions__buttons, .actions__wonders { display: flex; flex-direction: column; gap: 6px; align-self: stretch; }
.actions__wonders { flex-direction: row; flex-wrap: wrap; justify-content: center; }
.actions__wonders button { flex: none; padding: 4px; }
.is-sheet .actions__buttons { flex: 1; min-width: 160px; }
.actions button { display: flex; flex-direction: column; align-items: center; gap: 2px; padding: 8px 10px; min-height: 36px; background: var(--ground); border: 1px solid var(--ink); border-radius: var(--radius); cursor: pointer; }
.actions button:disabled { opacity: .5; cursor: default; }
.actions button small { font-size: 11px; color: var(--muted); }
.actions .price { display: inline-flex; align-items: center; gap: 3px; font-size: 12px; }
.actions .actions__close { position: absolute; top: 2px; right: 2px; min-height: 0; padding: 0 6px; border: 0; background: none; font-size: 18px; }
button.slot:focus-visible { outline: 3px solid var(--ink); outline-offset: 2px; }
`;
