package io.turnbase.games.sevenwondersduel.cost;

import io.turnbase.games.sevenwondersduel.effect.Resolution;
import io.turnbase.games.sevenwondersduel.effect.Trigger;
import io.turnbase.games.sevenwondersduel.event.ChainBuilt;
import io.turnbase.games.sevenwondersduel.id.PlayerId;

/**
 * The owner receives {@code coins} whenever they build a card through a chain symbol (Urbanism).
 *
 * @param coins what the owner receives
 */
public record ChainBonus(int coins) implements Trigger<ChainBuilt> {

  @Override
  public Class<ChainBuilt> eventType() {
    return ChainBuilt.class;
  }

  @Override
  public void react(ChainBuilt event, PlayerId owner, Resolution resolution) {
    if (event.builder().equals(owner)) {
      resolution.changeCoins(owner, coins);
    }
  }
}
