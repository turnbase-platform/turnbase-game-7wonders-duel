import type { ResourceCount } from "./wire.js";

/** Manufactured goods come before the raw materials, each group in a fixed order. */
const ORDER = ["glass", "papyrus", "wood", "clay", "stone"];

const rank = (resource: string): number => {
  const at = ORDER.indexOf(resource);
  return at < 0 ? ORDER.length : at;
};

/** A cost's resources as they are drawn: manufactured goods, then raw materials, grouped by resource. */
export function inCostOrder(resources: ResourceCount[]): ResourceCount[] {
  return [...resources].sort((a, b) => rank(a.resource) - rank(b.resource));
}
