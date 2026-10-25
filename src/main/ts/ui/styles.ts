// The first screen is deliberately plain; plans 13-15 replace it piece by piece. The stylesheet
// is a string the bundle injects itself, because the iframe may load scripts only (CSP
// style-src allows inline styles, not a second file; game-asset-serving.md §6).
export const STYLES = `
:root { color-scheme: light dark; font: 14px system-ui, sans-serif; }
body { margin: 0; padding: 8px; }
.header { display: flex; gap: 16px; flex-wrap: wrap; align-items: baseline; margin-bottom: 8px; }
.header .turn { font-weight: 600; }
.banner { padding: 6px 10px; margin-bottom: 8px; border-radius: 4px; background: #fdd; color: #600; }
.zone { margin: 4px 0; }
.zone h3 { margin: 0; font-size: 12px; opacity: 0.7; font-weight: 500; }
.zone ul { display: flex; flex-wrap: wrap; gap: 4px; list-style: none; margin: 2px 0; padding: 0; }
.card { padding: 3px 8px; border: 1px solid #8886; border-radius: 4px; }
.card.hidden { opacity: 0.55; }
.card.brown { border-color: #8b5a2b; } .card.grey { border-color: #888; }
.card.blue { border-color: #2a6fdb; } .card.green { border-color: #2a9d4b; }
.card.yellow { border-color: #d1a300; } .card.red { border-color: #d63a3a; }
.card.purple { border-color: #8a4fc7; }
.card button { margin-left: 4px; }
.log { margin-top: 12px; font-size: 12px; }
.log ol { padding-left: 18px; margin: 2px 0; }
.log .step { opacity: 0.6; }
`;

export function injectStyles(into: Document = document): void {
  const element = into.createElement("style");
  element.textContent = STYLES;
  into.head.appendChild(element);
}
