package io.turnbase.games.sevenwondersduel.effect;

import io.turnbase.games.sevenwondersduel.agenda.ComponentOption;
import io.turnbase.games.sevenwondersduel.agenda.Option;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.zone.Placements;
import io.turnbase.games.sevenwondersduel.zone.Spots;
import io.turnbase.games.sevenwondersduel.zone.Zones;

/**
 * The owner picks any discarded card and builds it for free: it joins the owner's city and its own
 * effects run. The turn already ends with the source that carries this effect, so none is queued.
 * An empty discard pile means nothing happens (B-EFF-3).
 */
public record BuildFromDiscard() implements Effect {

  @Override
  public void apply(Resolution resolution, PlayerId owner) {
    resolution.decide(
        owner,
        Placements.idsIn(resolution.state().placements(), Zones.DISCARD).stream()
            .<Option>map(ComponentOption::new)
            .toList());
  }

  @Override
  public void resume(Resolution resolution, PlayerId owner, Option answer) {
    ComponentId card = ((ComponentOption) answer).component();
    resolution.move(card, Spots.city(owner));
    resolution.queueEffects(card, owner);
  }
}
