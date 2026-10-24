# 7 Wonders Duel — Extra Wonders (community expansion, consolidated)

Source: `docs/rules/7-wonders-duel-extra-wonders.pdf` ("77 Wonders", community-made, 8 pp.). The expansion
adds no new components and no new rules outside the Wonder cards: **every new mechanic lives on a
Wonder**. Written as a delta against [`base.md`](base.md); some Wonders reference
[`agora.md`](agora.md) / [`pantheon.md`](pantheon.md) components. Rule IDs are `X-…`; gaps are
**[OPEN-Xn]**, listed in §6.

> The PDF shows **effect text only — no costs**. Costs are entered by hand from the physical cards
> in §4 (TODO).

## 1. Wonder pool

- **X-POOL-1** 77 Wonders = 12 base + 2 Agora + 2 Pantheon + **61 new**.
- **X-POOL-2** The draft (base B-WND) is unchanged: 8 random Wonders from the enabled pool.
- **X-POOL-3** A Wonder whose effect depends on an expansion that is not enabled is excluded from the
  pool (Curia Julia, Knossos → Agora; Sanctuary, Divine Theater → Pantheon). **Abu Simbel and
  Colosseum** need *Leaders*, which we do not support: **always excluded**, leaving 59 new Wonders.
- **X-POOL-4** Wonders referring to "purple cards (Guilds or Grand Temples)" work with either.
- **X-POOL-5** Several Wonders draw from the **undrafted Wonders** (the 4+ sent to the box at the
  draft — "returned to the box during setup"): Karnak, Stonehenge. With 77 Wonders that pool is
  69 cards; only Wonders of the expansions enabled in this game count.
- **X-POOL-6** All enabled Wonders form the pool. A finer per-Wonder selection is a possible later
  configuration option, not part of the rules.

### 1.1 Shield costs

- **X-SHC-1** Some new Wonders have **shields in their cost** — no other buildable card does.
- **X-SHC-2** Paying a shield cost of *n* moves the Conflict pawn *n* spaces **toward your own
  capital**, whatever its current position.
- **X-SHC-3** Shields cannot be bought, reduced (Architecture, Ziggurat of Ur, decrees) or replaced
  by coins. A Wonder built **for free** (Isis) waives its shield cost too.
- **X-SHC-4** Constructing such a Wonder is **illegal** if the move would put the pawn on your own
  capital (it would give the opponent military supremacy). The legality check belongs to the cost
  solver: `payable` is false.
