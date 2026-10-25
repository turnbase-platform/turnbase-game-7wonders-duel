import type { Catalog } from "./catalog.js";
import type { ComponentId, DuelView, PlayerId, ResourceCount } from "./wire.js";

/** Resources first, then the civil colours, military and guilds: stacks must not jump as cards are built. */
const COLOUR_ORDER = ["brown", "grey", "blue", "green", "yellow", "red", "purple"];
const RESOURCE_ORDER = ["wood", "clay", "stone", "glass", "papyrus"];

export interface Stack {
  colour: string;
  /** In the order built. */
  cards: ComponentId[];
}

export interface ScienceCount {
  symbol: string;
  count: number;
}

export interface CityWonder {
  id: ComponentId;
  built: boolean;
}

/** What one seat has in front of it. */
export interface City {
  coins: number;
  stacks: Stack[];
  wonders: CityWonder[];
  /** Progress tokens. */
  tokens: ComponentId[];
  /** Every science symbol in the game with how many the city holds; the symbols of Law count once, as its token. */
  science: ScienceCount[];
  /** The plain tally of resources the city's cards produce: the display line, not the price rule. */
  produces: ResourceCount[];
}

export function cityOf(view: DuelView, catalog: Catalog, player: PlayerId): City {
  const mine = (zone: string): ComponentId[] =>
    view.visible.filter((p) => p.zone === zone && p.key === player).map((p) => p.component);
  const built = mine("city");
  const isToken = (id: ComponentId): boolean => catalog.get(id)?.type === "TOKEN";
  const cards = built.filter((id) => !isToken(id));
  return {
    coins: view.players.find((p) => p.id === player)?.coins ?? 0,
    stacks: stacksOf(cards, catalog),
    wonders: [
      ...mine("wonders.unbuilt").map((id) => ({ id, built: false })),
      ...mine("wonders.built").map((id) => ({ id, built: true })),
    ].sort((a, b) => a.id.localeCompare(b.id)),
    tokens: built.filter(isToken),
    produces: producedBy(cards, catalog),
    science: scienceOf(built, catalog),
  };
}

/** Counts the symbols the catalog knows, so a symbol no card carries yet (an expansion's) appears with the first that does. */
function scienceOf(built: ComponentId[], catalog: Catalog): ScienceCount[] {
  const symbolsOf = (id: ComponentId): string[] =>
    (catalog.get(id)?.badges ?? []).filter((b) => b.kind === "science").flatMap((b) => b.of);
  const known = new Set(catalog.all().flatMap((e) => symbolsOf(e.id)));
  const held = built.flatMap(symbolsOf);
  return [...known].map((symbol) => ({ symbol, count: held.filter((h) => h === symbol).length }));
}

function stacksOf(cards: ComponentId[], catalog: Catalog): Stack[] {
  return COLOUR_ORDER.map((colour) => ({
    colour,
    cards: cards.filter((id) => catalog.tag(id, "color") === colour),
  })).filter((stack) => stack.cards.length > 0);
}

function producedBy(cards: ComponentId[], catalog: Catalog): ResourceCount[] {
  const tally = new Map<string, number>();
  for (const id of cards) {
    for (const badge of catalog.get(id)?.badges ?? []) {
      if (badge.kind !== "produces") continue;
      for (const resource of badge.of) {
        tally.set(resource, (tally.get(resource) ?? 0) + badge.amount);
      }
    }
  }
  return RESOURCE_ORDER.filter((r) => tally.has(r)).map((resource) => ({
    resource,
    count: tally.get(resource) ?? 0,
  }));
}
