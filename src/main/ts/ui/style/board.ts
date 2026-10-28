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
.structure { padding: 4px 0 10px; box-sizing: border-box; flex: 1; min-width: 0; min-height: 0; overflow: auto; display: flex; flex-direction: column; align-items: center; gap: 8px; }
.board-tokens { display: flex; flex-direction: column; align-items: center; flex: none; }
.board-tokens__ask { margin: 0 0 6px; font: 700 14px var(--serif); }
.token-pick { padding: 0; background: none; border: 0; border-radius: 50%; cursor: pointer; line-height: 0; filter: drop-shadow(0 2px 3px #0005); transition: transform .15s, filter .15s; }
.token-pick:hover, .token-pick:focus-visible { transform: translateY(-4px) scale(1.06); filter: drop-shadow(0 6px 6px #0006); outline: none; }
@media (prefers-reduced-motion: reduce) { .token-pick { transition: none; } }
.tray__tokens { display: flex; gap: 6px; justify-content: center; }
.layout-wide .board-tokens { align-self: stretch; }
.panel { box-sizing: border-box; min-height: 0; overflow: auto; }
.layout-wide .main { display: grid; grid-template-columns: clamp(190px, 24%, 330px) minmax(0, 1fr) clamp(190px, 24%, 330px); }
.city-col { box-sizing: border-box; min-height: 0; overflow: auto; }
/* One surface for every board component: the strip, the token tray, the panels and the city columns. */
.strip, .layout-wide .board-tokens, .panel, .city-col { padding: 10px; background: var(--surface); border: 1px solid var(--line); border-radius: 6px; box-shadow: none; }
.outcome__scrim { position: fixed; inset: 0; z-index: 30; display: grid; place-items: center; padding: 16px; background: color-mix(in srgb, var(--ground) 45%, transparent); backdrop-filter: blur(6px); animation: scrim ${String(MOTION_MS.appear)}ms ${EASE} 1; }
.outcome { max-width: min(100%, 420px); max-height: calc(100dvh - 32px); overflow: auto; text-align: center; padding: 20px 24px; background: var(--surface); border: 2px solid var(--ink); border-radius: 12px; box-shadow: 0 8px 32px #0006; }
.outcome h2 { margin: 0; font: 700 30px var(--serif); }
.outcome p { margin: 4px 0 0; color: var(--muted); }
.outcome button, .outcome__reopen { margin-top: 14px; padding: 6px 14px; font: inherit; font-weight: 600; color: var(--ink); background: var(--surface); border: 1px solid var(--ink); border-radius: 8px; cursor: pointer; }
.outcome__reopen { position: fixed; top: 8px; left: 50%; transform: translateX(-50%); z-index: 30; margin: 0; }
.outcome__scores { width: 100%; margin-top: 14px; border-collapse: collapse; font-variant-numeric: tabular-nums; }
.outcome__scores th, .outcome__scores td { padding: 4px 8px; border-bottom: 1px solid var(--line); }
.outcome__scores thead th { font-size: 12px; font-weight: 600; color: var(--muted); }
.outcome__scores tbody th { text-align: left; font-weight: 500; }
.outcome__scores td, .outcome__scores thead th:not(:first-child) { text-align: right; }
.outcome__scores tfoot th { text-align: left; } .outcome__scores tfoot th, .outcome__scores tfoot td { font-size: 16px; font-weight: 700; border-bottom: 0; border-top: 2px solid var(--ink); }
.outcome__scores .is-winner { color: var(--ink); text-decoration: underline; text-underline-offset: 3px; }
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
.city__head { position: relative; display: flex; align-items: center; gap: 4px 12px; margin-bottom: 28px; padding-bottom: 12px; border-bottom: 3px solid var(--seat); }
.city__head h3 { margin: 0; font: 700 15px var(--serif); }
.city__turn { width: 10px; height: 10px; border-radius: 50%; background: var(--seat); box-shadow: 0 0 0 3px color-mix(in srgb, var(--seat) 30%, transparent); }
.city__head .purse--big { position: absolute; right: 4px; top: 100%; transform: translateY(-50%); }
.purse--big { display: inline-grid; place-items: center; border-bottom: 0; font-size: 46px; }
.purse--big .icon { grid-area: 1 / 1; width: 1em; height: 1em; }
.purse--big b { grid-area: 1 / 1; position: relative; z-index: 1; font: 800 .42em / 1 var(--figures); font-variant-numeric: lining-nums; color: #1b1a17; text-shadow: 0 0 2px #fff; }
.city-col .stack { --sw: 64px; }
.city-col .city .wonders { --ww-max: 128px; }
.city-col .city { border-left: 0; padding-left: 0; }
.layout-landscape .panel { width: clamp(160px, 22%, 240px); flex: none; }
.layout-landscape .city .wonders { --ww-max: 92px; }
.layout-medium .panel { max-height: 40%; flex: none; }
.layout-portrait .panel { flex: 1; }
/* Short frames: the stage is the strip beside the structure, the stagebar beneath both. */
.is-rail .stage { display: grid; grid-template-columns: auto minmax(0, 1fr); grid-template-rows: minmax(0, 1fr) auto; grid-template-areas: "strip structure" "bar bar"; }
.is-rail .structure { grid-area: structure; }
.is-rail .stagebar { grid-area: bar; }
.is-rail .board-tokens:not(:has(.board-tokens__ask)) { position: absolute; z-index: 2; right: 6px; top: 6px; padding: 4px; }
.is-rail .tray__tokens { flex-direction: column; gap: 4px; }
.is-rail .structure:has(.board-tokens:not(:has(.board-tokens__ask))) { padding-right: 52px; }
.science { display: flex; flex-wrap: wrap; gap: 3px; margin: 0 0 8px; padding: 0; list-style: none; }
.city__head .science { position: absolute; right: 64px; top: 100%; transform: translateY(-50%); margin: 0; }
.science__symbol { position: relative; display: grid; place-items: center; width: 22px; height: 22px; border: 1px solid var(--line); border-radius: 50%; background: var(--surface); filter: grayscale(1); }
.science__symbol .icon { width: 15px; height: 15px; }
.science__symbol.is-held { filter: none; border-color: var(--ink); }
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
.stack { --sw: 56px; display: flex; flex-direction: column; width: var(--sw); }
.stack .card { --w: var(--sw); }
/* Under the next card only the banner of the one before shows: its effects and chain symbol. */
.stack .peekable + .peekable { margin-top: calc(var(--sw) * -1.0333 + 2px); }
.wonders { --ww-max: 112px; container-type: inline-size; display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 8px; margin-bottom: 8px; }
/* Two columns always: a Wonder (and the card tucked under it) is never wider than half of what it is given. */
.wonders .peekable { --ww: min(var(--ww-max), calc((100cqw - 8px) / 2.2)); display: flex; justify-content: center; position: relative; }
.wonders .card--wonder.card--standard { --w: var(--ww); }
/* A built Wonder has the card that built it slid in from the right, face down: a standard card laid
   on its side, smaller than the Wonder, with only its short edge sticking out. */
.wonder { position: relative; display: block; margin-right: calc(var(--ww) * .1); }
.wonder__under { position: absolute; z-index: 0; top: calc(var(--ww) * .0625); left: calc(var(--ww) * .267); }
.wonder__under .card, .peek .wonder__under .card { --w: calc(var(--ww) * .833); aspect-ratio: 4 / 3; }
.wonder > .card { z-index: 1; }
.peekable { padding: 0; border: 0; background: none; line-height: 0; cursor: pointer; border-radius: var(--radius); }
.peekable:focus-visible { outline: 2px solid var(--ink); outline-offset: 1px; }
/* The inspected card takes no room in the column, so showing it never makes the column scroll; it rides the bottom edge of what is in view. */
.peek { --ww: 240px; position: sticky; z-index: 5; bottom: 0; height: 0; pointer-events: none; }
.peek > * { position: absolute; bottom: 0; left: 50%; transform: translateX(-50%); }
/* Panels scroll when they must, by wheel or touch, and never draw a bar. */
.structure, .panel, .city-col, .drawer { scrollbar-width: none; }
.structure::-webkit-scrollbar, .panel::-webkit-scrollbar, .city-col::-webkit-scrollbar, .drawer::-webkit-scrollbar { display: none; }
.peek .card--large { --w: 200px; } .peek .card--wonder.card--large { --w: var(--ww); } .peek .token--large { width: 140px; }
.layout-landscape .peek { --ww: 200px; } .layout-landscape .peek .card--large { --w: 160px; }
.log { margin-top: 8px; font-size: 12px; }
.log ol { padding-left: 18px; margin: 2px 0; }
.log .step { opacity: .6; }
.phase-panel { text-align: center; padding: 8px; }
.phase-panel h2 { font: 700 1.2em var(--serif); margin: 4px 0 12px; }
.choices { display: flex; flex-wrap: wrap; gap: 12px; justify-content: center; }
.choice { display: flex; flex-direction: column; align-items: center; gap: 6px; }
.phase-panel button { padding: 6px 14px; background: var(--surface); border: 1px solid var(--ink); border-radius: var(--radius); cursor: pointer; }
.draft .card--wonder.card--large { --w: clamp(128px, 22vw, 260px); }
.draft__option { padding: 6px; border: 2px solid transparent; border-radius: var(--radius); background: none; line-height: 0; cursor: pointer; transition: transform ${String(MOTION_MS.travel)}ms ${EASE}, opacity ${String(MOTION_MS.travel)}ms ${EASE}; }
.draft__option:focus-visible { outline: 2px solid var(--ink); outline-offset: 2px; }
.draft__option.is-picked { border-color: var(--c-yellow); transform: translateY(-6px); }
.draft:has(.is-picked) .draft__option:not(.is-picked) { opacity: .6; }
.draft__confirm { position: sticky; bottom: 0; display: flex; justify-content: center; padding: 12px 0 calc(8px + env(safe-area-inset-bottom, 0px)); background: linear-gradient(transparent, var(--surface) 40%); }
.phase-panel .draft__confirm button { min-height: 44px; padding: 8px 24px; font-weight: 700; }
.phase-panel .draft__confirm button:disabled { opacity: .5; cursor: default; }
.prompt { flex: none; border-bottom: 2px solid var(--me); }
.prompt .card { --w: 96px; }
.choice--hidden { display: grid; place-items: center; width: 96px; aspect-ratio: 2 / 3; border: 2px dashed var(--line); border-radius: var(--radius); color: var(--muted); font-size: 24px; }
.pyramid-frame { width: 100%; flex: 1; min-height: 0; display: flex; justify-content: center; align-items: flex-start; }
.pyramid { --cw: 75px; position: relative; flex: none; }
.pyramid .card { --w: var(--cw); }
.pyramid__cards { position: absolute; inset: 0; transition: transform ${String(MOTION_MS.travel)}ms ${EASE}; }
.slot { position: absolute; padding: 0; border: 0; background: none; line-height: 0; }
.slot.is-covered { filter: brightness(.85) saturate(.8); }
.slot__quote { position: fixed; top: 0; left: 0; display: block; padding: 6px 8px; white-space: nowrap; line-height: 1.3; font-size: 12px; font-weight: 600; background: var(--surface); border: 1px solid var(--ink); border-radius: 8px; box-shadow: 0 2px 8px #0004; opacity: 0; pointer-events: none; transition: opacity ${String(MOTION_MS.appear / 2)}ms ${EASE}; z-index: 20; }
.slot__quote .tip__name { display: block; } .slot__quote .tip__text { display: block; width: 220px; margin-top: 2px; white-space: normal; font-weight: 500; }
.quote { display: grid; grid-template-columns: 26px repeat(5, minmax(46px, 1fr)); gap: 3px 0; text-align: center; font-variant-numeric: tabular-nums; }
.quote [role="row"] { display: contents; }
.quote [role="columnheader"] { font-size: 10px; font-weight: 500; color: var(--muted); padding-bottom: 3px; border-bottom: 1px solid var(--line); }
.quote [role="rowheader"], .quote [role="cell"] { display: grid; place-items: center; min-height: 22px; }
.quote .icon { width: 18px; height: 18px; }
.quote__sum [role="rowheader"] { grid-column: 1 / 6; place-items: center end; padding-right: 6px; }
.quote__sum [role="cell"] { border-left: 1px solid var(--line); }
.quote__total { font-size: 13px; }
.quote--brief { display: block; }
.quote + .quote--brief { margin-top: 4px; padding-top: 4px; border-top: 1px solid var(--line); }
button.slot { cursor: pointer; border-radius: var(--radius); }
.slot.is-takeable { box-shadow: 0 0 0 2px var(--me), 0 0 10px 2px color-mix(in srgb, var(--me) 55%, transparent); z-index: 1; }
/* The selected card: the structure blurs, the card rises where it stands, its actions float beside it. */
.spotlight { --lw: 75px; position: absolute; inset: 0; z-index: 12; pointer-events: none; }
.spotlight > * { pointer-events: auto; }
.spotlight__scrim { position: absolute; inset: 0; background: color-mix(in srgb, var(--ground) 20%, transparent); backdrop-filter: blur(2px); animation: scrim ${String(MOTION_MS.appear)}ms ${EASE} 1; }
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
.actions__wonders button:disabled { pointer-events: none; }
.spotlight button { display: flex; flex-direction: column; align-items: center; gap: 2px; padding: 8px 16px; min-height: 40px; background: var(--surface); border: 1px solid var(--ink); border-radius: 20px; box-shadow: 0 4px 12px #0004; font-weight: 600; cursor: pointer; }
.spotlight button:disabled { opacity: .5; cursor: default; }
.spotlight button small { font-size: 11px; color: var(--muted); }
.spotlight .price { display: inline-flex; align-items: center; gap: 3px; font-size: 12px; }
button.slot:focus-visible { outline: 3px solid var(--ink); outline-offset: 2px; }
@media (prefers-reduced-motion: reduce) { .pyramid__cards, .slot__quote { transition: none; } }
`;
