import { CARD } from "./style/card.js";
import { TOKENS } from "./style/tokens.js";

// SCREEN is the deliberately plain first screen; plans 13b-15 replace it piece by piece. The stylesheet
// is a string the bundle injects itself, because the iframe may load scripts only (CSP
// style-src allows inline styles, not a second file; game-asset-serving.md §6).
const SCREEN = `
:root { font: 14px system-ui, sans-serif; }
body { margin: 0; padding: 8px; }
.header { display: flex; gap: 16px; flex-wrap: wrap; align-items: baseline; margin-bottom: 8px; }
.header .turn { font-weight: 600; }
.banner { padding: 6px 10px; margin-bottom: 8px; border-radius: 4px; background: #fdd; color: #600; }
.zone { margin: 4px 0; }
.zone h3 { margin: 0; font-size: 12px; opacity: 0.7; font-weight: 500; }
.zone ul { display: flex; flex-wrap: wrap; gap: 4px; list-style: none; margin: 2px 0; padding: 0; }
.chip { padding: 3px 8px; border: 1px solid #8886; border-radius: 4px; }
.chip.hidden { opacity: 0.55; }
.chip.brown { border-color: #8b5a2b; } .chip.grey { border-color: #888; }
.chip.blue { border-color: #2a6fdb; } .chip.green { border-color: #2a9d4b; }
.chip.yellow { border-color: #d1a300; } .chip.red { border-color: #d63a3a; }
.chip.purple { border-color: #8a4fc7; }
.chip button { margin-left: 4px; }
.log { margin-top: 12px; font-size: 12px; }
.log ol { padding-left: 18px; margin: 2px 0; }
.log .step { opacity: 0.6; }
`;

export const STYLES = TOKENS + CARD + SCREEN;

export function injectStyles(into: Document = document): void {
  const element = into.createElement("style");
  element.textContent = STYLES;
  into.head.appendChild(element);
}
