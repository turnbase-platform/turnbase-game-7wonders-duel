import { EASE, MOTION_MS } from "../motion.js";

// The board and its arrangements (design §3). Layout classes come from `layoutFor`, measured on the
// board itself, so a narrow frame on a wide screen gets the narrow arrangement.
export const BOARD = `
html, body, #root { height: 100%; }
body { margin: 0; background: var(--ground); color: var(--ink); font: 14px var(--sans); }
.board { position: relative; box-sizing: border-box; height: 100%; display: flex; flex-direction: column; gap: 8px; padding: 8px; overflow: hidden; }
.board button { font: inherit; color: inherit; }
.header { display: flex; justify-content: space-between; align-items: center; gap: 6px 16px; flex-wrap: wrap; padding: 6px 10px; background: var(--surface); border-radius: 4px; border-bottom: 2px solid var(--line); font-size: 12px; }
.header .phase { font-variant: small-caps; letter-spacing: .05em; font-size: 1.1em; }
.header .turn { font-weight: 600; }
.purses { display: flex; align-items: center; gap: 14px; }
.purse { display: inline-flex; align-items: center; gap: 3px; }
.purse .who { margin-right: 3px; color: var(--muted); }
.owner-me { --seat: var(--me); } .owner-opp { --seat: var(--opp); }
.purse { border-bottom: 2px solid var(--seat); }
.banner { padding: 6px 10px; border-radius: var(--radius); background: #fdd; color: #600; }
.main { flex: 1; min-height: 0; display: flex; gap: 8px; }
.layout-medium .main, .layout-portrait .main { flex-direction: column; }
.stage { position: relative; flex: 1; min-width: 0; min-height: 0; display: flex; flex-direction: column; gap: 8px; }
.layout-portrait .stage:not(:has(.structure)) { flex: none; }
.structure { flex: 1; min-width: 0; min-height: 0; overflow: auto; display: flex; flex-direction: column; align-items: center; gap: 8px; }
.board-tokens { display: flex; flex-direction: column; align-items: center; flex: none; }
.tray__tokens { display: flex; gap: 6px; justify-content: center; }
.layout-wide .board-tokens { align-self: stretch; }
.panel { box-sizing: border-box; min-height: 0; overflow: auto; }
.layout-wide .main { display: grid; grid-template-columns: 330px minmax(0, 1fr) 330px; }
.city-col { box-sizing: border-box; min-height: 0; overflow: auto; }
/* One surface for every board component: the strip, the token tray, the panels and the city columns. */
.strip, .layout-wide .board-tokens, .panel, .city-col { padding: 10px; background: var(--surface); border: 1px solid var(--line); border-radius: 6px; box-shadow: none; }
.stagebar { flex: none; display: flex; justify-content: space-between; align-items: center; }
.logbtn, .pilebtn { padding: 3px 12px; background: var(--surface); border: 1px solid var(--line); border-radius: var(--radius); cursor: pointer; }
.pilebtn { display: inline-flex; align-items: center; gap: 6px; } .logbtn { margin-left: auto; }
.pilebtn .icon { width: 20px; height: 20px; }
.logbtn[aria-expanded="true"], .pilebtn[aria-expanded="true"] { border-color: var(--ink); }
.pile h3 { margin: 0 0 8px; font-size: 12px; opacity: .7; font-weight: 600; }
.pile__cards { display: flex; flex-wrap: wrap; gap: 6px; }
.pile .card { --w: 72px; }
.pile__empty { margin: 0; color: var(--muted); }
.drawer { position: absolute; z-index: 15; top: 8px; right: 8px; bottom: 8px; box-sizing: border-box; width: 340px; max-width: calc(100% - 16px); overflow: auto; padding: 10px; background: var(--surface); border: 1px solid var(--ink); border-radius: var(--radius); box-shadow: 0 6px 24px #0006; }
.drawer .log { margin-top: 0; }
.drawer__close { position: absolute; top: 2px; right: 6px; border: 0; background: none; font-size: 20px; cursor: pointer; }
.city__head { position: relative; display: flex; align-items: center; gap: 4px 12px; margin-bottom: 28px; padding-bottom: 4px; border-bottom: 3px solid var(--seat); }
.city__head h3 { margin: 0; font: 700 15px var(--serif); }
.city__turn { width: 10px; height: 10px; border-radius: 50%; background: var(--seat); box-shadow: 0 0 0 3px color-mix(in srgb, var(--seat) 30%, transparent); }
.city__head .purse--big { position: absolute; right: 4px; top: 100%; transform: translateY(-50%); }
.purse--big { display: inline-grid; place-items: center; border-bottom: 0; font-size: 46px; }
.purse--big .icon { grid-area: 1 / 1; width: 1em; height: 1em; }
.purse--big b { grid-area: 1 / 1; font: 800 .42em / 1 var(--figures); font-variant-numeric: lining-nums; color: #1b1a17; text-shadow: 0 0 2px #fff; }
.city-col .stack { width: 64px; } .city-col .stack .card { --w: 64px; }
.city-col .stack .peekable + .peekable { margin-top: -61px; }
.city-col .city .card--wonder.card--standard { --w: 148px; }
.city-col .city { border-left: 0; padding-left: 0; }
.layout-landscape .panel { width: 240px; flex: none; }
.layout-landscape .city .card--wonder.card--standard { --w: 92px; }
.layout-medium .panel { max-height: 40%; flex: none; }
.layout-portrait .panel { flex: 1; }
.tabs { display: flex; gap: 4px; margin-bottom: 6px; }
.tab { flex: 1; padding: 6px 8px; background: none; border: 1px solid var(--line); border-radius: var(--radius); cursor: pointer; }
.tab[aria-selected="true"] { background: var(--ground); font-weight: 700; }
.tab.owner-me[aria-selected="true"], .tab.owner-opp[aria-selected="true"] { border-color: var(--seat); }
.bottom-tabs { display: flex; gap: 4px; }
.bottom-tabs .tab { padding: 10px 8px; }
.tab.needs-you { border-color: var(--me); box-shadow: 0 0 0 2px var(--me); }
.log h3 { margin: 0 0 4px; font-size: 12px; opacity: .7; font-weight: 600; }
.city { border-left: 3px solid var(--seat); padding-left: 8px; }
.stacks, .tokens { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 8px; align-items: flex-start; }
.stack { display: flex; flex-direction: column; width: 56px; }
.stack .card { --w: 56px; }
.stack .peekable + .peekable { margin-top: -60px; }
.wonders { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; margin-bottom: 8px; }
.wonders .peekable { display: flex; justify-content: center; }
.city .card--wonder.card--standard { --w: 112px; }
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
.prompt { flex: none; border-bottom: 2px solid var(--me); }
.prompt .card { --w: 96px; }
.choice--hidden { display: grid; place-items: center; width: 96px; aspect-ratio: 2 / 3; border: 2px dashed var(--line); border-radius: var(--radius); color: var(--muted); font-size: 24px; }
.pyramid-frame { width: 100%; flex: 1; min-height: 0; display: flex; justify-content: center; align-items: flex-start; }
.pyramid { --cw: 75px; position: relative; flex: none; }
.pyramid .card { --w: var(--cw); }
.slot { transition: left ${String(MOTION_MS.travel)}ms ${EASE}; position: absolute; padding: 0; border: 0; background: none; line-height: 0; }
.slot.is-covered { filter: brightness(.72) saturate(.65); }
.slot__quote { position: absolute; left: 50%; bottom: 100%; transform: translate(-50%, 4px); display: inline-flex; align-items: center; gap: 3px; margin-bottom: 4px; padding: 2px 8px; white-space: nowrap; line-height: 1.3; font-size: 12px; font-weight: 600; background: var(--surface); border: 1px solid var(--ink); border-radius: 10px; box-shadow: 0 2px 8px #0004; opacity: 0; pointer-events: none; transition: opacity ${String(MOTION_MS.appear / 2)}ms ${EASE}, transform ${String(MOTION_MS.appear / 2)}ms ${EASE}; z-index: 2; }
.slot__quote .icon { width: 14px; height: 14px; }
button.slot:hover .slot__quote, button.slot:focus-visible .slot__quote { opacity: 1; transform: translate(-50%, 0); }
button.slot { cursor: pointer; border-radius: var(--radius); }
.slot.is-takeable { box-shadow: 0 0 0 2px var(--me), 0 0 10px 2px color-mix(in srgb, var(--me) 55%, transparent); z-index: 1; }
/* The selected card: the structure blurs, the card rises where it stands, its actions float beside it. */
.spotlight { --lw: 75px; position: absolute; inset: 0; z-index: 12; pointer-events: none; }
.spotlight > * { pointer-events: auto; }
.spotlight__scrim { position: absolute; inset: 0; background: color-mix(in srgb, var(--ground) 35%, transparent); backdrop-filter: blur(6px); animation: scrim ${String(MOTION_MS.appear)}ms ${EASE} 1; }
@keyframes scrim { from { opacity: 0; } }
.spotlight__card { position: absolute; line-height: 0; transform: translateY(-6px) scale(1.1); transform-origin: 50% 60%; filter: drop-shadow(0 10px 14px #0006); animation: lift ${String(MOTION_MS.appear)}ms ${EASE} 1; }
@keyframes lift { from { transform: none; filter: drop-shadow(0 0 0 #0000); } }
.spotlight .card { --w: var(--lw); }
.spotlight .actions__wonders .card { --w: 100px; }
.spotlight__beside { position: absolute; display: flex; flex-direction: column; gap: 8px; transform: translateY(-50%); animation: rise ${String(MOTION_MS.appear)}ms ${EASE} ${String(MOTION_MS.appear / 2)}ms backwards; }
@keyframes rise { from { opacity: 0; } }
.spotlight.is-leaving > * { pointer-events: none; }
.spotlight.is-leaving .spotlight__scrim { animation: scrim-out ${String(MOTION_MS.appear)}ms ${EASE} forwards; }
.spotlight.is-leaving .spotlight__card { animation: lift-out ${String(MOTION_MS.appear)}ms ${EASE} forwards; }
.spotlight.is-leaving .spotlight__beside { animation: scrim-out ${String(MOTION_MS.appear / 2)}ms ${EASE} forwards; }
@keyframes scrim-out { to { opacity: 0; } }
@keyframes lift-out { to { transform: none; filter: drop-shadow(0 0 0 #0000); } }
.slot.is-selected { visibility: hidden; }
.structure { position: relative; }
@media (prefers-reduced-motion: reduce) { .spotlight__scrim, .spotlight__card, .spotlight__beside { animation: none; } }
.spotlight h4 { margin: 0; text-align: center; font: 700 15px var(--serif); }
.actions__buttons { display: flex; flex-direction: column; gap: 8px; }
.actions__wonders { display: grid; grid-template-columns: repeat(2, auto); gap: 8px; }
.actions__wonders button { flex: none; padding: 4px; border-radius: 6px; }
.spotlight button { display: flex; flex-direction: column; align-items: center; gap: 2px; padding: 8px 16px; min-height: 40px; background: var(--surface); border: 1px solid var(--ink); border-radius: 20px; box-shadow: 0 4px 12px #0004; font-weight: 600; cursor: pointer; }
.spotlight button:disabled { opacity: .5; cursor: default; }
.spotlight button small { font-size: 11px; color: var(--muted); }
.spotlight .price { display: inline-flex; align-items: center; gap: 3px; font-size: 12px; }
button.slot:focus-visible { outline: 3px solid var(--ink); outline-offset: 2px; }
@media (prefers-reduced-motion: reduce) { .slot, .slot__quote { transition: none; } }
`;
