package io.turnbase.games.sevenwondersduel.cost;

import io.turnbase.games.sevenwondersduel.effect.Badge;
import io.turnbase.games.sevenwondersduel.effect.Resolution;
import io.turnbase.games.sevenwondersduel.effect.Trigger;
import io.turnbase.games.sevenwondersduel.event.TradePaid;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import java.util.List;

/**
 * The owner receives what the opponent pays for bought resources, instead of the bank (B-TRADE-4,
 * Economy). Printed coin costs are not trade payments, and the owner's own purchases still go to
 * the bank.
 */
public final class TradeRevenue implements Trigger<TradePaid> {

  @Override
  public Class<TradePaid> eventType() {
    return TradePaid.class;
  }

  @Override
  public void react(TradePaid event, PlayerId owner, Resolution resolution) {
    if (!event.payer().equals(owner)) {
      resolution.changeCoins(owner, event.amount());
    }
  }

  @Override
  public List<Badge> badges() {
    return List.of(Badge.of("tradeRevenue"));
  }
}
