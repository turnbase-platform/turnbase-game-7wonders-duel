# 7 Wonders Duel — Base Game Rules (consolidated)

Source: `docs/rules/7-wonders-duel-base.pdf` (Repos Production, 2015, English rulebook, 20 pp.).
This document restates the rulebook as a **specification for the backend**: every rule has a stable ID
(`B-…`) so the engine, its tests and the expansion documents can cite it. Where the rulebook is
silent or ambiguous, the gap is marked **[OPEN-Bn]** and listed in §12. Where a value comes from
outside the rulebook, its source is stated.

Companion documents — each one is written as a delta against this one:

| Document | Scope |
|---|---|
| `base.md` (this) | Base game, the shared vocabulary, and the mechanics model all expansions extend |
| [`agora.md`](agora.md) | Official expansion — Senate, Senators, Conspiracies, Decrees, Political Supremacy |
| [`pantheon.md`](pantheon.md) | Official expansion — Divinities, Pantheon, Grand Temples, Mythology & Offering tokens |
| [`extra-wonders.md`](extra-wonders.md) | Community expansion — 77 Wonders; new mechanics live entirely on Wonder cards |

---

## 1. Glossary

| Term | Meaning |
|---|---|
| **City** | Everything a player has constructed: Buildings + constructed Wonders. |
| **Building** | Any Age card or Guild card once constructed. Has a **color** (§3.2). |
| **Age card** | A card in an Age deck (I, II, III). Guilds are shuffled into Age III and are Age cards too. |
| **Structure** | The face-up / face-down layout of 20 Age cards for the current Age (§5.2). |
| **Accessible card** | A card in the structure not partially covered by any other card. |
| **Bank** | Unlimited supply of coins. All payments go to the bank; all gains come from it unless stated. |
| **Treasury** | A player's coins. |
| **Box** | Out of the game since setup (removed cards, progress tokens, undrafted Wonders, unused Guilds). Effects that draw from "the components removed at setup" are queries over the Box (type, Age), see §10.4. |
| **Destroyed** | Out of the game for good: anything that enters it can never leave it. Holds components removed during play (used military tokens, Wonders removed by the limit or by an effect). Never a source for draws. |
| **Discard pile** | Face-down pile next to the board; **public** — players may consult it at any time. Holds only cards discarded during play, never setup removals. |
| **Shield** | Military strength; each shield moves the Conflict pawn one space. |
| **Conflict pawn** | Marker on the military track (§8). |
| **Capital** | The last space at each end of the military track. |
| **Play again / Replay** | The active player takes another full turn immediately. |
| **Active player** | The player whose turn it is. "You" in an effect = the player who triggers it. |
| **Opponent** | The other player. |

## 2. Overview and victory

- **B-OVR-1** Two players, three Ages played in order I → II → III.
- **B-OVR-2** Three ways to win:
  - **Military supremacy** — the Conflict pawn enters the opponent's capital (§8). *Immediate.*
  - **Scientific supremacy** — a player gathers 6 different scientific symbols (§9). *Immediate.*
  - **Civilian victory** — most victory points at the end of Age III (§11).
- **B-OVR-3** A supremacy victory ends the game immediately. Check points are defined in §10.9.

## 3. Components and data

### 3.1 Inventory

| Component | Count | Notes |
|---|---|---|
| Board (military track) | 1 | 19 spaces, 9 zones; holds military + progress tokens |
| Age I cards | 23 | 3 removed at setup → 20 in play |
| Age II cards | 23 | 3 removed at setup → 20 in play |
| Age III cards | 20 | 3 removed at setup; 3 random Guilds added → 20 in play |
| Guild cards | 7 | 3 used per game |
| Wonder cards | 12 | 8 used per game, max 7 constructed |
| Military tokens | 4 | 2× "loot 2", 2× "loot 5" |
| Progress tokens | 10 | 5 on the board per game |
| Conflict pawn | 1 | |
| Coins | 31 physical (14×1, 10×3, 7×6) | **Treat the bank as unlimited** — the physical count is not a game limit. |

### 3.2 Resources, colors, symbols

**Resources** (5):

| Kind | Resources | Produced by |
|---|---|---|
| Raw materials | Wood, Clay, Stone | brown cards, some yellow cards, some Wonders |
| Manufactured goods | Glass, Papyrus | grey cards, some yellow cards, some Wonders |

**Building colors** (7): brown (raw materials), grey (manufactured goods), blue (civilian), green
(scientific), yellow (commercial), red (military), purple (guild).

**Scientific symbols** (7; names are ours, the rulebook only draws them): Wheel, Mortar (mortar &
pestle), Plumb (A-frame with plumb bob), Quill (quill & inkwell), Sundial, Armillary (armillary
sphere / astrolabe), Scales (only on the *Law* progress token). Each of the first six appears on
exactly two green cards.

