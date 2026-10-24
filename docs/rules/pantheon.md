# 7 Wonders Duel — Pantheon Expansion (consolidated)

Source: `docs/rules/7-wonders-duel-pantheon.pdf` (Repos Production, 2016, 16 pp.). Written as a **delta
against [`base.md`](base.md)**. Rule IDs are `P-…`; gaps are **[OPEN-Pn]**, listed in §10.

## 1. What Pantheon changes — summary

| Mechanics group (`base.md` §10) | Change |
|---|---|
| 10.1 Game flow | Age I: token pickup on reveal opens a **Divinity placement** decision. Age II–III: new main action **activate a Pantheon card** — does *not* take a structure card |
| 10.2 Cost solver | New target kind: Pantheon space (coin cost by space and player, Offering tokens, Sanctuary −2); Grand Temple free via Mythology token; *Engineering* (chained cards for 1$) |
| 10.3 Effects | 15 Divinities + Gate; Divinities are one-shot activations that stay in front of the player (some keep scoring) |
| 10.4 Zones | Mythology decks (5, ordered), Pantheon spaces (6, face down then up), tokens on structure cards, coins on a card (Astarte), progress tokens on a card (Enki), token on an opponent card (Snake) |
| 10.5 Military | Minerva pawn **stops** the Conflict pawn; Neptune applies military tokens out of order; *Poliorcetics* hook per step |
| 10.6 Science | New symbol sources: Ishtar (Scales), Nisaba (copy of an opponent green symbol) |
| 10.7 Scoring | Guilds replaced by **Grand Temples** (set scoring 5/12/21); new lines: Divinities, Mysticism |
| 10.8 Hidden info | Face-down Divinities in the Pantheon are known **only to the player who placed them**; that player also knows the **top of that Mythology deck** (they put the other card back) |

## 2. Components

| Component | Count | Notes |
|---|---|---|
| Pantheon board | 1 | 6 spaces; each prints two activation costs, one per player |
| Divinity cards | 15 | 5 Mythologies × 3 |
| Gate card | 1 | fills the last empty Pantheon space in Age II |
| Grand Temple cards | 5 | 3 used per game; replace the Guilds |
| Mythology tokens | 10 | 5 used per game (Age I) |
| Offering tokens | 3 | values 2, 3, 4 (Age II) |
| Snake token | 1 | Nisaba |
| Minerva pawn | 1 | Minerva |
| Wonders | 2 | The Sanctuary, The Divine Theater |
| Progress tokens | 3 | Mysticism, Poliorcetics, Engineering |

### 2.1 Divinities

