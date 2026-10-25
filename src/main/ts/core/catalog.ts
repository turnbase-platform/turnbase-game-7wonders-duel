import type { CatalogEntry, CatalogView, ComponentId } from "./wire.js";

/** Lookup for the static data that views and events refer to by id. */
export interface Catalog {
  get(id: ComponentId): CatalogEntry | undefined;
  /** The component's name; the id itself for one the catalog does not know. */
  name(id: ComponentId): string;
  /** The value of a `prefix:value` tag (`color:brown` → `brown`), or null. */
  tag(id: ComponentId, prefix: string): string | null;
}

export function makeCatalog(view: CatalogView): Catalog {
  const byId = new Map(view.entries.map((entry) => [entry.id, entry]));
  return {
    get: (id) => byId.get(id),
    name: (id) => byId.get(id)?.name ?? id,
    tag: (id, prefix) => {
      const head = `${prefix}:`;
      const found = byId.get(id)?.tags.find((t) => t.startsWith(head));
      return found === undefined ? null : found.slice(head.length);
    },
  };
}
