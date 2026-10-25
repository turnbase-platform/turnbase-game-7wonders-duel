import { useEffect, useLayoutEffect, useRef, useState } from "react";
import type { Catalog } from "../core/catalog.js";
import type { Rect } from "../core/flip.js";
import type { Offer } from "../core/moves.js";
import type { ComponentId, DuelView, PlayerId, QuoteView, WireAction } from "../core/wire.js";
import { Card } from "./Card.js";
import { Icon } from "./icons.js";
import { EASE, MOTION_MS, prefersReducedMotion } from "./motion.js";

interface Props {
  view: DuelView;
  catalog: Catalog;
  self: PlayerId;
  component: ComponentId;
  offers: Offer[];
  /** The card's place on the board, in the board's own pixels, which the lifted copy rises from. */
  origin: Rect | null;
  send: (action: WireAction) => void;
  onClose: () => void;
}

/** What a quote asks of the player, in words. */
export function priceText(quote: QuoteView): string {
  if (quote.total === 0) return quote.chained ? "Free by chain" : "Free";
  const trade = quote.tradeCost > 0 ? ` (${String(quote.tradeCost)} in trade)` : "";
  return `${String(quote.total)} coins${trade}`;
}

function Price({ quote, compact = false }: { quote: QuoteView | undefined; compact?: boolean }) {
  if (quote === undefined) return null;
  return (
    <span className="price" title={priceText(quote)}>
      <Icon id="coin" /> {compact ? quote.total : priceText(quote)}
    </span>
  );
}

/**
 * The three things the viewer may do with the selected card, with their prices: Build, construct a
 * Wonder (a second step, to pick which) and Discard. The board blurs behind, the card rises from its
 * place to the middle, and the actions float beneath it.
 */
export function Actions({ view, catalog, self, component, offers, origin, send, onClose }: Props) {
  const [step, setStep] = useState<"actions" | "wonder">("actions");
  const dialog = useRef<HTMLDivElement>(null);
  const lifted = useRef<HTMLDivElement>(null);
  const risen = useRef(false);
  const entry = catalog.get(component);
  const mine = offers.filter((o) => o.target === component);
  const quoteOf = (id: ComponentId): QuoteView | undefined =>
    view.quotes.find((q) => q.player === self && q.component === id);
  const quote = quoteOf(component);
  const coins = view.players.find((p) => p.id === self)?.coins ?? 0;

  // Focus goes into the dialog, and back to the card it was opened from.
  useEffect(() => {
    const before = document.activeElement;
    return () => {
      if (before instanceof HTMLElement && before.isConnected) before.focus();
    };
  }, []);
  useEffect(() => {
    dialog.current?.querySelector<HTMLElement>("button:not(:disabled)")?.focus();
  }, [step]);
  // The card rises once, from where it sat to where it now stands, scaled up on the way.
  const known = origin !== null;
  useLayoutEffect(() => {
    const card = lifted.current;
    const board = dialog.current?.offsetParent;
    if (
      !known ||
      risen.current ||
      !card ||
      !board ||
      (card as Partial<HTMLElement>).animate === undefined
    )
      return;
    risen.current = true;
    if (prefersReducedMotion()) return;
    const to = card.getBoundingClientRect();
    const box = board.getBoundingClientRect();
    card.animate(
      [
        {
          transform: `translate(${String(origin.left - (to.left - box.left))}px, ${String(origin.top - (to.top - box.top))}px) scale(${String(origin.width / to.width)})`,
          transformOrigin: "0 0",
        },
        { transform: "none", transformOrigin: "0 0" },
      ],
      { duration: MOTION_MS.travel, easing: EASE },
    );
  }, [known, origin]);

  const act = (action: WireAction): void => {
    send(action);
    onClose();
  };
  const build = mine.find((o) => o.action.type === "build");
  const discard = mine.find((o) => o.action.type === "discard");
  const wonderOffers = mine.filter((o) => o.action.type === "buildWonder");
  const unbuilt = view.visible
    .filter((p) => p.zone === "wonders.unbuilt" && p.key === self)
    .map((p) => p.component);

  return (
    <div
      ref={dialog}
      className="spotlight"
      role="dialog"
      aria-label={`Play ${catalog.name(component)}`}
      onKeyDown={(e) => {
        if (e.key === "Escape") onClose();
      }}
    >
      <div className="spotlight__scrim" onClick={onClose} />
      <div ref={lifted} className="spotlight__card">
        {entry && <Card entry={entry} size="large" />}
      </div>
      {step === "actions" ? (
        <>
          <div className="actions__buttons">
            <button
              type="button"
              disabled={build === undefined}
              onClick={() => {
                if (build) act(build.action);
              }}
            >
              Build <Price quote={quote} />
              {build === undefined && quote && (
                <small>
                  Needs {quote.total} coins, you have {coins}
                </small>
              )}
            </button>
            <button
              type="button"
              disabled={wonderOffers.length === 0}
              onClick={() => {
                setStep("wonder");
              }}
            >
              Wonder
              {wonderOffers.length === 0 && (
                <small>{unbuilt.length === 0 ? "No Wonder left" : "Can't afford one"}</small>
              )}
            </button>
            <button
              type="button"
              disabled={discard === undefined}
              onClick={() => {
                if (discard) act(discard.action);
              }}
            >
              Discard
            </button>
          </div>
        </>
      ) : (
        <>
          <h4>Build which Wonder?</h4>
          <div className="actions__wonders">
            {unbuilt.map((id) => {
              const offer = wonderOffers.find(
                (o) => o.action.type === "buildWonder" && o.action.wonder === id,
              );
              const wonder = catalog.get(id);
              return (
                <button
                  key={id}
                  type="button"
                  disabled={offer === undefined}
                  onClick={() => {
                    if (offer) act(offer.action);
                  }}
                >
                  {wonder && <Card entry={wonder} size="standard" />}
                  <Price quote={quoteOf(id)} compact />
                </button>
              );
            })}
          </div>
          <button
            type="button"
            onClick={() => {
              setStep("actions");
            }}
          >
            Back
          </button>
        </>
      )}
    </div>
  );
}
