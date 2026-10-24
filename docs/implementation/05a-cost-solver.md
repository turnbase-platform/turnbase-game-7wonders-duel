# Cost Solver Implementation Plan (5a)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans (native, inline). Steps use checkbox (`- [ ]`) syntax.

**Goal:** A pure function `CostSolver.quote(state, player, target)` answering B-COST-1..5, B-PROD-1..4, B-TRADE-1..3 and B-RED-1: the minimum coin outlay to construct a card or Wonder, or that it is free by chain. Nothing is paid or moved here; Build (5b) consumes the quote.

**Architecture:** Costs are declarative data on the component (`Cost`). Everything the economy varies is a narrow `Modifier` read from a player's live components: `Production` (units a city makes), `PriceModifier` (what a unit costs to buy), `Reduction` (symbols removed from a target's cost), `ChainSource` (symbols the city holds). The solver derives all of it from placements (B-EFF-1), so Masonry, Architecture, trade cards and expansions are content, not code. Resource and chain symbols are open `String` records like `Tag`, so an expansion adds its own.

**Tech Stack:** Java 21, JUnit 6, AssertJ; same build as 4c.

**Spec:** `docs/rules/base.md` §4 (B-COST, B-PROD, B-TRADE, B-RED, chains), §10.2.

**Decided with the user:** data + modifiers (not a per-rule solver); 5 = 5a solver → 5b Build/BuildWonder → 5c military → 5d science/tokens. Out of scope here: paying (5b), the payee/Economy hook (5b), Wonder shield costs and every expansion modifier (carried; they are additions to the same interfaces), real card data (none exists; tests use fixtures — do not invent any).

## Global Constraints

- New package `cost`; domain code never imports `snapshot`. Nothing in `cost` mutates state.
- `Resource(String id)` and `ChainSymbol(String id)` reject blank ids; base constants `WOOD, STONE, CLAY, GLASS, PAPYRUS` live on `Resource`.
- `Component.cost()` defaults to `Cost.FREE`; the solver treats an empty cost like any other.
- Unit price: `2 + opponent's units of r from Production flagged raisesOpponentPrice that make exactly r` (B-TRADE-2), then each own `PriceModifier` in placement order (ordering between several is undefined in the rules; carried).
- Search is exhaustive over reductions × choice-producer assignments (≤ 7 symbols per cost); no heuristics.

## Review Focus

- A chain symbol held frees the target entirely, coins included, and is always applied (B-COST-5).
- Opponent choice producers and any non-flagged production never raise the price (B-TRADE-2); the player's own production never does.
- A 1$ trade card applies as soon as it is in the city, whatever zone it came by, and does not make a resource cheaper than the modifier says (B-TRADE-3).
- Reductions remove the most expensive symbols given production, not the first listed; two choice producers that could each cover the same symbol are not double-counted (B-PROD-4).
- Needing a resource nobody can supply is still quotable (bought at the unit price, B-TRADE-1); payability is only a comparison with the treasury.
- A reduction larger than the cost, a target with no resource cost, and a reduction that does not apply to the target type are all harmless.

---

### Task 1: Cost values and the declarative cost

**Files:** Create `cost/{Resource,ChainSymbol,Cost}.java`; modify `catalog/Component.java`. Test `cost/CostTest`.

**Interfaces:** `record Resource(String id)` + constants; `record ChainSymbol(String id)`; `record Cost(int coins, Map<Resource,Integer> resources, Set<ChainSymbol> freedBy)` — rejects negative coins/counts and zero counts, copies defensively; `Cost.FREE`; `Cost.coins(int)`, `Cost.of(int coins, Resource... symbols)` (repeat a resource to count it) and `withFreedBy(ChainSymbol...)`; `int symbols()` total resource symbols. `Component.cost()` default `Cost.FREE`.

- [ ] Failing tests: blank ids rejected; constants distinct; negative coins/count rejected; `of(2, CLAY, CLAY, GLASS)` counts; `FREE` is free; defensive copy. RED (compile) → GREEN → commit `feat(cost): resources, chain symbols and the declarative Cost (B-COST-1..5)`.

### Task 2: Production, price and the solver without reductions or chains

**Files:** Create `cost/{Production,PriceModifier,Quote,CostSolver}.java`. Test `cost/CostSolverTest`.

**Interfaces:** `record Production(List<Set<Resource>> units, boolean raisesOpponentPrice) implements Modifier` (a unit's set is its alternatives; `Production.fixed(Resource, int n, boolean raises)`, `Production.choice(boolean raises, Resource...)` for one unit of one of); `interface PriceModifier extends Modifier { int price(Resource r, int current); }` plus `static PriceModifier fixedAt(int price, Resource... r)`; `record Quote(int coins, Map<Resource,Integer> bought, int tradeCost, boolean chained)` with `total()` and `payableWith(int treasury)`; `CostSolver(RuleSet)`, `Quote quote(DuelState, PlayerId, ComponentId target)`, `int unitPrice(DuelState, PlayerId, Resource)`.

- [ ] Failing tests with the rulebook's worked examples: opponent 1 Clay, no Papyrus → Clay+Papyrus = 5$; opponent 2 Stone → 3 Stone = 12$; Caravansery-style (2$ + Glass + Papyrus, opponent 1 Glass) = 7$; own fixed production covers symbols and is not consumed (quoting twice is equal); a choice producer covers one of two needs, never both (B-PROD-4); two choice producers assigned jointly to minimise cost; opponent choice producers and unflagged production do not raise the price; a 1$ card sets the price whatever zone-key it sits under only if in the city; payableWith compares total to treasury; unsupplied resource is bought. RED → GREEN → commit `feat(cost): production, unit prices and the cost solver (B-PROD, B-TRADE)`.

### Task 3: Reductions and chains

**Files:** Create `cost/{Reduction,ChainSource}.java`; modify `CostSolver`. Test `cost/CostSolverReductionTest`.

**Interfaces:** `record Reduction(int symbols, Predicate<Component> appliesTo) implements Modifier`; `record ChainSource(Set<ChainSymbol> symbols) implements Modifier`. Solver: chained when any held symbol is in `target.cost().freedBy()` → `Quote` with coins 0, nothing bought, `chained`; otherwise removal budget = Σ symbols of applicable reductions, searched exhaustively together with the choice-producer assignment.

- [ ] Failing tests for every Review Focus bullet (chain frees coins too; Masonry removes the dearest uncovered symbols; a cheap-by-production symbol is left alone; a reduction bigger than the cost; wrong target type; no resource cost). RED → GREEN → commit `feat(cost): reductions and chains (B-RED-1, B-COST-5)`.
