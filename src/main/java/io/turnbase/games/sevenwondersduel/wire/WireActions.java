package io.turnbase.games.sevenwondersduel.wire;

import io.turnbase.games.sevenwondersduel.agenda.ComponentOption;
import io.turnbase.games.sevenwondersduel.agenda.PlayerOption;
import io.turnbase.games.sevenwondersduel.engine.Answer;
import io.turnbase.games.sevenwondersduel.engine.Build;
import io.turnbase.games.sevenwondersduel.engine.BuildWonder;
import io.turnbase.games.sevenwondersduel.engine.ChooseStarter;
import io.turnbase.games.sevenwondersduel.engine.Discard;
import io.turnbase.games.sevenwondersduel.engine.DraftWonder;
import io.turnbase.games.sevenwondersduel.engine.PlayerAction;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.gdk.context.ActionDecodeException;

/** Turns what a client sent into an engine action, or says why it cannot. */
public final class WireActions {

  private WireActions() {}

  /**
   * Decodes {@code wire}, which must carry exactly the fields its type uses. Only the fields of
   * {@link WireAction} are checked: the platform's JSON mapper ignores unknown properties, so a
   * payload with extra keys reaches this method without them.
   *
   * @throws ActionDecodeException if the type is unknown or the fields do not fit it
   */
  public static PlayerAction decode(WireAction wire) {
    if (wire == null || wire.type() == null) {
      throw new ActionDecodeException("no action type given");
    }
    String type = wire.type();
    return switch (type) {
      case "draftWonder" -> new DraftWonder(card(wire));
      case "discard" -> new Discard(card(wire));
      case "build" -> new Build(card(wire));
      case "answerComponent" -> new Answer(new ComponentOption(card(wire)));
      case "buildWonder" -> {
        takes(wire, true, true, false);
        yield new BuildWonder(id(wire, wire.component()), id(wire, wire.wonder()));
      }
      case "chooseStarter" -> new ChooseStarter(named(wire));
      case "answerPlayer" -> new Answer(new PlayerOption(named(wire)));
      default -> throw new ActionDecodeException("unknown action type '" + type + "'");
    };
  }

  /** The fields the type uses, to be checked against what was sent. */
  private record Fields(boolean component, boolean wonder, boolean player) {}

  private static void takes(WireAction wire, boolean component, boolean wonder, boolean player) {
    Fields wanted = new Fields(component, wonder, player);
    Fields sent =
        new Fields(wire.component() != null, wire.wonder() != null, wire.player() != null);
    if (!wanted.equals(sent)) {
      throw new ActionDecodeException(
          "action '" + wire.type() + "' takes " + describe(wanted) + " but got " + describe(sent));
    }
  }

  private static String describe(Fields fields) {
    StringBuilder out = new StringBuilder();
    if (fields.component()) {
      out.append("component ");
    }
    if (fields.wonder()) {
      out.append("wonder ");
    }
    if (fields.player()) {
      out.append("player ");
    }
    return out.length() == 0 ? "no fields" : out.toString().trim();
  }

  private static ComponentId card(WireAction wire) {
    takes(wire, true, false, false);
    return id(wire, wire.component());
  }

  private static PlayerId named(WireAction wire) {
    takes(wire, false, false, true);
    return PlayerId.of(wire.player());
  }

  private static ComponentId id(WireAction wire, String value) {
    try {
      return ComponentId.of(value);
    } catch (IllegalArgumentException e) {
      throw new ActionDecodeException("action '" + wire.type() + "' names an invalid id", e);
    }
  }
}