- **X-SHC-5** The move is resolved **exactly as if the opponent had gained *n* shields**: military
  tokens in the zones it enters are applied against you (to the opponent's benefit), and Pantheon's
  Minerva pawn stops it, and the opponent's *Poliorcetics* makes you lose 1$ per space.

## 2. New Wonders (61)

Legend — **$**: take from bank; **steal n$**: take from the opponent (you gain what they actually
lose, `base.md` B-EFF-2); **opp −n$**: opponent loses to
the bank; **⚔n**: shields; **↻**: play again; **VP**: printed VP. Tags refer to §3.

- **X-ORD-1** A Wonder's effects resolve in the PDF's printed order (`base.md` B-EFF-4). The ⚔
  column resolves **before** the Effects column, except *Potala Palace*, whose shield is printed
  last (after opp −3$) and *Borobudur*, whose own construction triggers its effect after the shield.
  ↻ is always last.

| Wonder | Effects | ⚔ | ↻ | VP | Tags |
|---|---|---|---|---|---|
| Abu Simbel | Bury a recruited Leader under it: no ability, worth 2× its cost in VP | | ↻ | | LEADERS |
| Acropolis of Athens | Discard 1 opponent **purple or yellow** card | | | 4 | DESTROY |
| Angkor Wat | steal 4$ | 1 | | 3 | COIN |
| Aqueduct of Segovia | Chains for Lighthouse, Arena (Age III); +9$ | | ↻ | | CHAIN |
| Arc de Triomphe | End: 1 VP per red card in your city; opp −6$ | | ↻ | | SCORE |
| Athena Pronaia Sanctuary | Look secretly at 1 face-down structure card, may repeat once; +6$ | | ↻ | | PEEK |
| Atlantis | Destroy 1 opponent **constructed** Wonder (instant effects kept; its tucked card → discard pile; the limit re-opens per `base.md` B-ACT-5); +3$ | | | 3 | UNBUILD |
| Borobudur | Each new Building or Wonder of yours worth ≥1 shield (this one included) → steal 1$ | 1 | | 3 | TRIGGER |
| Çamlıca Mosque | End: 6 VP per complete set of 6 colors (red, yellow, brown, grey, blue, green); each card in one set; +6$ | | | | SCORE |
| Chichen Itza | Choose: take 1 opponent blue card, **or** steal 5$ | | | 2 | CHOICE, STEAL |
| Christ the Redeemer | Each of your following turns, choose: all brown **or** all grey resources cost 1$ from the bank; +3$ | | | | TRADE, PER-TURN |
| Colosseum | Draw 2 unused Leaders, may recruit 1 for free | | | 2 | LEADERS |
| Cothon of Carthage | End: 1 VP per brown and grey card in your city; +3$ | | ↻ | | SCORE |
| Derawar Fort | opp −5$; +8$ | 1 | | | COIN |
| Easter Island | Ignores the 7-Wonder limit for itself and cancels it for you afterwards — you may build any number of Wonders; discard a card tucked under any Wonder — mandatory if any exists, never Easter Island's own (un-builds it, instants kept, may be rebuilt) | | | 5 | LIMIT, UNBUILD |
| Eiffel Tower | Pick 3 progress tokens from the board; opponent shuffles them; take 1 at random (public); the other 2 go back to the board. End: 3 VP per progress token you own; +3$ | | | | PROGRESS, SCORE |
| Forbidden City | **Lock** 1 progress token (board / opponent / out of game) face down on it — as Agora's *Obscurantism* (chooser sees the pool; identity hidden); +5$ | | | 3 | PROGRESS |
| Grand Bazaar | End: 1 VP per yellow card in your city; +6$ | | ↻ | | SCORE |
| Great Mosque of Djenné | Counts as a card of **any color except purple** whenever **its owner** gains coins or VP from a color count (PDF: "each time you gain…") — own Guilds, Çamlıca, discard income; counted once in a multi-color sum (Shipowners). Not for the opponent's effects, not for Senate actions; never a target of color-based steals/discards | | ↻ | 2 | COLOR |
| Great Wall of China | Discard all military tokens on **your** side unapplied; +7$ | | | 4 | MILITARY |
| Great Ziggurat of Ur | Every Building and Wonder you construct costs 1 resource less (your choice); +3$ | | | 3 | COST |
| Gyeongbokgung Palace | opp −7$ | 1 | | 4 | COIN |
| Hagia Sophia | Re-apply the instant effects of up to 3 Buildings in your city, each a different color among red, purple, yellow (values recomputed now; red shields move the pawn and fire Borobudur; Strategy / Trojan Horse add nothing — no card is constructed) | | | 2 | REPLAY-EFFECT |
| Hermitage Museum | End: 2 VP per scientific symbol in your city (duplicates count); +9$ | | | | SCORE |
| Himeji Castle | Choose: take 1 opponent green card, **or** opp −10$ | | | 2 | CHOICE, STEAL |
| Ishtar Gate | ⚔2, **the opponent may** buy off shields at 4$ each (paid to you): 0/4/8$; +8$ | 2 | | | OPP-CHOICE |
| Karnak Temple | Choose: apply the instant effects of an unconstructed Wonder (either player's), **or** draw a random undrafted Wonder (shown to both), apply its instant effects, return it. Copied shields move the pawn; "on choosing" effects are not copied; card-attached coins (Spring Temple Buddha) go on Karnak | | | 3 | REPLAY-EFFECT |
| Leaning Tower of Pisa | Take 1 progress token from the board; give the opponent 1 random setup-removed progress token | | | 6 | PROGRESS |
| Louvre Museum | Your discards give +2$ more; +3$ | | | 3 | STATIC |
| Machu Picchu | +6$; opp −5$ | | | 4 | COIN |
| Messe | Construct for free a card from the **last row** of the structure, even if not accessible | | | 2 | STRUCTURE |
| Mont-Saint-Michel | Opponent's next turn: the structure card they take **cannot be constructed as a Building** (other uses allowed) | 1 | | 3 | RESTRICT |
| Moscow Kremlin | Steal half the opponent's coins, rounded up | 1 | | 2 | COIN |
| Mount Rushmore | Swap: take 1 opponent blue or green card, give 1 of yours of the same color; +6$ | | | 3 | SWAP |
| Neuschwanstein Castle | End: 1 VP per green card in your city; opp −6$ | | ↻ | | SCORE |
| Notre-Dame de Paris | Choose: take 1 opponent brown card, **or** opp −6$ | | | 2 | CHOICE, STEAL |
| Palace of Westminster | Choose: take 1 opponent grey card, **or** opp −6$ | | | 2 | CHOICE, STEAL |
| Petra | Chains for Horse Breeders, Barracks (II), Fortifications, Siege Workshop, Circus (III); steal 3$ | 1 | | | CHAIN |
| Porcelain Tower | Swap 1 of your blue/yellow/green Buildings for 1 opponent Building of a **different** color among the three | | | | SWAP |
| Potala Palace | Discard 1 accessible structure card, may repeat once; opp −3$ | 1 | | | STRUCTURE |
| Prambanan | Discard 1 opponent blue or green card; steal 6$ | | | | DESTROY |
| Pyramid of Djoser | Your later Wonders cost 1 resource less; +3$ per later Wonder you construct. End: 2 VP per Wonder in your city (this included) | | | | COST, TRIGGER, SCORE |
| Sagrada Familia | Draw 1 random purple card removed at setup, construct it free — Guilds without Pantheon; with Pantheon **only the 2 unused Grand Temples** (`pantheon.md` P-SET-7) | | ↻ | | SETUP-POOL |
| Saint Basil's Cathedral | End: 1 VP per blue card in your city; +6$ | | ↻ | | SCORE |
| Saint Peter's Basilica | All chain symbols: every Age II/III Building with a chain cost is free for you | | | | CHAIN |
| Sheikh Zayed Mosque | **Both players** score 1 VP per coin (instead of per 3); steal 5$ | | | 3 | GLOBAL-RULE |
| Shwedagon Pagoda | +6$; both players bid for the next turn, the winner pays the loser (§3.4) | | | 4 | AUCTION |
| Spring Temple Buddha | Put 25$ on it; unusable until the end of the current Age, then moved to your treasury. If the Wonder leaves play or is un-built first, the coins go with it (not yet gained, so B-EFF-1 does not keep them) | | | 3 | DEFERRED |
| Statue of Liberty | Take the current Age's 3 setup-removed cards: construct 1 free, discard 1, **opponent** constructs the 3rd free | | | 5 | SETUP-POOL, OPP-CHOICE |
| Stonehenge | Replace an unconstructed Wonder (either player's) with a random undrafted Wonder; the replaced one joins the undrafted pool (drawable again); the new one's "on choosing" fires if never obtained before (`agora.md` A-WND-1) | | ↻ | | WONDER-POOL |
| Swords in Rock | opp −3$ | 1 | ↻ | | COIN |
| Sydney Opera House | Opponent's next turn: the structure card they take **cannot be used to construct a Wonder**; steal 3$ | | | 4 | RESTRICT |
| Taj Mahal | Take 1 unconstructed opponent Wonder; opp −2$ | | | 2 | STEAL |
| Teotihuacan | Choose: take 1 opponent yellow card, **or** +10$ | | | 2 | CHOICE, STEAL |
| Terracotta Army | — | 3 | | | |
| Tikal | Chains for Statue, Temple, Aqueduct (II), Gardens, Pantheon, Senate (III); +6$ | | | 4 | CHAIN |
| Tower Bridge | Pay 1$ less per resource bought (min 1$); +5$ | | | 2 | TRADE |
| Tower of Babel | Chains for Library, Dispensary (II), University, Observatory (III); opp −3$ | | ↻ | | CHAIN |
| Trojan Horse | Each later red card of yours gives +1 shield (not Wonders, not retroactive; stacks with Strategy); opp −2$. Shields already gained stay (B-EFF-1); while un-built, it adds none | | ↻ | | STATIC |
| United States Capitol | Scales symbol (as *Law*); +3$ | | | 2 | SCIENCE |
| Vaux-le-Vicomte | End: 1 VP per 3$ in your treasury; steal 3$ | | ↻ | | SCORE |

The 16 existing Wonders are restated in the PDF with the same effects as `base.md` §3.7,
`agora.md` §9 and `pantheon.md` §7.

## 3. Mechanics introduced — and where they land in the model

Mapped onto the mechanics groups of `base.md` §10. The point of this section: if the engine is built
on those groups, **none of the 61 Wonders needs a special case in the turn loop**, except the five
marked ★.

### 3.1 Effect primitives (10.3) — already needed by base / Agora / Pantheon

| Tag | Primitive | Already used by |
|---|---|---|
| COIN | gain $, opponent loses $, steal $, steal half | base, Agora |
| DESTROY | discard an opponent card of color set C | Circus Maximus, Statue of Zeus, Agora |
| STEAL | take an opponent card of color set C / an unbuilt Wonder | Baal, Ra, Agora |
| SWAP | exchange cards between cities under a color rule | Agora |
| UNBUILD | Destroy a built Wonder, or un-build it back to its owner's unbuilt Wonders / discard its tucked card | Anubis, Agora |
| PROGRESS | take / lock / give progress tokens; draw from the setup pool | Great Library, Enki, Agora |
| STRUCTURE | discard / construct structure cards regardless of accessibility | Zeus, Agora |
| SETUP-POOL | draw from cards removed at setup | Agora |
| SCIENCE | grant a symbol | Law, Ishtar |
| CHAIN | grant chain symbols (a set of symbols, or all) | Agora decree (opponent's chains), Engineering |
| COST / TRADE | resource reductions; per-unit trade modifiers | Masonry, Architecture, Agora decrees |
| SCORE | end-game VP per count (color, symbols, coins, Wonders, tokens) | Guilds, Mathematics |
| STATIC | discard income, red-card shield bonus | Agora decree, Strategy |
| CHOICE | "choose one of two effects" | — (new, trivial: a pending decision) |

### 3.2 New mechanics

| Tag | What is new | Model consequence |
|---|---|---|
| COLOR | Djenné counts as any non-purple color for **each** of its owner's coin/VP color counts independently | Color counts go through one function `count(player, color)` that consults modifiers — never `city.filter(color).size()` scattered around |
| GLOBAL-RULE | Sheikh Zayed changes coin scoring **for both players** | Scoring rules read from state modifiers, not constants; a modifier's scope can be "both players" |
| LIMIT | Easter Island lifts the 7-Wonder limit for its owner; with Ra / Taj Mahal / Stonehenge a player can own and build 6+ Wonders | The limit is a rule query (`canStillBuildWonder(player)`), not a counter check inlined in the action; a player's Wonder count is unbounded |
| DEFERRED ★ | Spring Temple Buddha: coins held on the card until end of Age | Card-attached coins (cf. Astarte) + an **end-of-Age hook** |
| RESTRICT ★ | Mont-Saint-Michel / Sydney Opera House forbid an action on the opponent's next turn | A **turn-scoped restriction** in state, consumed by the opponent's next turn. A play-again turn is a new turn, so the ban never covers more than one turn |
| PER-TURN ★ | Christ the Redeemer: a choice made **once per turn**, applying to every purchase that turn | A per-turn modifier fixed by the first purchase of the turn (or an explicit choice); the solver can propose the cheaper option |
| OPP-CHOICE ★ | Ishtar Gate, Statue of Liberty: the **opponent** decides | Pending decisions owned by the non-active player (base §10.1 already allows it) |
| AUCTION ★ | Shwedagon Pagoda: alternating bids for the next turn | A multi-step decision loop between both players (§3.4); the opponent's win grants a **deferred play again** (at the end of their next turn) |
| PEEK | Athena Pronaia: one player learns a face-down card | Per-player knowledge (base §10.8) |
| REPLAY-EFFECT | Hagia Sophia, Karnak: re-run another card's **instant** effects | Each card's instant effects must be a re-invocable unit, separate from its static/scoring effects |
| WONDER-POOL | Stonehenge, Karnak draw from undrafted Wonders | Undrafted Wonders are a seeded, hidden zone |
| TRIGGER | Borobudur (on own shield-giving construction), Pyramid of Djoser (on own Wonder) | Event bus as in base §10.3 |

### 3.3 Interpretations needed (see §6)

The texts are a fan translation; several need a ruling before they can be specified exactly.

### 3.4 Shwedagon Pagoda — auction for the next turn

Both players bid for the right to the next turn; the **winner pays the loser**.

1. The owner opens with a bid *b* ∈ [0, 6]$.
2. Players alternate: each either **raises** (to more than the current bid, up to 6$ and at most
   the coins they hold) or **accepts**, which makes the other player the winner at the current bid.
3. A bid of **6$** wins immediately. The +6$ on the Wonder means the owner can always reach it.
4. **Owner wins:** pays the bid to the opponent and plays again.
   **Opponent wins:** pays the bid to the owner; play continues normally, and the opponent gains a
   **play again at the end of their next turn**. A play-again the owner earned elsewhere that turn
   (Theology…) is **kept** — they play it before the opponent's turn.
5. Built on the turn that empties the structure: no auction (the next starter is chosen by
   `base.md` B-AGE-0); the +6$ stays.

## 4. Costs (TODO — fill in from the physical cards)

Resources in the `base.md` letters (`W C S G P`), coins as a number, shields as a number (§1.1).
Also cross-check the printed VP and shields against §2.

| Wonder | Resources | Coins | Shields |
|---|---|---|---|
| Abu Simbel | — excluded (Leaders) | | |
| Acropolis of Athens | TODO | TODO | TODO |
| Angkor Wat | TODO | TODO | TODO |
| Aqueduct of Segovia | TODO | TODO | TODO |
| Arc de Triomphe | TODO | TODO | TODO |
| Athena Pronaia Sanctuary | TODO | TODO | TODO |
| Atlantis | TODO | TODO | TODO |
| Borobudur | TODO | TODO | TODO |
| Çamlıca Mosque | TODO | TODO | TODO |
| Chichen Itza | TODO | TODO | TODO |
| Christ the Redeemer | TODO | TODO | TODO |
| Colosseum | — excluded (Leaders) | | |
| Cothon of Carthage | TODO | TODO | TODO |
| Derawar Fort | TODO | TODO | TODO |
| Easter Island | TODO | TODO | TODO |
| Eiffel Tower | TODO | TODO | TODO |
| Forbidden City | TODO | TODO | TODO |
| Grand Bazaar | TODO | TODO | TODO |
| Great Mosque of Djenné | TODO | TODO | TODO |
| Great Wall of China | TODO | TODO | TODO |
| Great Ziggurat of Ur | TODO | TODO | TODO |
| Gyeongbokgung Palace | TODO | TODO | TODO |
| Hagia Sophia | TODO | TODO | TODO |
| Hermitage Museum | TODO | TODO | TODO |
| Himeji Castle | TODO | TODO | TODO |
| Ishtar Gate | TODO | TODO | TODO |
| Karnak Temple | TODO | TODO | TODO |
| Leaning Tower of Pisa | TODO | TODO | TODO |
| Louvre Museum | TODO | TODO | TODO |
| Machu Picchu | TODO | TODO | TODO |
| Messe | TODO | TODO | TODO |
| Mont-Saint-Michel | TODO | TODO | TODO |
| Moscow Kremlin | TODO | TODO | TODO |
| Mount Rushmore | TODO | TODO | TODO |
| Neuschwanstein Castle | TODO | TODO | TODO |
| Notre-Dame de Paris | TODO | TODO | TODO |
| Palace of Westminster | TODO | TODO | TODO |
| Petra | TODO | TODO | TODO |
| Porcelain Tower | TODO | TODO | TODO |
| Potala Palace | TODO | TODO | TODO |
| Prambanan | TODO | TODO | TODO |
| Pyramid of Djoser | TODO | TODO | TODO |
| Sagrada Familia | TODO | TODO | TODO |
| Saint Basil's Cathedral | TODO | TODO | TODO |
| Saint Peter's Basilica | TODO | TODO | TODO |
| Sheikh Zayed Mosque | TODO | TODO | TODO |
| Shwedagon Pagoda | TODO | TODO | TODO |
| Spring Temple Buddha | TODO | TODO | TODO |
| Statue of Liberty | TODO | TODO | TODO |
| Stonehenge | TODO | TODO | TODO |
| Swords in Rock | TODO | TODO | TODO |
| Sydney Opera House | TODO | TODO | TODO |
| Taj Mahal | TODO | TODO | TODO |
| Teotihuacan | TODO | TODO | TODO |
| Terracotta Army | TODO | TODO | TODO |
| Tikal | TODO | TODO | TODO |
| Tower Bridge | TODO | TODO | TODO |
| Tower of Babel | TODO | TODO | TODO |
| Trojan Horse | TODO | TODO | TODO |
| United States Capitol | TODO | TODO | TODO |
| Vaux-le-Vicomte | TODO | TODO | TODO |

## 5. Wording normalisation

| PDF wording | Means |
|---|---|
| "Traders Guild" (Grand Bazaar) | Merchants Guild |
| "Raw goods" | brown cards |
| "Immediately play a second turn" | play again (base B-TURN-4) |
| "returned to the box during the game's setup" | the setup-removed pool (progress tokens / Age cards / undrafted Wonders) |
| "last line of the structure" (Messe) | top row (the last row to become accessible); always face up; "even if not accessible" = even if covered |

## 6. Open questions (Extra Wonders)

None remain.

Resolved by the user (2026-10-24): **X1** costs — entered by hand in §4; **X2** Leaders Wonders
excluded (X-POOL-3); **X5** restrictions cover one turn (§3.2); **X6** Christ the Redeemer chooses
once per turn (§3.2); Messe is the top row (§5); **X9** Hagia Sophia — exactly red, yellow, purple,
≤1 each; **X12** Djenné counts for Çamlıca's sets; **X13** Sheikh Zayed changes only the treasury
line — Moneylenders and Vaux-le-Vicomte stack unchanged; Easter Island allows any number of Wonders
(§3.2) and the 7th-Wonder rule still boxes the opponent's last Wonder (**X18**); shield costs, incl.
tokens, Minerva and Poliorcetics (§1.1, **X19**, **X20**); **X3** draw pools (X-POOL-5); **X4** pool
configuration (X-POOL-6); **X7** Shwedagon (§3.4); **X8** Messe's row is face up (§5); **X10** Karnak
leaves the target unbuilt with its owner; **X11** Ishtar Gate — the opponent decides 0/1/2 shields
(0/4/8$) before the pawn moves; **X14** Porcelain Tower is optional; **X15** a progress token's
instant effects apply to whoever gains it; **X16** Eiffel Tower takes all remaining when fewer than 3;
**X17** Spring Temple Buddha's coins move to the treasury before final scoring.
