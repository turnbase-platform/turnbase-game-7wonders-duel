# 7 Wonders Duel — Agora Expansion (consolidated)

Source: `docs/rules/7-wonders-duel-agora.pdf` (Repos Production, 2020, 20 pp.). Written as a **delta against
[`base.md`](base.md)**: everything not stated here follows the base rules. Rule IDs are `A-…`; gaps
are **[OPEN-An]**, listed in §11. Values that are icon-only in the rulebook have been confirmed by
the user.

## 1. What Agora changes — summary

| Mechanics group (`base.md` §10) | Change |
|---|---|
| 10.1 Game flow | New optional **pre-action step** (trigger 1 Conspiracy); two new main actions (**recruit a Senator**, **prepare a Conspiracy**) |
| 10.2 Cost solver | New card type with a **dynamic coin cost** (Senators); decree-based modifiers (ignore a symbol, −1 per wonder resource, −1$ trade, opponent's chains) |
| 10.3 Effects | New effect families: Senate actions (place / move / remove cube), conspiracy effects, decrees (static + "either player builds" triggers) |
| 10.4 Zones | New zones: Senate chambers (cubes, decrees), Conspiracy deck, player's Conspiracies (unprepared / prepared / triggered), card under a Conspiracy |
| 10.5 Military | 4 new military tokens with Senate effects; a decree that moves the pawn on **control change** |
| 10.8 Hidden info | Conspiracies in hand (owner-only), face-down decrees, conspiracy deck top known to one player, removed setup cards seen by one player; Senators have their own **grey card back**, so a face-down Senator is publicly known to be a Senator (`base.md` §3.10) |
| 10.9 Victory | **Political supremacy** — control all 6 chambers |
| 10.7 Scoring | New score line: Senate (chamber VP) |

## 2. Components

| Component | Count | Notes |
|---|---|---|
| Senate board | 1 | 6 Chambers in 3 sections (left, centre, right; 2 chambers each) |
| Influence cubes | 24 | 12 per player — **a hard limit** (A-SEN-6) |
| Senator cards | 13 | 7 Politicians (white), 6 Conspirators (black); grey back; shuffled into the Age decks |
| Conspiracy cards | 16 | |
| Decree tokens | 16 | 6 used per game |
| Wonders | 2 | Curia Julia, Knossos |
| Progress tokens | 2 | Corruption, Organized Crime |
| Military tokens | 4 | **replace** the base ones (A-MIL) |

### 2.1 Senate board

- **A-BRD-1** Chambers are numbered 1–6 left to right. Sections: Left = {1,2}, Centre = {3,4},
  Right = {5,6}.
- **A-BRD-2** Each chamber has an **Influence zone** (cubes of both players), a **Decree zone** and a
  **Control zone** (one cube of the controlling player, if any).
- **A-BRD-3** Adjacency: chamber *n* is adjacent to *n−1* and *n+1* (linear, no wrap).
- **A-BRD-4** End-game VP per controlled chamber, chambers 1→6: **1, 2, 3, 3, 2, 1**.
- **A-BRD-5** Decree face pattern, chambers 1→6: **Up, Down, Up, Down, Up, Down**.

## 3. Setup (delta)

- **A-SET-1** Senate board under the base board.
- **A-SET-2** Prepare each Age deck as in the base game (remove 3 cards), **then** add random
  Senators: Age I **5**, Age II **5**, Age III **3**; shuffle. All 13 Senators are used every game (5+5+3).
- **A-SET-3** Structures use the Agora layouts (§4) instead of the base ones.
- **A-SET-4** Shuffle the 16 Decrees face down; place 6 random ones, one per chamber, face up or down
  per the board symbol. The other 10 go to the box **unseen**.
- **A-SET-5** Shuffle the Conspiracies into a face-down deck.
- **A-SET-6** Place the Agora military tokens **instead of** the base ones (A-MIL).
- **A-SET-7** Each player takes 12 cubes.
- **A-SET-8** Progress tokens and Wonders: shuffle the expansion's ones in with the base ones and
  follow the base rules (5 progress on the board out of 12; Wonders drafted from 14).

## 4. Structures

Rows top → bottom; bottom row accessible first. `U` up, `D` down.

| Age | Rows | Cards |
|---|---|---|
| I | 3U · 4D · 5U · 6D · 7U | 25 = 20 + 5 Senators |
| II | 7U · 6D · 5U · 4D · 3U | 25 = 20 + 5 Senators |
| III | 2U · 3D · 4U · 5D · 4U · 3D · 2U | 23 = 20 + 3 Senators |

Age III Agora has **no gap** (row 4 is a full row of 5).

Horizontal positions in half-card units (same covering relation as `base.md` §5.2):

| Age | Row cards → `x` positions |
|---|---|
| I | 3: {4,6,8} · 4: {3,5,7,9} · 5: {2,4,6,8,10} · 6: {1,3,5,7,9,11} · 7: {0,2,4,6,8,10,12} |
| II | 7: {0,2,4,6,8,10,12} · 6: {1,3,5,7,9,11} · 5: {2,4,6,8,10} · 4: {3,5,7,9} · 3: {4,6,8} |
| III | 2: {3,5} · 3: {2,4,6} · 4: {1,3,5,7} · 5: {0,2,4,6,8} · 4: {1,3,5,7} · 3: {2,4,6} · 2: {3,5} |

In Age III the outer cards of the 5-row (x = 0, 8) are covered by one card each.

- **A-STR-2** Rows always alternate, starting **face up** with the first row placed (the top row,
  covered by the second). So the top row is always face up.

- **A-STR-1 Combined with Pantheon** — use the Agora layouts above. Pantheon's tokens go on the
  face-down rows that correspond to the base ones (each Agora row is one card wider), in the same
  positions, except that the **last token of each row moves to the last card of that row**:

  | Token | Base row → positions | Agora row → positions |
  |---|---|---|
  | Mythology (Age I) | 3D → 1, 3 | 4D → 1, **4** |
  | Mythology (Age I) | 5D → 1, 3, 5 | 6D → 1, 3, **6** |
  | Offering (Age II) | 5D → 1, 3, 5 | 6D → 1, 3, **6** |

## 5. Senators

- **A-SEN-1** A Senator card in the structure may be used like any Age card: **recruit** it,
  **discard** it for coins, or use it to **construct a Wonder** (or **prepare a Conspiracy**).
- **A-SEN-2** Recruit cost in coins = number of Senators already in your city (first is free).
  *Corruption* → always free. This cost system is unique to Senators; no other cost modifier
  (decrees, reductions, chains) applies to it.
- **A-SEN-3** Recruited Senators stay in your city. They are Building cards with their own colors —
  **white** (Politician) and **black** (Conspirator) — and their own grey back. No base effect counts
  white or black, so in practice they do not feed discard income, guilds, Lighthouse or color decrees.
  "Recruit" is the Senator's form of "construct a Building" and is **the same action** in every
  rule: any effect that lets you construct a card (Mausoleum, Hades, Messe, Treason…) may recruit a
  Senator (full effect, counts for A-SEN-2), and any ban on constructing a Building
  (Mont-Saint-Michel) bans recruiting. *Property Fraud* is the one printed exception.
- **A-SEN-4 Politician** — shows a section (left / centre / right). You get *N* **Senate actions**,
  where *N* depends on your blue-card count (the Senate-actions decree adds 2 to the **count**, not to
  *N*). Each action is **optional** — "you *can* take" (p. 9); a player may take fewer than *N*:

  | Blue cards | 0–1 | 2–3 | 4+ |
  |---|---|---|---|
  | Senate actions | 1 | 2 | 3 |

  Each action is, freely chosen:
  - **Place** — 1 cube from your supply into **either chamber of the shown section**. If the chamber's
    decree is face down, turn it face up.
  - **Move** — 1 of your cubes from any chamber to an **adjacent** chamber. The same cube may move
    several times, including one just placed.
- **A-SEN-5 Conspirator** — exactly one of:
  - **Place** 1 cube in **any** chamber (flip a face-down decree).
  - **Conspire** — draw 2 Conspiracies, keep 1 face down, put the other on the **top or bottom** of
    the deck (your choice). *Organized Crime*: keep both.
- **A-SEN-6** No cubes left in supply → place actions are unavailable (moves still are). Cube count
  per chamber is unlimited.
- **A-SEN-7** Politicians by section: **2 left, 3 centre, 2 right**. Conspirators have no section
  (they place in any chamber).

## 6. Chambers, control, decrees

- **A-CTL-1** You **control** a chamber when you have **strictly more** cubes there than the opponent.
  Tie → nobody controls it, its decree applies to nobody.
- **A-CTL-2** Control is re-evaluated after **every** cube placement, move or removal (control
  changes are events — the Shield decree reacts to them).
- **A-CTL-3** Represent control by moving one of the controller's cubes to the Control zone; it still
  counts for the majority. (UI concern only — the model should compute control from counts.)
- **A-CTL-4** The controller of a chamber benefits from **all** decrees in it (after the "move
  decree" conspiracy a chamber may hold 2, another 0).
- **A-CTL-5** A face-down decree is turned face up when its chamber receives its **first cube**,
  whether placed (from any source: Senator, military token, Knossos, conspiracy) or moved in.

### 6.1 Decrees (16)

"Either player" decrees pay **the controller**, whoever builds.

| # | Decree | Kind | Cost-solver / trigger hook |
|---|---|---|---|
| 1–3 | Ignore 1 cost symbol (a resource **or** the coin cost) when constructing a **yellow** / **red** / **green** card | static | cost reduction per color |
| 4 | Pay 1 resource less (your choice) when constructing **Wonders** | static | wonder reduction |
| 5–8 | Each time **either** player constructs a **blue** / **green** / **yellow** / **red** card, take coins = current Age (1/2/3) — every construction, free or indirect included; never a card moving between cities | trigger | on-build (any player) |
| 9 | Each time **either** player constructs a **Wonder**, take coins = current Age | trigger | on-wonder-built |
| 10–11 | Pay 1$ less per **brown** / **grey** resource bought from the bank (min 1$) (two decrees, one per type: 16 − 14 others = 2) | static | unit-price modifier |
| 12 | +2 to your blue-card count when computing Senate actions | static | Senate-action count |
| 13 | **Shield**: gaining control = +1 shield, pawn 1 toward opponent; losing it = −1 shield, pawn 1 back toward you; opponent **stealing** it = pawn 2 toward you | trigger | on-control-change |
| 14 | +2$ extra when discarding a card for coins | static | discard income |
| 15 | When constructing a Building, use chains from the **opponent's** cards as your own | static | chain sources |
| 16 | Recruiting a **Conspirator** → play again | trigger | on-recruit |

Notes:
- **A-CTL-6** The Shield decree benefits **whoever controls the chamber it currently sits in**. Any
  change of that beneficiary — by cubes, or by *Insider Influence* moving the decree between chambers
  — fires the same moves: the old beneficiary loses the shield (pawn 1 toward them), the new one
  gains it (pawn 1 toward their opponent).
- The Shield decree is the **only instant effect that can be undone** (`base.md` B-EFF-1): its
  shield exists only while you control the chamber. Losing control = pawn 1 back toward you (as if
  the shield were lost); the opponent then gaining control = 1 more toward you (they gain it).
- The Shield decree is the only effect in all four documents that can **move the pawn backwards
  toward the moving player's own side by losing something**; military tokens are only applied on
  *entering* a zone (base B-MIL-3), so a backward move followed by a forward move can re-enter a zone
  whose token is already gone — no effect. Its pawn moves are ordinary
  shield movements (`base.md` §10.5): military tokens, supremacy, Poliorcetics and Minerva all apply.

## 7. Conspiracies

- **A-CON-1 States** of a Conspiracy owned by a player: **unprepared** (face down, in front of the
  owner) → **prepared** (an Age card tucked under it) → **triggered** (face up; the tucked card stays
  under it; done).
- **A-CON-2 Prepare** — a main action: take any accessible structure card, tuck it face down under
  one of your unprepared Conspiracies. Costs nothing. The card has no other effect. Ends the turn
  (play-again rules unaffected).
- **A-CON-3 Trigger** — at the **start of your turn**, before any other decision, optionally reveal
  **one** prepared Conspiracy and apply its effect; then take your normal turn. At most **one trigger
  per turn**. Right after the trigger, run the reveal step (B-STR-2, Pantheon token pickup): every
  accessible card is face up whenever a main action is chosen. If the trigger empties the
  structure, the Age ends at once and the player takes no main action (they are the last active
  player). A play-again turn is a turn, so it has its own trigger step.
- **A-CON-4** A player may hold any number of Conspiracies (unprepared and prepared).
- **A-CON-5** Conspiracy identities are hidden from the opponent until triggered. The opponent knows
  how many each player holds and which are prepared.
- **A-CON-6** Curia Julia triggers an **unprepared** Conspiracy (no tucked card).

### 7.1 Conspiracies (16)

Names and texts from the cards (supplied by the user). Effects resolve in the printed order. "+ move"
= **you may move 1 of your cubes to an adjacent chamber** (optional), which ends cards 1–8.

| # | Conspiracy | Effects | Notes for the engine |
|---|---|---|---|
| 1 | Political Maneuver | place 1 cube in any chamber; remove 1 opponent cube from the Senate; + move | removed cube returns to its owner's supply |
| 2 | Insider Influence | move 1 Decree under another chamber's Decree; + move | one chamber ends with none, one with two (A-CTL-4); either decree may be face down — one that lands in a chamber holding ≥1 cube is flipped (A-CTL-5); the Shield decree's beneficiary may change (A-CTL-6); the decree-less chamber still scores and counts for supremacy |
| 3 | Expropriation | discard 1 opponent **blue** card; + move | |
| 4 | Swindle | discard 1 opponent **yellow** card; + move | static effects of the card (trade price, production) end |
| 5 | Blackmail | take half the opponent's coins, rounded up; + move | |
| 6 | Turn of Events | discard 1 accessible structure card, may repeat once; + move | newly uncovered cards are revealed at end of turn; a token on a discarded card goes to **you** (A-TOK-1) |
| 7 | Extortion | take 1 unconstructed Wonder from the opponent; + move | Wonder count per player is not capped at 4 |
| 8 | Coercion | swap 1 blue or green card with the opponent, same color both ways; + move | needs a card of that color on **both** sides ("in exchange, give them 1 of your cards of the same color", p. 19); science effects per A-SCI-1 |
| 9 | Embezzlement | gain 1$ per own cube in the Senate; opponent loses 1$ per **their** cube | |
| 10 | Espionage | secretly look at all progress tokens removed at setup, choose 1 to play | the rest go back; the knowledge stays with the player |
| 11 | Obscurantism | **lock** 1 progress token — from the board, the opponent, or out of the game — face down on this card; nobody can use it; from "out of the game" the chooser looks at the whole setup-removed pool, and the locked token's identity stays hidden from the opponent | taking it from the opponent removes its static and scoring effects; instant effects (coins) stay |
| 12 | Property Fraud | construct for free 1 Building from **the end of the structure** (top row), accessible or not; not a Senator | the top row is always face up (A-STR-2) |
| 13 | Treason | secretly look at the cards removed at setup (Age I: 3; Age II: 6 from I+II; Age III: 9 from I–III), choose 1 and **play** it for free | "play" = treat it as a card taken from the structure: construct/recruit (free), discard for coins, build a Wonder with it (the Wonder costs its **normal** price — only the card is free), or prepare a Conspiracy; only the player sees the chosen card unless it becomes public (built, discarded) |
| 14 | Foreclosure | take 1 brown or grey card from the opponent into your city | production and trade prices change for both players |
| 15 | Sabotage | return 1 opponent **constructed Wonder** to the box; its effects are lost | instant effects already applied stay (`base.md` B-EFF-1); its ongoing effects and VP are lost; the tucked card goes to the **discard pile**; the 7-Wonder limit **re-opens** (one more Wonder may be built; a Wonder already boxed by the limit stays in the box) |
| 16 | Coup | gain 2 shields: the pawn moves 2 spaces toward the opponent's capital | an ordinary shield gain (`base.md` §10.5) |

- **A-TOK-1** Pantheon combination: when any effect removes a structure card that carries a Pantheon
  token, the token goes to **the player who caused the removal**.
- **A-SCI-1** Science after a card moves between cities (Coercion, steals, swaps): a **new** pair
  grants a progress token as usual; **breaking** a pair takes nothing back (tokens are not tied to
  pairs once taken); scientific supremacy is checked.

## 8. Military tokens (Agora)

- **A-MIL-1** The 4 Agora military tokens **replace** the base ones, on the same zones. Small tokens:
  **place 1 cube**; large tokens: **remove 1 opponent cube** and/or **move 1 own cube** (confirmed by the
  user, 2026-10-24).
- **A-MIL-2** They have no coin-loss effect.
- **A-MIL-3** As in the base game, the token benefits the player advancing the pawn, applies once,
  and is removed.

## 9. Wonders and progress tokens

| Wonder | Cost | On choosing (draft) | On construction | VP |
|---|---|---|---|---|
| Curia Julia | W C S P G | draw 2 Conspiracies, keep 1, other to top/bottom | trigger an **unprepared** Conspiracy you own (optional); +6$; play again | 0 |
| Knossos | W C S G G | place 1 cube in any chamber | place 1 cube in any chamber; move 1 cube to adjacent (optional) | 3 |

- **A-WND-1** "On choosing" effects are a **new trigger: Wonder first obtained** — once per game per
  Wonder: in the draft, or later when the Wonder first enters a player's hand (Stonehenge). Stealing
  it (Ra, Taj Mahal, Extortion) or Karnak's copy never re-triggers it. Draft-time ones resolve in the
  draft phase, before Age I. Knossos's placement at draft time flips a face-down decree (A-CTL-5) and can
  establish control before the game starts.

| Token | Effect |
|---|---|
| Corruption | All Senators cost 0$ for you |
| Organized Crime | When you Conspire, keep both drawn cards |

## 10. Turn, victory, scoring (delta)

- **A-TURN-1** Turn order: (1) optionally trigger 1 prepared Conspiracy; (2) mandatory: take a
  structure card and **construct/recruit**, **discard**, **construct a Wonder**, or **prepare a
  Conspiracy**.
- **A-TURN-2** With Pantheon: (2) may instead be **activate a Pantheon card** (the Conspiracy step
  still comes first).
- **A-VIC-1** **Political supremacy** — controlling all 6 chambers at any moment → immediate win.
  Checked after every control change (A-CTL-2).
- **A-SCO-1** End-game: each controlled chamber scores its printed VP (A-BRD-4). Tie-break unchanged
  (blue VP, then shared).

## 11. Open questions (Agora)

None remain.

Resolved by the user (2026-10-24): **A20** Sabotage keeps instant effects (§7.1); **A4** row faces (A-STR-2); **A9** shield decree (§6.1); **A11**
Conspiracy list (§7.1); **A12** tokens on removed cards (A-TOK-1); **A15** Treason (§7.1); **A16**
science after swaps (A-SCI-1); **A19** moot — the top row is always face up; **A2** chamber VP (A-BRD-4); **A1** adjacency (A-BRD-3); **A3** decree pattern (A-BRD-5);
**A5** combined layouts (A-STR-1); **A6** Senator colors (A-SEN-2, A-SEN-3); **A7** sections
(A-SEN-7); **A8** decree reveal (A-CTL-5); **A10** trigger timing (A-CON-3); **A13** returned Wonder
(§7.1); **A14** end of the structure (§7.1); **A17** military tokens (A-MIL); **A18** Wonder costs
(§9).
