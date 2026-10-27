import { EASE, MOTION_MS } from "../motion.js";

// Cues that are not a component moving (design §9): coins floating off a purse and the turn marker's
// pulse. Durations come from MOTION_MS; with reduced motion they are not drawn at all.
export const MOTION = `
.purse { position: relative; }
.float { position: absolute; left: 50%; top: -2px; font-weight: 700; pointer-events: none; opacity: 0; animation: float ${String(MOTION_MS.coins)}ms ${EASE} 1; }
.float.is-gain { color: var(--c-green); } .float.is-loss { color: var(--c-red); }
/* On the big coin of a city head the amount floats beside it, not over its number. */
.purse--big .float { left: auto; right: 100%; top: 50%; margin-right: 6px; font-size: 24px; text-shadow: 0 0 3px var(--surface), 0 0 3px var(--surface); animation-name: float-beside; }
@keyframes float-beside { 0% { opacity: 1; transform: translateY(-50%); } 100% { opacity: 0; transform: translateY(calc(-50% - 22px)); } }
@keyframes float { 0% { opacity: 1; transform: translate(-50%, 0); } 100% { opacity: 0; transform: translate(-50%, -22px); } }
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
@media (prefers-reduced-motion: reduce) { .float, .burn { display: none; } .turn.is-pulse { animation: none; } .starter { animation: none; } }
`;