**Chain symbols** (17; white icons, names are ours): see §4.5 table.

### 3.3 Age I cards (23)

Cost legend: `W` Wood, `C` Clay, `S` Stone, `G` Glass, `P` Papyrus, `n$` coins, `—` free.
Chain: `gives ▸X` — the card grants chain symbol X; `◂X` — free if the city has chain symbol X.

| Card | Color | Cost | Effect | Chain |
|---|---|---|---|---|
| Lumber Yard | brown | — | W | |
| Logging Camp | brown | 1$ | W | |
| Clay Pool | brown | — | C | |
| Clay Pit | brown | 1$ | C | |
| Quarry | brown | — | S | |
| Stone Pit | brown | 1$ | S | |
| Glassworks | grey | 1$ | G | |
| Press | grey | 1$ | P | |
| Guard Tower | red | — | 1 shield | |
| Stable | red | W | 1 shield | gives ▸Horseshoe |
| Garrison | red | C | 1 shield | gives ▸Sword |
| Palisade | red | 2$ | 1 shield | gives ▸Tower |
| Workshop | green | P | Plumb, 1 VP | |
| Apothecary | green | G | Wheel, 1 VP | |
| Scriptorium | green | 2$ | Quill | gives ▸Book |
| Pharmacist | green | 2$ | Mortar | gives ▸Gear |
| Theater | blue | — | 3 VP | gives ▸Mask |
| Altar | blue | — | 3 VP | gives ▸Moon |
| Baths | blue | S | 3 VP | gives ▸Drop |
| Stone Reserve | yellow | 3$ | Stone trades at 1$ | |
| Clay Reserve | yellow | 3$ | Clay trades at 1$ | |
| Wood Reserve | yellow | 3$ | Wood trades at 1$ | |
| Tavern | yellow | — | +4$ (once) | gives ▸Amphora |

### 3.4 Age II cards (23)

| Card | Color | Cost | Effect | Chain |
|---|---|---|---|---|
| Sawmill | brown | 2$ | W W | |
| Brickyard | brown | 2$ | C C | |
| Shelf Quarry | brown | 2$ | S S | |
| Glass-Blower | grey | — | G | |
| Drying Room | grey | — | P | |
| Walls | red | S S | 2 shields | |
| Horse Breeders | red | C W | 1 shield | ◂Horseshoe |
| Barracks | red | 3$ | 1 shield | ◂Sword |
| Archery Range | red | S W P | 2 shields | gives ▸Target |
| Parade Ground | red | C C G | 2 shields | gives ▸Helmet |
| Library | green | S W G | Quill, 2 VP | ◂Book |
| Dispensary | green | C C S | Mortar, 2 VP | ◂Gear |
| School | green | W P P | Wheel, 1 VP | gives ▸Lyre |
| Laboratory | green | W G G | Plumb, 1 VP | gives ▸Lamp |
| Tribunal | blue | W W G | 5 VP | |
| Statue | blue | C C | 4 VP | ◂Mask, gives ▸Column |
| Temple | blue | W P | 4 VP | ◂Moon, gives ▸Sun |
| Aqueduct | blue | S S S | 5 VP | ◂Drop |
| Rostrum | blue | S W | 4 VP | gives ▸Senate |
| Forum | yellow | 3$ C | produces 1 of G / P (choice, per turn) | |
| Caravansery | yellow | 2$ G P | produces 1 of W / C / S (choice, per turn) | |
| Customs House | yellow | 4$ | Papyrus and Glass trade at 1$ | |
| Brewery | yellow | — | +6$ (once) | gives ▸Barrel |

### 3.5 Age III cards (20)

| Card | Color | Cost | Effect | Chain |
|---|---|---|---|---|
| Arsenal | red | C C C W W | 3 shields | |
| Courthouse | red | 8$ | 3 shields | |
| Fortifications | red | S S C P | 2 shields | ◂Tower |
| Siege Workshop | red | W W W G | 2 shields | ◂Target |
| Circus | red | C C S S | 2 shields | ◂Helmet |
| Academy | green | S W G G | Sundial, 3 VP | |
| Study | green | W W G P | Sundial, 3 VP | |
| University | green | C G P | Armillary, 2 VP | ◂Lyre |
| Observatory | green | S P P | Armillary, 2 VP | ◂Lamp |
| Palace | blue | C S W G G | 7 VP | |
| Town Hall | blue | S S S W W | 7 VP | |
| Obelisk | blue | S S G | 5 VP | |
| Gardens | blue | C C W W | 6 VP | ◂Column |
| Pantheon | blue | C W P P | 6 VP | ◂Sun |
| Senate | blue | C C S P | 5 VP | ◂Senate |
| Chamber of Commerce | yellow | P P | +3$ per grey card in your city (once); 3 VP | |
| Port | yellow | W G P | +2$ per brown card in your city (once); 3 VP | |
| Armory | yellow | S S G | +1$ per red card in your city (once); 3 VP | |
| Lighthouse | yellow | C C G | +1$ per yellow card in your city, itself included (once); 3 VP | ◂Amphora |
| Arena | yellow | C S W | +2$ per Wonder constructed in your city (once); 3 VP | ◂Barrel |

