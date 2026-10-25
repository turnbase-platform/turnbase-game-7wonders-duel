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
@media (prefers-reduced-motion: reduce) { .float { display: none; } .turn.is-pulse { animation: none; } }
`;
