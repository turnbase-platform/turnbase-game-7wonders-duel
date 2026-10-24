package io.turnbase.games.sevenwondersduel.turn;

import io.turnbase.games.sevenwondersduel.agenda.TurnEndStep;
import io.turnbase.games.sevenwondersduel.cost.Quote;
import io.turnbase.games.sevenwondersduel.effect.Resolution;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import java.util.List;

/**
 * The paid actions of a turn. Each pays the bank, moves what is built, then settles: the effects
 * run and the turn ends once the last decision they open is answered (B-TURN-3).
 */
public final class Construct {

  private Construct() {}

  /** The actor builds {@code card} for {@code quote}. The caller has checked legality and cost. */
  public static void building(
      Resolution resolution, PlayerId actor, ComponentId card, Quote quote) {
    resolution.changeCoins(actor, -quote.total());
    resolution.move(card, Places.city(actor));
    resolution.queue(List.of(new TurnEndStep()));
    resolution.queueEffects(card, actor);
    Turn.settle(resolution);
  }
}
