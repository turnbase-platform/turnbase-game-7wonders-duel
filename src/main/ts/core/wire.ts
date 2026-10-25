// What the game backend sends and receives. Hand-written because the GDK has no schema generator;
// src/test/resources/wire/*.json pins the shapes from the Java side (WireFixturesTest) and
// wire.test.ts checks them from this side. Ids are strings (components, zones) or UUID strings
// (players). A null is a field that does not apply to the kind.

export type PlayerId = string;
export type ComponentId = string;

export type Phase = "SETUP" | "WONDER_DRAFT" | "AGE" | "OVER";
export type VictoryReason = "MILITARY" | "SCIENCE" | "POINTS" | "FORFEIT";

export interface PlayerView {
  id: PlayerId;
  coins: number;
  playAgain: boolean;
}

export interface PlacedView {
  component: ComponentId;
  zone: string;
  key: string;
}

export interface HiddenView {
  zone: string;
  key: string;
  back: string | null;
  count: number;
}

export interface PendingView {
  decider: PlayerId;
  options: string[];
  hidden: number;
}

/** How one resource of a cost is met: needed = produced + discounted + bought. */
export interface QuoteLineView {
  resource: string;
  needed: number;
  produced: number;
  discounted: number;
  bought: number;
  unitPrice: number;
}

export interface QuoteView {
  player: PlayerId;
  component: ComponentId;
  coins: number;
  tradeCost: number;
  total: number;
  chained: boolean;
  affordable: boolean;
  /** Exposed, so it may be taken now; false for covered cards and Wonders. */
  takeable: boolean;
  lines: QuoteLineView[];
}

export interface DuelView {
  phase: Phase;
  active: PlayerId;
  age: number;
  first: PlayerId;
  deciding: PlayerId | null;
  players: PlayerView[];
  pawnSide: PlayerId | null;
  pawnSpace: number;
  victoryWinner: PlayerId | null;
  victoryReason: VictoryReason | null;
  visible: PlacedView[];
  hidden: HiddenView[];
  pending: PendingView | null;
  quotes: QuoteView[];
}

export interface PlaceView {
  zone: string;
  key: string;
}

export interface PawnView {
  side: PlayerId | null;
  space: number;
}

export type EventKind =
  "moved" | "coins" | "trade" | "chain" | "pawn" | "playAgainGranted" | "playAgainCleared";

export interface EventView {
  id: number;
  /** The id of the event that caused this one, -1 for a root event. */
  parent: number;
  kind: EventKind;
  cause: ComponentId | null;
  component: ComponentId | null;
  from: PlaceView | null;
  to: PlaceView | null;
  player: PlayerId | null;
  amount: number;
  pawnFrom: PawnView | null;
  pawnTo: PawnView | null;
}

export interface Badge {
  kind: string;
  amount: number;
  of: string[];
  per: number;
  larger: boolean;
}

export interface ResourceCount {
  resource: string;
  count: number;
}

export interface CostView {
  coins: number;
  resources: ResourceCount[];
  freedBy: string[];
}

export type ComponentType = "CARD" | "WONDER" | "TOKEN";

export interface CatalogEntry {
  id: ComponentId;
  name: string;
  type: ComponentType;
  tags: string[];
  back: string | null;
  cost: CostView;
  badges: Badge[];
}

export interface CatalogView {
  entries: CatalogEntry[];
}

/** A picture of the game, or the reason an action was refused. */
export interface ServerMessage {
  kind: "view" | "rejected";
  view: DuelView | null;
  reason: string | null;
  /** What happened to bring the game to `view`, as this client may know it. */
  events: EventView[];
  /** Sent with a resync only. */
  catalog: CatalogView | null;
  /** The public message sent to everybody; a seat also gets its own and skips this one. */
  broadcast: boolean;
}

export type WireAction =
  | { type: "draftWonder"; component: ComponentId }
  | { type: "chooseStarter"; player: PlayerId }
  | { type: "discard"; component: ComponentId }
  | { type: "build"; component: ComponentId }
  | { type: "buildWonder"; component: ComponentId; wonder: ComponentId }
  | { type: "answerComponent"; component: ComponentId }
  | { type: "answerPlayer"; player: PlayerId };
