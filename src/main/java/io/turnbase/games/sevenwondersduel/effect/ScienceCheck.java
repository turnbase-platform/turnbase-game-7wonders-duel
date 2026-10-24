package io.turnbase.games.sevenwondersduel.effect;

import io.turnbase.games.sevenwondersduel.agenda.ComponentOption;
import io.turnbase.games.sevenwondersduel.agenda.Option;
import io.turnbase.games.sevenwondersduel.catalog.Tag;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.id.ZoneId;
import io.turnbase.games.sevenwondersduel.science.Science;
import io.turnbase.games.sevenwondersduel.science.ScienceSymbol;
import io.turnbase.games.sevenwondersduel.science.Symbol;
import io.turnbase.games.sevenwondersduel.state.Victory;
import io.turnbase.games.sevenwondersduel.zone.Location;
import io.turnbase.games.sevenwondersduel.zone.Placements;
import io.turnbase.games.sevenwondersduel.zone.Zones;
import java.util.List;
import java.util.Map;

/**
 * Carried by a source that shows scientific symbols (a green card, the Law token): once the source
 * is in the owner's city, six different symbols win the game (B-SCI-3), and otherwise the first
 * pair of a symbol lets the owner take a progress token from the board (B-SCI-1).
 */
public record ScienceCheck() implements Effect {

  private static final ZoneId BOARD = Zones.BOARD;
  private static final ZoneId CITY = Zones.CITY;

  @Override
  public void apply(Resolution resolution, PlayerId owner) {
    ComponentId source = resolution.source().orElseThrow();
    Map<ScienceSymbol, Integer> counts =
        Science.counts(resolution.state(), resolution.rules(), owner);
    if (counts.size() >= Science.SUPREMACY) {
      resolution.declareVictory(owner, Victory.Reason.SCIENCE);
      return;
    }
    long pairs =
        resolution.rules().catalog().get(source).modifiers().stream()
            .filter(Symbol.class::isInstance)
            .map(m -> ((Symbol) m).symbol())
            .filter(symbol -> counts.get(symbol) == 2)
            .count();
    if (pairs > 0) {
      resolution.decide(owner, tokensOnBoard(resolution));
    }
  }

  @Override
  public void resume(Resolution resolution, PlayerId owner, Option answer) {
    ComponentId token = ((ComponentOption) answer).component();
    resolution.move(token, new Location(CITY, owner.value().toString()));
    resolution.queueEffects(token, owner);
  }

  private static List<Option> tokensOnBoard(Resolution resolution) {
    return Placements.idsIn(resolution.state().placements(), BOARD).stream()
        .filter(id -> resolution.rules().catalog().get(id).tags().contains(Tag.PROGRESS))
        .<Option>map(ComponentOption::new)
        .toList();
  }
}
