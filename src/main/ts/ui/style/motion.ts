import { EASE, MOTION_MS } from "../motion.js";

// Cues that are not a component moving (design §9): small coins flying into and out of a purse and the turn marker's
// pulse. Durations come from MOTION_MS; with reduced motion they are not drawn at all.
export const MOTION = `
.purse { position: relative; }
/* Small coins rise from below into the purse's coin when it gains, and sink out of it when it loses. They sit over the whole page. */
.coinfx { --r: 40px; position: fixed; z-index: 60; width: 0; height: 0; pointer-events: none; }
.coinfx__coin { --x: 0; --y: 1; --delay: 0ms; --travel: 500ms; position: absolute; left: 0; top: 0; width: 1em; height: 1em; margin: -.5em 0 0 -.5em; line-height: 0; opacity: 0; filter: drop-shadow(0 1px 2px #000a); animation: var(--travel) var(--delay) ${EASE} both 1; }
.coinfx__coin .icon { width: 100%; height: 100%; }
.coinfx.is-gain .coinfx__coin { animation-name: coin-in; }
.coinfx.is-loss .coinfx__coin { animation-name: coin-out; }
@keyframes coin-in { 0% { opacity: 0; transform: translate(calc(var(--x) * var(--r)), calc(var(--y) * var(--r))) scale(1.1); } 20% { opacity: 1; } 85% { opacity: 1; } 100% { opacity: 0; transform: translate(0, 0) scale(.5); } }
@keyframes coin-out { 0% { opacity: 1; transform: translate(0, 0) scale(.6); } 80% { opacity: 1; } 100% { opacity: 0; transform: translate(calc(var(--x) * var(--r)), calc(var(--y) * var(--r))) scale(1.1); } }
.turn.is-pulse { display: inline-block; animation: pulse ${String(MOTION_MS.pulse)}ms ${EASE} 1; }
@keyframes pulse { 0% { transform: scale(1); } 40% { transform: scale(1.25); } 100% { transform: scale(1); } }
/* A discarded card burns from the bottom up: a mask sweeps the card away while a band of fire rides its edge. */
.burn { --bw: 75px; position: fixed; z-index: 14; pointer-events: none; line-height: 0; overflow: hidden; border-radius: 6px; animation: burn-life ${String(MOTION_MS.burn)}ms linear 1; }
@keyframes burn-life { from { opacity: 1; } to { opacity: 1; } }
.burn .card { --w: var(--bw); }
.burn__card { mask-image: linear-gradient(to bottom, #000 50%, transparent 66.7%); mask-size: 100% 300%; mask-repeat: no-repeat; animation: burn-away ${String(MOTION_MS.burn)}ms cubic-bezier(.3,.1,.6,1) forwards; }
@keyframes burn-away { 0% { mask-position: 0 0; filter: none; } 30% { filter: sepia(1) saturate(4) hue-rotate(-25deg); } 100% { mask-position: 0 100%; filter: sepia(1) brightness(.3); } }
.burn__fire { position: absolute; inset: 0; background: linear-gradient(to bottom, transparent 38%, #ffe27a 46%, #ff7a1a 51%, #b5260a 57%, transparent 64%); animation: burn-edge ${String(MOTION_MS.burn)}ms cubic-bezier(.3,.1,.6,1) forwards; }
@keyframes burn-edge { from { transform: translateY(125%); } to { transform: translateY(-75%); } }
/* The pop-up of who starts a round: it rises in, holds, and fades out on its own. */
.starter { position: fixed; z-index: 30; left: 50%; top: 38%; transform: translate(-50%, -50%); pointer-events: none; text-align: center; padding: 14px 32px; background: var(--surface); color: var(--ink); border: 2px solid var(--c-yellow); border-radius: 10px; box-shadow: 0 8px 30px rgba(0,0,0,.35); animation: starter-life ${String(MOTION_MS.starter)}ms ${EASE} 1 forwards; }
.starter__round { font-size: 12px; letter-spacing: .12em; text-transform: uppercase; color: var(--muted); }
.starter__who { font: 700 28px var(--serif); }
@keyframes starter-life { 0% { opacity: 0; transform: translate(-50%, -30%) scale(.9); } 12% { opacity: 1; transform: translate(-50%, -50%) scale(1.04); } 20% { transform: translate(-50%, -50%) scale(1); } 85% { opacity: 1; } 100% { opacity: 0; transform: translate(-50%, -58%) scale(1); } }
@media (prefers-reduced-motion: reduce) { .coinfx, .burn { display: none; } .turn.is-pulse { animation: none; } .starter { animation: none; } }
`;