| Mythology (theme) | Divinity | Effect on activation |
|---|---|---|
| Greek (civilian) | Aphrodite | 9 VP at game end |
| | Hades | Take all cards discarded since the start of the game (not setup removals), construct 1 for free |
| | Zeus | Put any structure card (face up **or face down**, accessible or not) in the discard pile; take any token on it |
| Phoenician (commercial) | Astarte | Put 7$ from the bank on the card — not in your treasury, immune to coin loss, spendable normally; each coin left = 1 VP at game end. When paying, the owner **chooses the split** between the card and the treasury (it matters: a card coin is 1 VP, a treasury coin ⅓). Never counted by "coins in your city/treasury" effects (`base.md` B-EFF-2) |
| | Baal | Steal a brown or grey card from the opponent's city into yours |
| | Tanit | +12$ |
| Mesopotamian (science) | Enki | **When revealed** (Age II setup, Gate, Divine Theater): draw 2 random progress tokens from the setup-removed pool, face up on the card. On activation: take 1; the other returns to the pool |
| | Ishtar | Grants the Scales symbol (same as *Law*) |
| | Nisaba | Put the Snake on an opponent's green card; Nisaba is worth that card's scientific symbol |
| Egyptian (wonders) | Anubis | Discard the Age card tucked under **any** constructed Wonder (yours or the opponent's). Instant effects stay. The Wonder becomes unbuilt and may be rebuilt (effects apply again) |
| | Isis | Take 1 card from the discard pile and use it to construct one of your Wonders for free — shield costs (Extra Wonders) are waived too; the Wonder's play again applies |
| | Ra | Steal 1 unbuilt Wonder from the opponent |
| Roman (military) | Mars | 2 shields |
| | Minerva | Place the Minerva pawn on any track space (capitals included; the pawn's current space too — it then only stops a later re-entry). When the Conflict pawn would **enter** that space, it stops and the rest of the movement is lost; then the Minerva pawn is discarded |
| | Neptune | Discard 1 military token (any side) without applying it, then apply another one (any side), which is then discarded. The applied token always benefits the **activator** (FAQ p. 16); with Agora tokens, the activator's cubes are used. Only one token left → it is discarded, nothing is applied |

**Gate** — placed in Age II in the **single** empty Pantheon space (always exactly one: 5 Mythology
tokens fill 5 of 6 spaces, and every token draw finds a card — 3 per deck, at most 2 tokens per
Mythology, so even a Divine Theater pull in Age I leaves one; a token whose deck holds 1 card places
it). Costs **twice** its space's cost. Reveal the top Divinity of each Mythology deck, activate
one for free, put the others back face down on their decks (same order).

## 3. Setup (delta)

- **P-SET-1** Base setup, then the Age I structure.
- **P-SET-2** Put 5 random Mythology tokens face down on the marked Age I face-down cards, then reveal
  the **tokens** (the cards stay face down). Other 5 tokens → box. Positions: face-down row of 3 →
  positions 1 and 3; face-down row of 5 → positions 1, 3, 5.
- **P-SET-3** Wonder draft (base rules; pool includes the 2 new Wonders).
- **P-SET-4** Pantheon board above the base board; shuffle each Mythology deck (3 cards) face down.
- **P-SET-5** Offering tokens, Gate, Snake, Minerva next to the board.
- **P-SET-6 Age II setup** — put the Gate face up in the empty Pantheon space; **reveal all
  Divinities** in the Pantheon (Enki's reveal effect fires); lay out the Age II structure; put the 3
  Offering tokens randomly face down on the marked spaces (face-down row of 5 → positions 1, 3, 5),
  then reveal them.
- **P-SET-7 Age III setup** — remove the 7 Guilds and **3 random Age III cards** to the box (instead
  of the base 3), add 3 random Grand Temples unseen; other 2 → box unseen. Done at the start of Age
  III or eagerly at game setup — equivalent, since all of it is hidden. Senators are added after it
  (`agora.md` A-SET-2), so they are never among the removed cards. If an effect needs the unused
  temples before Age III (Sagrada Familia), they are chosen at that moment.

> With 5 Mythology tokens and 6 spaces, the Pantheon always holds 5 Divinities + the Gate.

## 4. Age I — token pickup and Divinity placement

- **P-TOK-1** At end of turn, when a face-down card carrying a token is revealed, the **active player**
  first takes the token.
- **P-TOK-2 Mythology token** — the player:
  1. draws the top 2 Divinities of that Mythology,
  2. places 1 face down in an **empty Pantheon space of their choice**,
  3. puts the other face down **on top** of that Mythology deck.
  The token stays with the player (Grand Temple, Mysticism).
- **P-TOK-3** Several tokens revealed in one turn → resolve one by one, in the active player's order
  (the layouts make this rare, and the order does not change the outcome).
- **P-TOK-4 Offering token** (Age II) — kept; single-use discount of its value on one Pantheon
  activation; discarded after use; no change given.
- **P-TOK-5** Pick-up happens **before** a play-again turn starts (base B-STR-2 order).

## 5. Activating a Pantheon card (Age II and III)

- **P-ACT-1** New main action, alternative to taking a structure card: choose a Divinity or the Gate
  in the Pantheon.
- **P-ACT-2** Cost = **your** printed cost for that space (×2 for the Gate), minus Offering tokens you
  choose to spend, minus 2 with *The Sanctuary*; paid in coins to the bank (Astarte's coins may be
  used). Minimum 0: an Offering larger than the remaining cost may be used, with no change given.
- **P-ACT-3** Space costs (per player, mirrored): the spaces closest to you cost **3, 4, 5**, the
  farthest **6, 7, 8**; your opponent reads the same board the other way round.
- **P-ACT-4** Take the card, place it in front of you, apply its effect immediately. The space stays
  empty.
- **P-ACT-5** A Divinity may be activated even if its effect is impossible (FAQ).
- **P-ACT-6** Activating does **not** take a structure card, and it is the whole turn (no play again
  unless the effect says so). The end-of-turn reveal still runs: Zeus can uncover cards, and can
  empty the structure, ending the Age.
- **P-ACT-7** Activated Divinities stay in front of the player and are **part of the city**, but **no
  mechanic can target them** (no steal, swap, discard, color count). Aphrodite and Astarte score at
  the end; Ishtar and Nisaba keep providing a symbol; Mars's shields move the pawn once.

## 6. Grand Temples

- **P-GT-1** 5 Grand Temples, 3 in Age III. They are **purple** Buildings and share the Guilds'
  dark-purple back (`base.md` §3.10), so effects on "purple cards" target them.
- **P-GT-2** A Grand Temple may be constructed by paying its cost, **or** for free if you hold a
  Mythology token with that temple's symbol — the token is not spent.
- **P-GT-3** End-game: 1 / 2 / 3 Grand Temples built → **5 / 12 / 21** VP (total, not per temple).
- **P-GT-4** One Grand Temple per Mythology; a Mythology token of that Mythology builds it free.

  | Mythology | Grand Temple | Cost |
  |---|---|---|
  | Greek | TODO: name | TODO: cost |
  | Phoenician | TODO: name | TODO: cost |
  | Mesopotamian | TODO: name | TODO: cost |
  | Egyptian | TODO: name | TODO: cost |
  | Roman | TODO: name | TODO: cost |

  > **TODO (user):** fill in the names and costs from the physical cards.

## 7. Wonders and progress tokens

| Wonder | Cost | Effects | VP |
|---|---|---|---|
| The Sanctuary | S S P G | Pantheon activation costs −2$ for you (static); play again | 0 |
| The Divine Theater | W W P P G | Reveal a whole Mythology deck of your choice, activate 1 for free, restack the rest in **your chosen order** | 2 |

| Token | Effect |
|---|---|
| Mysticism | End: 2 VP per Mythology **and** Offering token you still hold |
| Poliorcetics | Each time **you** move the pawn forward, the opponent loses 1$ per space |
| Engineering | Any card with a chain symbol under its cost can be constructed for 1$ even without the chain. Not a chain build (no *Urbanism* coins) |

## 8. FAQ rulings (normative)

- **P-FAQ-1** Impossible effects can still be activated.
- **P-FAQ-2** Offerings never refund.
- **P-FAQ-3** Ra: a player can own 5 Wonders (the other 2+); still max 7 constructed overall.
- **P-FAQ-4** Anubis after the 7th Wonder: the 8th stays in the box; the un-built Wonder may be
  rebuilt, because the count dropped to 6 (`base.md` B-ACT-5).
- **P-FAQ-5** Nisaba's and Ishtar's symbols count for pairs (progress tokens) and supremacy.
- **P-FAQ-6** Minerva can be placed on a capital.
- **P-FAQ-7** Enki not chosen at Gate / Divine Theater: its revealed tokens go back to the
  setup-removed pool. Returned tokens mix with that pool.
- **P-FAQ-8** Pair with no tokens left on the board: nothing.
- **P-FAQ-9** Engineering + Urbanism: no 4$.
- **P-FAQ-10** Neptune may pick a token on either side (e.g. one that hurts the opponent).

## 9. Engine notes

- **Per-player knowledge.** In Age I each Pantheon space and each Mythology deck top is known by
  exactly one player (or nobody). This is the clearest case for modelling hidden information as
  *who knows what* rather than a public/private flag.
- **Pawn movement must be step-wise with interception** (Minerva) and per-step hooks (Poliorcetics,
  military tokens, supremacy).
- **Un-building a Wonder** (Anubis) means "constructed" is a reversible state; Wonder static effects
  must be derived from current state.
- **Card-attached state**: coins on Astarte, tokens on Enki, Snake on a green card (follows the card
  if it moves; Nisaba loses the symbol as soon as that card leaves the opponent's city).
- **Divine Theater restack order** is a player decision with up to 3! outcomes.

## 10. Open questions (Pantheon)

None remain. Zeus's face-down target is revealed to both, since the discard pile is public (P2, P10 resolved).

Resolved by the user (2026-10-24): **P1** pickup order (P-TOK-3); **P3** space costs (P-ACT-3);
**P4** Divinities in the city (P-ACT-7); **P5** Grand Temples are purple (P-GT-1); **P6** one temple
per Mythology — names and costs are a TODO in P-GT-4; **P7** Snake (§9); **P8** the token taker
places the Divinity (P-TOK-2); **P9** Wonder costs (§7).
