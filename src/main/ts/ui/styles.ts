import { BOARD } from "./style/board.js";
import { CARD } from "./style/card.js";
import { TOKENS } from "./style/tokens.js";

// The stylesheet is a string the bundle injects itself, because the iframe may load scripts only (CSP
// style-src allows inline styles, not a second file; game-asset-serving.md §6).
export const STYLES = TOKENS + CARD + BOARD;

export function injectStyles(into: Document = document): void {
  const element = into.createElement("style");
  element.textContent = STYLES;
  into.head.appendChild(element);
}
