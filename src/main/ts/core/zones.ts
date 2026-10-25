import type { ComponentId, DuelView } from "./wire.js";

/** One place of the table: the cards the viewer sees there and how many it may not see. */
export interface ZoneGroup {
  zone: string;
  key: string;
  components: ComponentId[];
  hidden: number;
}

/** The view's visible and hidden entries gathered by place, in the order they first appear. */
export function groupZones(view: DuelView): ZoneGroup[] {
  const groups = new Map<string, ZoneGroup>();
  const place = (zone: string, key: string): ZoneGroup => {
    const id = `${zone}\u0000${key}`;
    let group = groups.get(id);
    if (group === undefined) {
      group = { zone, key, components: [], hidden: 0 };
      groups.set(id, group);
    }
    return group;
  };
  for (const placed of view.visible)
    place(placed.zone, placed.key).components.push(placed.component);
  for (const down of view.hidden) place(down.zone, down.key).hidden += down.count;
  return [...groups.values()];
}
