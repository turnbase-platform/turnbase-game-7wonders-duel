package io.turnbase.games.sevenwondersduel.playthrough;

import io.turnbase.games.sevenwondersduel.agenda.ComponentOption;
import io.turnbase.games.sevenwondersduel.agenda.PlayerOption;
import io.turnbase.games.sevenwondersduel.engine.Answer;
import io.turnbase.games.sevenwondersduel.engine.Build;
import io.turnbase.games.sevenwondersduel.engine.BuildWonder;
import io.turnbase.games.sevenwondersduel.engine.ChooseStarter;
import io.turnbase.games.sevenwondersduel.engine.Discard;
import io.turnbase.games.sevenwondersduel.engine.DraftWonder;
import io.turnbase.games.sevenwondersduel.engine.PlayerAction;
import io.turnbase.games.sevenwondersduel.wire.WireAction;

/** What a client would send for an engine action. */
final class Moves {

  private Moves() {}

  static WireAction wire(PlayerAction action) {
    return switch (action) {
      case DraftWonder a -> new WireAction("draftWonder", a.wonder().value(), null, null);
      case ChooseStarter a -> new WireAction("chooseStarter", null, null, a.starter().value());
      case Discard a -> new WireAction("discard", a.card().value(), null, null);
      case Build a -> new WireAction("build", a.card().value(), null, null);
      case BuildWonder a ->
          new WireAction("buildWonder", a.card().value(), a.wonder().value(), null);
      case Answer a when a.choice() instanceof ComponentOption o ->
          new WireAction("answerComponent", o.component().value(), null, null);
      case Answer a when a.choice() instanceof PlayerOption o ->
          new WireAction("answerPlayer", null, null, o.player().value());
      default -> throw new AssertionError("no wire form for " + action);
    };
  }
}
