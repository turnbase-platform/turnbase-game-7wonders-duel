package io.turnbase.games.sevenwondersduel.effect;

import io.turnbase.games.sevenwondersduel.catalog.ComponentType;
import io.turnbase.games.sevenwondersduel.event.ComponentMoved;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.zone.Location;
import io.turnbase.games.sevenwondersduel.zone.Zones;

/**
 * The owner plays again whenever they build a Wonder (Theology). Granting a flag that is already
 * set changes nothing, so it does not stack with a Wonder's own play again.
 */
public final class WonderBonusTurn implements Trigger<ComponentMoved> {

  @Override
  public Class<ComponentMoved> eventType() {
    return ComponentMoved.class;
  }

  @Override
  public void react(ComponentMoved event, PlayerId owner, Resolution resolution) {
    Location to = event.to();
    if (to.zone().equals(Zones.WONDERS_BUILT)
        && to.key().equals(owner.value().toString())
        && resolution.rules().catalog().get(event.component()).type() == ComponentType.WONDER) {
      resolution.setPlayAgain(owner, true);
    }
  }
}
