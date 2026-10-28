import { useEffect, useRef, useState, type CSSProperties } from "react";
import type { Catalog } from "../core/catalog.js";
import type { Rect } from "../core/flip.js";
import type { Offer } from "../core/moves.js";
import type { ComponentId, DuelView, PlayerId, QuoteView, WireAction } from "../core/wire.js";
import { Card } from "./Card.js";
import { Icon } from "./icons.js";
import { HoverFloat, HoverQuote } from "./QuoteTable.js";

/** Where the selected card sits in the structure area, and how big that area is, in px. */
export interface Placement {
  card: Rect;
  /** Where the pyramid starts, so the blur leaves what sits above it (the token tray) sharp. */
  top: number;
  width: number;
  height: number;
}

/** Room the actions need beside the card: the Wonder step, the wider of the two. */
const BESIDE = 250;
const GAP = 12;
/** The blur starts this far above the pyramid, so its edge falls in the gap and not on the top cards. */
const EDGE = 6;

interface Props {
  view: DuelView;
  catalog: Catalog;
  self: PlayerId;
  component: ComponentId;
  offers: Offer[];
  /** Null until the card has been measured. */
  placement: Placement | null;
  /** The card is going back down; the actions are no longer live. */
  leaving?: boolean;
  send: (action: WireAction) => void;
  onClose: () => void;
}

/** What a quote asks of the player, in words: the coins to pay, or why nothing is due. */
export function priceText(quote: QuoteView): string {
  if (quote.chained) return "Free by chain";
  if (quote.total > 0) return `${String(quote.total)} coins`;
  return quote.lines.length > 0 ? "Production satisfied" : "Free";
}

function Price({ quote, compact = false }: { quote: QuoteView | undefined; compact?: boolean }) {
  if (quote === undefined) return null;
  return (
    <span className="price" aria-label={priceText(quote)}>
      {quote.total > 0 && <Icon id="coin" />}{" "}
      {compact && quote.total > 0 ? quote.total : priceText(quote)}
      {compact && <HoverFloat>{priceText(quote)}</HoverFloat>}
    </span>
  );
}

/**
 * The three things the viewer may do with the selected card, with their prices: Build, construct a
 * Wonder (a second step, to pick which) and Discard. The structure blurs behind, the card rises where
 * it stands, and the actions float beside it: on the right, or on the left when the right is short.
 */
export function Actions({
  view,
  catalog,
  self,
  component,
  offers,
  placement,
  leaving = false,
  send,
  onClose,
}: Props) {
  const [step, setStep] = useState<"actions" | "wonder">("actions");
  const dialog = useRef<HTMLDivElement>(null);
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

  const beside = ((): CSSProperties => {
    if (placement === null) return { display: "none" };
    const { card, width, height } = placement;
    const top = Math.min(Math.max(card.top + card.height / 2, 90), Math.max(90, height - 90));
    return card.left + card.width + GAP + BESIDE <= width
      ? { left: card.left + card.width + GAP, top }
      : { right: width - card.left + GAP, top, alignItems: "flex-end" };
  })();
  const lift: CSSProperties | undefined =
    placement === null
      ? { display: "none" }
      : {
          left: placement.card.left,
          top: placement.card.top,
          ["--lw" as string]: `${String(placement.card.width)}px`,
        };

  return (
    <div
      ref={dialog}
      className={`spotlight${leaving ? " is-leaving" : ""}`}
      inert={leaving}
      role="dialog"
      aria-label={`Play ${catalog.name(component)}`}
      onKeyDown={(e) => {
        if (e.key === "Escape") onClose();
      }}
    >
      <div
        className="spotlight__scrim"
        style={placement === null ? undefined : { top: placement.top - EDGE }}
        onClick={onClose}
      />
      <div className="spotlight__card" style={lift}>
        {entry && <Card entry={entry} size="standard" />}
      </div>
      <div className="spotlight__beside" style={beside}>
        {step === "actions" ? (
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
        ) : (
          <>
            <h4>Build which Wonder?</h4>
            <div className="actions__wonders">
              {unbuilt.map((id) => {
                const offer = wonderOffers.find(
                  (o) => o.action.type === "buildWonder" && o.action.wonder === id,
                );
                const wonder = catalog.get(id);
                const quote = quoteOf(id);
                return (
                  <div key={id} className="wonderpick">
                    <button
                      type="button"
                      disabled={offer === undefined}
                      onClick={() => {
                        if (offer) act(offer.action);
                      }}
                    >
                      {wonder && <Card entry={wonder} size="standard" />}
                      <Price quote={quote} compact />
                    </button>
                    {quote && <HoverQuote quote={quote} />}
                  </div>
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
    </div>
  );
}
