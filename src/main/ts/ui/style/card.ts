// One anatomy for buildings, Wonders and tokens (design §2): effects banner, cost overlay, art,
// name bar, chain badge. The card scales with --w, so one rule set serves all three sizes.
export const CARD = `
.sprite { position: absolute; width: 0; height: 0; overflow: hidden; }
.icon { width: 1em; height: 1em; flex: none; }
.icon-vp { color: #f2c94c; } .icon-coin { color: #e6b422; } .icon-loot { color: #b9892b; }
.icon-shield { color: #c9ccd1; }
.icon-wood { color: #a8743c; } .icon-stone { color: #9a9a9a; } .icon-clay { color: #c8693a; }
.icon-glass { color: #62bdd0; } .icon-papyrus { color: #dcc791; }

.card {
  --w: 75px; --u: var(--w); --c: var(--muted);
  position: relative; isolation: isolate; box-sizing: border-box; flex: none;
  width: var(--w); aspect-ratio: 3 / 4; display: flex; flex-direction: column;
  font: 600 max(7px, calc(var(--u) / 8)) / 1.1 var(--sans);
  color: #fff; background: var(--c);
  border: 1px solid #0006; box-shadow: 0 1px 2px #0004; border-radius: var(--radius); overflow: visible;
}
.card--small { --w: 38px; } .card--large { --w: 120px; }
.card--wonder { --c: var(--wonder); aspect-ratio: 4 / 3; --w: 120px; --u: calc(var(--w) * .66); }
.card--wonder.card--small { --w: 60px; } .card--wonder.card--large { --w: 180px; }
.token { width: 56px; aspect-ratio: 1; flex: none; }
.token--small { width: 34px; } .token--large { width: 96px; }
.token .icon { width: 100%; height: 100%; filter: drop-shadow(0 1px 2px #0006); }
.card--token { --c: var(--token); aspect-ratio: 1; --w: 64px; }
.card--token.card--small { --w: 34px; } .card--token.card--large { --w: 96px; }
.color-brown { --c: var(--c-brown); } .color-grey { --c: var(--c-grey); }
.color-blue { --c: var(--c-blue); } .color-green { --c: var(--c-green); }
.color-yellow { --c: var(--c-yellow); } .color-red { --c: var(--c-red); }
.color-purple { --c: var(--c-purple); }

.card__banner {
  --span: 4;
  display: flex; flex-wrap: nowrap; gap: calc(var(--u) * .02); align-items: center;
  justify-content: center; min-height: calc(var(--u) * .3); padding: 1px;
  position: relative; background: #0003; color: #fff; border-radius: calc(var(--radius) - 1px) calc(var(--radius) - 1px) 0 0;
}
.glyph .icon-or { margin: 0 -.4em; }
.glyph {
  --n: 1;
  position: relative; display: inline-flex;
  font-size: min(calc(var(--u) * .25), calc(var(--w) * .92 / (var(--span))));
}
.glyph.is-pile { width: calc(1em + (var(--n) - 1) * .25em); height: calc(1em + (var(--n) - 1) * .25em); }
.glyph__pile { position: absolute; display: inline-flex; left: calc(var(--i) * 1em); top: calc(var(--i) * 1em); width: 1em; height: 1em; }
.glyph.has-corner { margin: 0 calc(var(--u) * .02) 0 calc(var(--u) * .05); }
.glyph.is-better { margin-right: -.2em; } .glyph.is-better + .glyph { margin-left: 0; }
.glyph__corner { position: absolute; bottom: -.15em; display: inline-flex; font-size: .5em; }
.glyph__corner.is-right { right: -.15em; } .glyph__corner.is-left { left: -.15em; }
/* On a pile the corners belong to the card on top, which is the one at the front right. */
.glyph.is-pile .glyph__corner.is-left { left: calc((var(--n) - 1) * .5em - .15em); }
.glyph__corner b { font-size: .9em; }
.card__banner .glyph .icon { filter: drop-shadow(0 0 .5px #ffffff80); }
.card--standard .card__banner, .card--large .card__banner {
  background-image:
    repeating-linear-gradient(45deg, #ffffff14 0 1px, transparent 1px 4px),
    repeating-linear-gradient(-45deg, #00000014 0 1px, transparent 1px 4px),
    linear-gradient(#0000001f, #0000000a);
  box-shadow: inset 0 -1px 0 #ffffff26, inset 0 1px 2px #0000002e;
}
.glyph .icon-vp + b { transform: translateY(-.06em); }
.glyph .icon-vp + b { color: #fff; text-shadow: 0 0 2px #0009, 0 0 1px #0009; }
.glyph .icon-coin-lost + b { color: #e53935; -webkit-text-stroke: .14em #000; paint-order: stroke fill; }
.glyph b {
  position: absolute; inset: 0; display: grid; place-items: center; font: 800 .62em/1 var(--figures); font-variant-numeric: lining-nums;
  color: #1b1a17;
}
.glyph.tint-brown .icon, .glyph__pile.tint-brown .icon { color: var(--c-brown); } .glyph.tint-grey .icon, .glyph__pile.tint-grey .icon { color: var(--c-grey); }
.glyph.tint-blue .icon, .glyph__pile.tint-blue .icon { color: var(--c-blue); } .glyph.tint-green .icon, .glyph__pile.tint-green .icon { color: var(--c-green); }
.glyph.tint-yellow .icon, .glyph__pile.tint-yellow .icon { color: var(--c-yellow); } .glyph.tint-red .icon, .glyph__pile.tint-red .icon { color: var(--c-red); }
.glyph.tint-purple .icon, .glyph__pile.tint-purple .icon { color: var(--c-purple); }

.card__cost {
  position: absolute; top: calc(var(--u) * .38); left: 0; z-index: 1; display: flex;
  flex-direction: column; align-items: flex-start; gap: calc(var(--u) * .08);
  font-size: calc(var(--u) * .13);
}
.cost__bar {
  display: flex; flex-direction: row; align-items: center; gap: calc(var(--u) * .01);
  height: .6em; padding: 0 calc(var(--u) * .05) 0 calc(var(--u) * .02); background: #6f6a6099;
  border-radius: 0 calc(var(--u) * .1) calc(var(--u) * .1) 0;
}
.cost__res { display: inline-flex; }
.cost__coin { position: relative; display: inline-flex; }
.cost__coin b { position: absolute; inset: 0; display: grid; place-items: center; font: 800 .62em/1 var(--figures); font-variant-numeric: lining-nums; color: #1b1a17; text-shadow: 0 0 2px #fff; }
.cost__free {
  display: inline-grid; place-items: center; width: 1em; height: 1em; box-sizing: border-box;
  background: var(--c); border: 1px solid #0004; border-radius: .22em; box-shadow: inset 0 0 0 1px #ffffff2e;
}
.cost__free .icon { color: #fff; width: .78em; height: .78em; }
.card--wonder .card__chain { display: none; }
.card__art {
  flex: 1; display: grid; place-items: center; min-height: 0;
  background: radial-gradient(circle at 50% 40%, #fff3, #0000 70%);
  color: #fff; font-size: calc(var(--u) * .42);
}
.card--wonder .card__art { font-size: calc(var(--u) * .26); }
.card__name {
  padding: 1px 3px; font-size: .88em; text-align: center; white-space: nowrap; overflow: hidden;
  text-overflow: ellipsis; background: #0006; color: #fff; font-family: var(--serif);
  border-radius: 0 0 calc(var(--radius) - 1px) calc(var(--radius) - 1px);
}
.card__chain {
  position: absolute; top: 50%; right: calc(var(--u) * .04); transform: translateY(-50%); z-index: 1; display: grid;
  place-items: center; font-size: calc(var(--u) * .12); color: #fff;
  filter: drop-shadow(0 0 1px #000c) drop-shadow(0 1px 2px #0008);
}
.card--small .card__name, .card--small .card__cost, .card--small .card__chain { display: none; }
.card--small .card__art { display: none; }
.card--small { aspect-ratio: auto; min-height: calc(var(--u) * .3); }

.card--wonder.is-unbuilt { opacity: .92; border-style: dashed; border-color: #fff9; }

.card--back { --c: var(--muted); background:
  repeating-linear-gradient(45deg, color-mix(in srgb, var(--c) 80%, #000) 0 3px, var(--c) 3px 6px);
  box-shadow: inset 0 0 0 3px var(--surface); }
.card--back.back-brown { --c: var(--c-brown); } .card--back.back-blue { --c: var(--c-blue); }
.card--back.back-dark-purple { --c: #3d2a50; } .card--back.back-violet { --c: var(--c-purple); }
`;
