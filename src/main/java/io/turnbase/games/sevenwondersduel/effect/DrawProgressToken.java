package io.turnbase.games.sevenwondersduel.effect;

import io.turnbase.games.sevenwondersduel.agenda.ComponentOption;
import io.turnbase.games.sevenwondersduel.agenda.Option;
import io.turnbase.games.sevenwondersduel.catalog.Tag;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.zone.Placements;
import io.turnbase.games.sevenwondersduel.zone.Spots;
import io.turnbase.games.sevenwondersduel.zone.Zones;
import java.util.List;

/**
 * The owner draws {@code drawn} random progress tokens from the Box, learns them (the opponent does
 * not), and takes one; the others stay in the Box. With fewer in the Box the owner is offered all
 * of them, with none nothing happens (B-EFF-3).
 */
public record DrawProgressToken(int drawn) implements Effect {

  public DrawProgressToken {
    if (drawn <= 0) {
      throw new IllegalArgumentException("drawn must be positive: " + drawn);
    }
  }

  @Override
  public void apply(Resolution resolution, PlayerId owner) {
    List<ComponentId> inBox =
        Placements.idsIn(resolution.state().placements(), Zones.BOX).stream()
            .filter(id -> resolution.rules().catalog().get(id).tags().contains(Tag.PROGRESS))
            .toList();
    List<ComponentId> offered = resolution.shuffle(inBox).stream().limit(drawn).toList();
    offered.forEach(token -> resolution.revealTo(owner, token));
    resolution.decide(owner, offered.stream().<Option>map(ComponentOption::new).toList());
  }

  @Override
  public void resume(Resolution resolution, PlayerId owner, Option answer) {
    ComponentId token = ((ComponentOption) answer).component();
    resolution.move(token, Spots.city(owner));
    resolution.queueEffects(token, owner);
  }
}
