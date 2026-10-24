package io.turnbase.games.sevenwondersduel.turn;

import io.turnbase.games.sevenwondersduel.agenda.TurnEndStep;
import io.turnbase.games.sevenwondersduel.cost.Quote;
import io.turnbase.games.sevenwondersduel.effect.Resolution;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.zone.Location;
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

  /**
   * The actor builds {@code wonder} for {@code quote}, tucking {@code card} under it. The seventh
   * Wonder destroys the unbuilt ones before its own effects run (B-ACT-5).
   */
  public static void wonder(
      Resolution resolution, PlayerId actor, ComponentId card, ComponentId wonder, Quote quote) {
    resolution.changeCoins(actor, -quote.total());
    resolution.move(card, Places.underWonder(wonder));
    resolution.move(wonder, Places.built(actor));
    WonderLimit.enforce(resolution);
    resolution.queue(List.of(new TurnEndStep()));
    resolution.queueEffects(wonder, actor);
    Turn.settle(resolution);
  }

  /** Whether another Wonder may still be built. */
  public static boolean wonderLimitAllows(DuelState state) {
    return WonderLimit.allows(state);
  }

  /** Where {@code player}'s unbuilt Wonders lie. */
  public static Location unbuilt(PlayerId player) {
    return Places.unbuilt(player);
  }
}