> **Naming note.** Later printings and the Pantheon rulebook call the 8-coin, 3-shield card
> *Pretorium* (French *Prétoire*); this rulebook calls it *Courthouse*. Same card. Use one canonical
> id and keep the other as an alias.

### 3.6 Guilds (7) — 3 random per game, shuffled into Age III

"The city with the most X" — the owner chooses **one** city (theirs or the opponent's) that has the
most X; on a tie the count is the same either way, so the engine takes `max(own, opponent)`. The coin choice (on construction) and the
VP choice (at game end) are made **independently** and may name different cities.

| Guild | Cost | On construction (once) | End of game |
|---|---|---|---|
| Merchants Guild | C W G P | +1$ per yellow card in the city with most yellow | 1 VP per yellow card in that city |
| Shipowners Guild | C S G P | +1$ per brown **and** grey card in the city with most brown+grey (one city for both colors) | 1 VP per brown+grey card in that city |
| Builders Guild | S S C W G | — | 2 VP per Wonder in the city with most Wonders |
| Magistrates Guild | W W C P | +1$ per blue card in the city with most blue | 1 VP per blue card in that city |
| Scientists Guild | C C W W | +1$ per green card in the city with most green | 1 VP per green card in that city |
| Moneylenders Guild | S S W W | — | 1 VP per 3$ in the richest city |
| Tacticians Guild | S S C P | +1$ per red card in the city with most red | 1 VP per red card in that city |

> The rulebook says "the city which has the most" — with a strict maximum the owner has no real
> choice, the engine can compute it. The choice only matters on a tie, where both cities give the
> same count. **Design consequence: no player decision is needed for guilds** — compute
> `max(own, opponent)`.

### 3.7 Wonders (12)

Costs are **not printed in the rulebook**; they come from the cards (cross-checked against the
reference implementation). Effects in the order printed — apply in that order.

| Wonder | Cost | Effects | VP |
|---|---|---|---|
| The Appian Way | S S C C P | +3$; opponent loses 3$; play again | 3 |
| Circus Maximus | S S W G | discard one grey card from the opponent's city; 1 shield | 3 |
| The Colossus | C C C G | 2 shields | 3 |
| The Great Library | W W W G P | draw 3 random progress tokens from the progress tokens **in the Box**; take 1, return 2 to the Box (they stay drawable by later effects); only the drawer sees the 3 | 4 |
| The Great Lighthouse | W S P P | produces 1 of W / C / S (choice, per turn) | 4 |
| The Hanging Gardens | W W G P | +6$; play again | 3 |
| The Mausoleum | C C G G P | look through the discard pile, construct 1 card from it for free | 2 |
| Piraeus | W W S C | produces 1 of G / P (choice, per turn); play again | 2 |
| The Pyramids | S S S P | — | 9 |
| The Sphinx | S C G G | play again | 6 |
| The Statue of Zeus | S W C P P | discard one brown card from the opponent's city; 1 shield | 3 |
| The Temple of Artemis | W S G P | +12$; play again | 0 |

Wonder shields are **not** boosted by *Strategy* (B-PRG table).

### 3.8 Progress tokens (10) — 5 on the board per game

| Token | Effect | Kind |
|---|---|---|
| Agriculture | +6$ immediately; 4 VP | instant + score |
| Architecture | future Wonders cost 2 fewer resources (player chooses which, per construction) | cost modifier |
| Economy | money the **opponent** spends on **trading** goes to you instead of the bank (not card coin costs) | trade hook |
| Law | grants the Scales scientific symbol | science |
| Masonry | future blue cards cost 2 fewer resources (player chooses which, per construction) | cost modifier |
| Mathematics | 3 VP per progress token you own, itself included | score |
| Philosophy | 7 VP | score |
| Strategy | your **future red cards** give +1 shield (not Wonders; not retroactive) | shield modifier |
| Theology | your **future Wonders** gain "play again" (a Wonder that already has it does not get it twice) | wonder modifier |
| Urbanism | +6$ immediately; +4$ each time you construct a Building **for free via a chain** — chain symbols granted by effects (Agora decree #15, Extra Wonders) count; *Engineering* does not | instant + chain hook |

Economy clarification: the opponent's trade discounts (reserves, Customs House) change the price; you
receive the **actual** amount they paid for goods.

### 3.9 Military tokens (4)

| Token | Location | Effect when the pawn enters the zone |
|---|---|---|
| Loot 2 | each side, zone at spaces 3–5 from centre | opponent of the advancing player loses 2$ (all they have if fewer); token → Destroyed |
| Loot 5 | each side, zone at spaces 6–8 from centre | opponent loses 5$ (all if fewer); token → Destroyed |

Lost coins go to the bank. A token is single-use: once applied it leaves the game.

### 3.10 Card backs (public information)

The **back** of every card identifies its group, so a face-down card in the structure is not fully
hidden: both players know which group it belongs to.

| Back | Group | Used by |
|---|---|---|
| Brown | Age I Building | base |
| Blue | Age II Building | base |
| Violet | Age III Building | base |
| Dark purple | Guild / Grand Temple | base (Guilds), Pantheon (Grand Temples) |
| Grey | Senator | Agora |

- **B-BACK-1** A face-down structure card exposes its back group to both players; only its identity
  is hidden. Per-viewer views must send the back group for every face-down slot.
- **B-BACK-2** Card **color** (the front: brown, grey, blue, green, yellow, red, purple — and Agora's
  white/black) is a different attribute from the **back** group. Effects only ever refer to color.

## 4. Construction and costs

### 4.1 Cost kinds

- **B-COST-1 Free** — no cost.
- **B-COST-2 Coins** — pay to the bank.
- **B-COST-3 Resources** — each listed resource must be produced by the city **or bought** (§4.3).
- **B-COST-4 Coins + resources** — pay both.
- **B-COST-5 Chain (free construction condition)** — if the city has the chain symbol, the card is
  free regardless of its cost (§4.5). The chain is **always** used when available; the engine offers
  no choice (paying instead is never better, and Urbanism rewards the chain).

### 4.2 Production

- **B-PROD-1** Resources are **not consumed**. A city's production is available every turn for the
  whole game. Production never decreases except when an effect removes a card.
- **B-PROD-2** Fixed producers: brown and grey cards (1 or 2 units of one resource).
- **B-PROD-3** Choice producers (Forum, Caravansery, Great Lighthouse, Piraeus): each produces
  **one unit of one** of the listed resources, chosen per construction.
- **B-PROD-4** Production is used per payment — the same unit satisfies one symbol of one cost.

### 4.3 Trading (buying resources)

- **B-TRADE-1** Any missing resource may be bought from the bank, without limit.
- **B-TRADE-2** Price per unit of resource *r* = **2 + number of *r* symbols produced by the
  opponent's brown and grey cards**. Choice producers (yellow cards, Wonders) of the opponent do not
  count.
- **B-TRADE-3** A "trades at 1$" card (Stone/Clay/Wood Reserve, Customs House) fixes the price of
  that resource at 1$ for its owner **as soon as the card is owned** — built or obtained any other
  way. The same holds for every price modifier (decrees, Wonders): there is no activation delay.
- **B-TRADE-4** Payment goes to the bank — or to the opponent if they own *Economy*.

Worked examples (from the rulebook): opponent has 1 Clay, no Papyrus → buying Clay+Papyrus costs
3+2 = 5$. Opponent has 2 Stone → each Stone costs 4$; 3 Stone = 12$. Caravansery (2$ G P) with the
opponent producing 1 Glass, none Papyrus → 2 + 3 + 2 = 7$.

### 4.4 Cost reduction ("N fewer resources")

- **B-RED-1** Masonry (blue cards) and Architecture (Wonders) remove **2 resource symbols of the
  player's choice** from the cost before trading. The player picks the most expensive ones; the
  engine should simply compute the minimum price.

### 4.5 Chains

| Symbol | Given by (Age) | Frees (Age) |
|---|---|---|
| Horseshoe | Stable (I) | Horse Breeders (II) |
| Sword | Garrison (I) | Barracks (II) |
| Tower | Palisade (I) | Fortifications (III) |
| Target | Archery Range (II) | Siege Workshop (III) |
| Helmet | Parade Ground (II) | Circus (III) |
| Book | Scriptorium (I) | Library (II) |
| Gear | Pharmacist (I) | Dispensary (II) |
| Lyre | School (II) | University (III) |
| Lamp | Laboratory (II) | Observatory (III) |
| Mask | Theater (I) | Statue (II) |
| Column | Statue (II) | Gardens (III) |
| Moon | Altar (I) | Temple (II) |
| Sun | Temple (II) | Pantheon (III) |
| Drop | Baths (I) | Aqueduct (II) |
| Senate | Rostrum (II) | Senate (III) |
| Amphora | Tavern (I) | Lighthouse (III) |
| Barrel | Brewery (II) | Arena (III) |

### 4.6 Cost evaluation — what the engine must answer

For a given (player, card-or-wonder) the engine must compute the **minimum coin outlay** and whether
it is affordable, considering: fixed production, choice producers, reductions (Masonry/Architecture
and expansion equivalents), per-resource price (base 2 + opponent's brown/grey production, 1$
overrides, expansion modifiers), chains, and coin cost. This is a small optimisation problem
(choice producers × reductions); with ≤ 7 resource symbols per cost it is solvable by exhaustive
search. Expansions add modifiers to every step — see §10.2.

## 5. Setup

### 5.1 Game setup

- **B-SET-1** Conflict pawn on the centre (neutral) space.
- **B-SET-2** 4 military tokens face up on their spaces (§3.9).
- **B-SET-3** Shuffle the 10 progress tokens; 5 random face-up on the board; the other 5 go to the
  box **(their identity is hidden from both players; Great Library draws from the progress tokens in the Box)**.
- **B-SET-4** Each player takes 7$.
- **B-SET-5** Wonder selection (§5.3).
- **B-SET-6** Age decks: remove 3 random cards from each of Age I, II, III to the Box, unseen
  (expansions draw from them by type and Age). Draw 3 random Guilds into the Age III
  deck, unseen; the other 4 Guilds go to the box.

### 5.2 Structure layouts

Each Age lays out 20 cards in rows. Rows are listed top → bottom; the **bottom row is accessible
first**. A card is covered by the (up to) two cards in the row below that overlap it horizontally.
`U` = face up, `D` = face down.

| Age | Rows (top → bottom) |
|---|---|
| I (pyramid) | 2U · 3D · 4U · 5D · 6U |
| II (inverted pyramid) | 6U · 5D · 4U · 3D · 2U |
| III ("vase") | 2U · 3D · 4U · 2D · 4U · 3D · 2U |

Age III row 4 has a gap in the middle: its two cards (x = 1 and 5) are each covered by a pair of
row-5 cards ({0,2} and {4,6}) and each cover a pair of row-3 cards ({0,2} and {4,6}).

Horizontal positions in half-card units (`x`), centre-aligned, derived from pp. 6–7 and 20:

| Age | Row cards → `x` positions |
|---|---|
| I | 2: {4,6} · 3: {3,5,7} · 4: {2,4,6,8} · 5: {1,3,5,7,9} · 6: {0,2,4,6,8,10} |
| II | 6: {0,2,4,6,8,10} · 5: {1,3,5,7,9} · 4: {2,4,6,8} · 3: {3,5,7} · 2: {4,6} |
| III | 2: {2,4} · 3: {1,3,5} · 4: {0,2,4,6} · 2: {1,5} · 4: {0,2,4,6} · 3: {1,3,5} · 2: {2,4} |

> **Model recommendation:** store each slot as `(row, x)` where `x` is in half-card units and a slot
> *covers* the slots of the row above whose `x` differs by exactly 1. Accessibility and "reveal when
> uncovered" both derive from that one relation, and the expansion layouts (Agora: 25/25/23 cards)
> become pure data.

- **B-STR-1** Lay out the shuffled Age deck according to the layout; face-down cards are unknown to
  both players.
- **B-STR-2** At the end of the turn in which a face-down card becomes accessible, it is turned face
  up (before any "play again" turn begins).

### 5.3 Wonder selection

- **B-WND-1** Choose a first player at random (seeded). This player starts the draft and is the
  **deciding player** for Age I (B-AGE-0).
- **B-WND-2** Shuffle the 12 Wonders. Reveal 4. First player takes 1, second player takes 2, first
  player takes the last.
- **B-WND-3** Reveal 4 more. This time the **second** player takes 1, the first takes 2, the second
  takes the last.
- **B-WND-4** The remaining 4 Wonders go to the box (unseen).
- **B-WND-5** Each player ends with 4 Wonders. Wonders are public information.
- **B-WND-6** Fixed first-game assignment (optional variant): P1 Pyramids, Great Lighthouse, Temple
  of Artemis, Statue of Zeus; P2 Circus Maximus, Piraeus, Appian Way, Colossus.

## 6. Turn

- **B-TURN-1** The starting player of each Age is chosen per B-AGE-0. Players alternate.
- **B-TURN-2** On your turn you **must** take one accessible card from the structure and do exactly
  one of:
  1. **Construct the Building** (B-ACT-1)
  2. **Discard it for coins** (B-ACT-2)
  3. **Construct a Wonder** with it (B-ACT-3)
- **B-TURN-3** End of turn: reveal any face-down card that became accessible (B-STR-2).
- **B-TURN-4** "Play again": the same player takes a new turn after the reveal step. It is a full,
  new turn in every respect ("next turn" effects, once-per-turn limits, start-of-turn steps).
- **B-TURN-5** A "play again" earned on the turn that empties the structure is **lost**.
- **B-TURN-6** "Play again" is a **flag per player**, not a counter: several sources give one extra
  turn. The flag can be set on either player (Shwedagon Pagoda gives it to the opponent) and is
  consumed at the end of the **flag holder's** next turn; the owner's flag is therefore played first.
  One earned anywhere in the turn — including the start-of-turn step (Agora Conspiracy) — is taken
  at the end of that turn, after the reveal.

### 6.1 Actions

- **B-ACT-1 Construct the Building** — pay its cost (§4) and put it in your city. Immediate effects
  resolve now (coins, shields, science pair check).
- **B-ACT-2 Discard** — the card goes to the discard pile; gain **2$ + 1$ per yellow card** in your
  city. Always legal.
- **B-ACT-3 Construct a Wonder** — choose one of your unbuilt Wonders, pay **the Wonder's** cost
  (the Age card's cost is ignored), tuck the Age card face down under the Wonder (it has no other
  effect), resolve the Wonder's effects.
- **B-ACT-4 Seven Wonders limit** — only 7 Wonders may be constructed in total. The moment the 7th
  is constructed, the one remaining unbuilt Wonder is Destroyed. Hence a player cannot construct
  a Wonder if 7 already exist.
- **B-ACT-5 Limit as a count** (needed once expansions can un-build or remove Wonders). Let
  `built` = Wonders constructed right now, both cities. A player may construct a Wonder iff
  `built < 7`, or they own a constructed *Easter Island*, or the Wonder *is* Easter Island
  (`extra-wonders.md`). Whenever a construction brings `built` to ≥ 7, every unbuilt Wonder held by a
  player without a constructed Easter Island is Destroyed. A Destroyed Wonder never returns. So
  un-building (Anubis, Easter Island) or removing (Sabotage, Atlantis) a Wonder re-opens the limit
  for the Wonders still unbuilt — in practice only the un-built one, since the rest were Destroyed.

## 7. Ages

- **B-AGE-0** At the start of **every** Age, a **deciding player** chooses who plays first in that
  Age: themselves or the opponent. This is a decision, not a turn.
  - Age I: the deciding player is the first player of the draft (B-WND-1).
  - Age II and III: see B-AGE-3.
- **B-AGE-1** An Age ends when all cards of its structure have been taken.
- **B-AGE-2** Set up the next structure (B-STR-1).
- **B-AGE-3** For Age II and III, the deciding player is the one with the **weaker military** (the
  pawn is on their side). If the pawn is on the centre space, it is the last active player (the one
  who took the last card of the previous Age).

## 8. Military

- **B-MIL-1** Track: 19 spaces = centre (0), spaces 1–8 toward each side, capital at 9. Zones: centre
  {0}; {1,2}; {3,4,5}; {6,7,8}; capital {9} on each side → 9 zones.
- **B-MIL-2** Each shield gained moves the pawn **one space at a time** toward the opponent's capital.
  The pawn can go back and forth: moving toward your capital first undoes your opponent's advantage.
- **B-MIL-3** When the pawn **enters** a zone that holds a military token, apply the token
  (§3.9) to the player on whose side it is, then remove it.
- **B-MIL-4** Pawn entering a capital → military supremacy (immediate win).
- **B-MIL-5** End-of-game military VP for the player the pawn is advanced toward the opponent:
  zone {1,2} → 2 VP, {3,4,5} → 5 VP, {6,7,8} → 10 VP; centre → 0 for both.

## 9. Science and progress

- **B-SCI-1** The first time you hold a **pair of identical** scientific symbols, you immediately
  choose one progress token **from the board**. If none remain, nothing happens. A third or fourth
  identical symbol grants nothing; only the first pair of each symbol counts.
- **B-SCI-2** Symbols come from green cards and the *Law* token (and expansion sources).
- **B-SCI-3** Holding **6 different** symbols → scientific supremacy (immediate win).

## 10. Mechanics model (architectural grouping)

This section groups the rules above — and the extension points the three expansions need — into
mechanics that should map to separate, testable parts of the engine. It is not a design, but every
rule in the four documents should land in exactly one of these groups.

### 10.1 Game flow (state machine)

Phases: `SETUP → WONDER_DRAFT → AGE(I) → AGE(II) → AGE(III) → SCORING → OVER`, with `OVER`
reachable from anywhere via supremacy. Inside an Age, a turn is a sequence:

```
[turn start hooks]       Agora: optionally trigger 1 prepared Conspiracy
main action              take a structure card → build | discard | wonder  (+ Agora: recruit, prepare conspiracy)
                         | Pantheon (Age II–III): activate a Pantheon card (no structure card)
[resolution]             effects resolve; may open nested decisions (see 10.3)
[end-of-turn]            reveal newly-accessible cards; Pantheon: collect Mythology/Offering tokens on them
[next]                   play again? → same player · structure empty? → age end · else → opponent
age end                  weaker-military player chooses next starting player
```

**Design point:** decisions the rules give to a player mid-effect (choose a progress token, choose
an opponent card to discard, choose a guild city, choose a Divinity, place influence…) are **not**
separate turns. Model them as a **stack of pending decisions** owned by a specific player — which
may be the *opponent* (Extra Wonders: Ishtar Gate, Shwedagon Pagoda; Agora/Pantheon do not
need this but base does not exclude it). Every client action either answers the top decision or is
the main action of a turn.

### 10.2 Economy & cost solver

One pure function: `cost(player, target, state) → {payable, minCoins, paymentBreakdown}`.
Inputs it must honour (base + expansions):

| Step | Base | Expansions add |
|---|---|---|
| Shield cost | — | Extra Wonders: some Wonders cost shields — paid by moving the pawn toward your **own** capital; illegal if it would give the opponent military supremacy (`extra-wonders.md` §1.1) |
| Chain → free | own chain symbols | Agora decree: opponent's chains count; Pantheon *Engineering*: any chained card for 1$; Extra Wonders: Petra/Tikal/Tower of Babel/Aqueduct of Segovia/St Peter's grant symbols |
| Coin cost | printed | Agora: Senator cost = number of Senators owned; *Corruption* → 0; decree "ignore 1 cost symbol (coin or resource)" per color |
| Resource reduction | Masonry (blue −2), Architecture (wonder −2) | Agora decree (wonder −1); Extra: Ziggurat of Ur (all −1), Pyramid of Djoser (wonders −1) |
| Production | fixed + choice producers | stolen cards; Pantheon Baal; Extra: card swaps |
| Unit price | 2 + opp. brown/grey; 1$ overrides | Agora decree / Tower Bridge: −1$ per unit, min 1$; Christ the Redeemer: per-turn choice of brown-or-grey at 1$ |
| Payee | bank / opponent (Economy) | — |
| Post-payment hooks | Urbanism +4$ on chain | Agora decrees pay coins on construction |

### 10.3 Effects

Every card, wonder, token, divinity, decree, conspiracy, senator is a bundle of effects from a small
taxonomy. Classify each effect by **when** it acts:

| Kind | When | Examples |
|---|---|---|
| **Instant** | once, on construction/acquisition | coins, shields, discard opponent card, take progress token, play again |
| **Static / modifier** | queried while owned | production, trade price, cost reduction, Strategy, Theology, chain sources |
| **Triggered** | on an event, while owned | Urbanism (on chain build), Economy (on opponent trade), decrees (on any construction), Poliorcetics (on pawn advance), Borobudur |
| **End-game scoring** | at final scoring | VP, per-color VP, guilds, Mathematics |
| **Supremacy contributor** | continuously checked | shields, science symbols, chamber control |

- **B-EFF-1** Instant effects are **permanent**: once applied, they are never undone, even if their
  source later leaves play (a card discarded, stolen or swapped; a Wonder Destroyed or
  un-built). Only static, triggered and scoring effects follow the source. The single exception is
  Agora's **Shield decree**, whose shield is tied to chamber control (`agora.md` §6.1).

- **B-EFF-2 Coin transfers.** "Lose *n*" takes `min(n, treasury)` to the bank; "take *n* from the
  opponent" moves `min(n, treasury)` — you gain exactly what they lost. Only the **treasury** is ever
  counted, lost or taken; coins held on a card (Astarte, Spring Temple Buddha) are not.
- **B-EFF-3 Infeasible choices.** An effect offering a choice of options ("choose: X or Y") offers
  only the feasible ones; if none is feasible the effect does nothing. An effect that takes from a
  pool or a target set (setup-removed cards, progress tokens, opponent cards of a color) uses what is
  there: fewer items than asked → all of them; none → nothing. A mandatory effect with a target is
  applied whenever a target exists.
- **B-EFF-4 Resolution order.** A source's own effects resolve in **printed order**. Triggered
  effects it causes then fire — the active player's first, then the opponent's; payment-time hooks
  (Economy) fire during the payment, before either. Pending decisions created by one effect for both
  players are answered active player first.

Instant effects that need a player choice push a pending decision (10.1). Static and triggered
effects should be **derived from current ownership** rather than stored as flags, so that effects
that remove/steal/swap cards or Wonders (Circus Maximus, Baal, Ra, Anubis, Agora conspiracies, Extra
Wonders) automatically update production, chains, and scoring.

### 10.4 Card zones & movement

Zones a card/component can be in: `deck(age)`, `structure(slot, faceUp)`,
`city(player)`, `underWonder(wonder)`, `discard`, `box`, `destroyed`. `destroyed` is terminal (nothing leaves it) and never a draw source; there is **one** `box`: effects that "draw from the cards/tokens/Wonders removed at setup" are queries over it (type, and Age for cards). Movements: take from structure, build,
discard, tuck under Wonder; and (expansions) steal, swap, discard-from-city, destroy-Wonder,
un-build a Wonder. The same applies to tokens (progress: `board / player / box / destroyed / locked`) and Wonders (`draft / player.unbuilt / player.built / box / destroyed`).

### 10.5 Military track

Pawn position ∈ [−9, +9]. A movement of *n* shields is resolved as a **path**: compute the
landing space (clamped at the capital; cut short by Pantheon's Minerva), apply the military tokens
of every zone entered along the path, and apply per-space hooks (Pantheon *Poliorcetics*). Military
supremacy is decided **once, on the landing space** (B-MIL-4) — no per-step check. Movement can be
toward either capital: Agora's Shield decree and Extra Wonders' shield costs move the pawn toward
the mover's own side.

### 10.6 Science

Symbol multiset per player; pair-detection per symbol (once); count of distinct symbols for
supremacy. Expansions add symbol sources (Ishtar, Nisaba copy, US Capitol) and a VP counter
(Hermitage Museum).

### 10.7 Scoring

Score-pad categories, kept separate for tie-breaks and UI: blue, green, yellow, purple, Wonders,
progress, coins (⌊coins / 3⌋), military — plus expansion lines (Agora: Senate chambers; Pantheon:
Divinities, Grand Temples). **Tie-break**: most blue-card VP; still tied → shared victory (draw).

### 10.8 Hidden information & randomness

| Hidden | From whom | Revealed when |
|---|---|---|
| Face-down structure cards (identity only; the back group is public, §3.10) | both | become accessible (end of turn) |
| Cards & progress tokens removed at setup | both | only via effects (Great Library etc.) |
| Undrafted Wonders | both | never (Extra Wonders: Stonehenge/Karnak draw from them) |
| Deck order | both | — |

All randomness (shuffles, random draws from setup pools) must come from the seeded RNG
(`InitContext.seed()`); random draws **during** play consume the same RNG stream, so the state must
carry the RNG position. The expansions add **asymmetric** knowledge (one player knows something the
other does not) — see `agora.md` and `pantheon.md` §Hidden information; build per-viewer views from a
per-player knowledge model, not from a single public/private flag.

### 10.9 Victory checks

Each supremacy is checked at a single point:

| Supremacy | Checked | Condition |
|---|---|---|
| Military | after a pawn movement resolves (§10.5) | landing space is the opponent's capital |
| Science | after a scientific symbol is acquired | distinct symbols owned ≥ 6 |
| Political (Agora) | after a chamber control change | the player controls all 6 chambers |

The first condition met ends the game. If one atomic effect gives **both** players a supremacy at
once (e.g. a green-card swap), the **active player** wins.

## 11. End of game

- **B-END-1** The game ends immediately on supremacy, otherwise after Age III.
- **B-END-2** Civilian score = military VP (§8) + Building VP (blue, green, yellow, purple) + Wonder
  VP + progress VP + ⌊treasury / 3⌋.
- **B-END-3** Tie → most blue-card VP wins; still tied → shared victory.

## 12. Open questions (base)

None remain.

Resolved by the user (2026-10-24): **B2** guild ties (§3.6); **B3** chains are mandatory (B-COST-5);
**B4** trade cards and play again (B-TRADE-3).

Resolved by the user (2026-10-24): **B1** — supremacy check points (§10.9); **B5** — first player and
the deciding player (B-WND-1, B-AGE-0); **B6** — only the first pair grants a token (B-SCI-1); base
Wonder costs confirmed (§3.7).

## 13. Reference implementation discrepancies

Card data in `grmp/…/deck/DeckFactory.java` was checked against rulebook pp. 18–19:

| Issue | Reference | Rulebook |
|---|---|---|
| Duplicate name | Age III has two "Fortifications"; the second (C C S S, ◂Helmet) | is **Circus** |
| Wrong chain | Arena ◂Vase (Amphora) | Arena ◂**Barrel**; Lighthouse ◂Amphora |
| Naming | Age II "Courthouse" (W W G, 5 VP); Age III "Pretorium" (8$, 3 shields) | Age II **Tribunal**; Age III **Courthouse** (= Pretorium) |
| Typo | "Cirrus Maximus" | Circus Maximus |
| Scoring | `Map.of(6,10,3,5,1,2)` iterated for military VP — `Map.of` has no iteration order | ordered thresholds |

All other costs, effects and chains match.
