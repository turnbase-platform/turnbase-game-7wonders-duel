package io.turnbase.games.sevenwondersduel.view;

import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.event.ChainBuilt;
import io.turnbase.games.sevenwondersduel.event.CoinsChanged;
import io.turnbase.games.sevenwondersduel.event.ComponentMoved;
import io.turnbase.games.sevenwondersduel.event.EffectRef;
import io.turnbase.games.sevenwondersduel.event.EventRecord;
import io.turnbase.games.sevenwondersduel.event.PawnMoved;
import io.turnbase.games.sevenwondersduel.event.PlayAgainChanged;
import io.turnbase.games.sevenwondersduel.event.TradePaid;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.state.Pawn;
import io.turnbase.games.sevenwondersduel.zone.Location;
import java.util.List;

/**
 * What happened during one action, as each viewer may know it. A component is named exactly when
 * the viewer can identify it in {@code after}, the state the view shows; places are never hidden
 * because the view's hidden counts show them anyway.
 */
public final class EventViews {

  private EventViews() {}

  public static List<EventView> of(
      List<EventRecord> events, DuelState after, RuleSet rules, Viewer viewer) {
    return events.stream().map(record -> of(record, after, rules, viewer)).toList();
  }

  private static EventView of(EventRecord record, DuelState after, RuleSet rules, Viewer viewer) {
    String cause =
        record
            .source()
            .map(EffectRef::source)
            .map(source -> name(source, after, rules, viewer))
            .orElse(null);
    int id = record.id();
    int parent = record.parentId();
    return switch (record.event()) {
      case ComponentMoved e ->
          new EventView(
              id,
              parent,
              "moved",
              cause,
              name(e.component(), after, rules, viewer),
              place(e.from()),
              place(e.to()),
              null,
              0,
              null,
              null);
      case CoinsChanged e ->
          new EventView(
              id,
              parent,
              "coins",
              cause,
              null,
              null,
              null,
              e.player().value(),
              e.delta(),
              null,
              null);
      case TradePaid e ->
          new EventView(
              id,
              parent,
              "trade",
              cause,
              null,
              null,
              null,
              e.payer().value(),
              e.amount(),
              null,
              null);
      case ChainBuilt e ->
          new EventView(
              id,
              parent,
              "chain",
              cause,
              name(e.card(), after, rules, viewer),
              null,
              null,
              e.builder().value(),
              0,
              null,
              null);
      case PawnMoved e ->
          new EventView(
              id, parent, "pawn", cause, null, null, null, null, 0, pawn(e.from()), pawn(e.to()));
      case PlayAgainChanged e ->
          new EventView(
              id,
              parent,
              e.granted() ? "playAgainGranted" : "playAgainCleared",
              cause,
              null,
              null,
              null,
              e.player().value(),
              0,
              null,
              null);
    };
  }

  private static String name(ComponentId id, DuelState after, RuleSet rules, Viewer viewer) {
    return Views.canSee(after, rules, viewer, id) ? id.value() : null;
  }

  private static PlaceView place(Location at) {
    return new PlaceView(at.zone().value(), at.key());
  }

  private static PawnView pawn(Pawn pawn) {
    return new PawnView(pawn.side() == null ? null : pawn.side().value(), pawn.space());
  }
}
