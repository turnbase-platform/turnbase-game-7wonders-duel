package io.turnbase.games.sevenwondersduel.view;

import io.turnbase.games.sevenwondersduel.agenda.ComponentOption;
import io.turnbase.games.sevenwondersduel.agenda.DecisionStep;
import io.turnbase.games.sevenwondersduel.agenda.Option;
import io.turnbase.games.sevenwondersduel.agenda.PlayerOption;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.cost.CostSolver;
import io.turnbase.games.sevenwondersduel.cost.Quote;
import io.turnbase.games.sevenwondersduel.id.BackId;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.id.ZoneId;
import io.turnbase.games.sevenwondersduel.scoring.Scoring;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.state.Phase;
import io.turnbase.games.sevenwondersduel.structure.Structure;
import io.turnbase.games.sevenwondersduel.turn.Construct;
import io.turnbase.games.sevenwondersduel.zone.Location;
import io.turnbase.games.sevenwondersduel.zone.Placement;
import io.turnbase.games.sevenwondersduel.zone.Placements;
import io.turnbase.games.sevenwondersduel.zone.Visibility;
import io.turnbase.games.sevenwondersduel.zone.Zones;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/** What each viewer may see of a game (base.md §10.8), derived from the state alone. */
public final class Views {

  private static final Comparator<Location> PLACE_ORDER =
      Comparator.comparing((Location l) -> l.zone().value()).thenComparing(Location::key);

  private record HiddenKey(Location at, String back) {}

  private static final Comparator<HiddenKey> HIDDEN_ORDER =
      Comparator.comparing(HiddenKey::at, PLACE_ORDER)
          .thenComparing(HiddenKey::back, Comparator.nullsFirst(Comparator.naturalOrder()));

  private static final Comparator<PlayerId> PLAYER_ORDER = Comparator.comparing(PlayerId::value);

  private Views() {}

  /** The game as {@code viewer} may see it. */
  public static DuelView of(DuelState state, RuleSet rules, Viewer viewer) {
    List<PlacedView> visible = new ArrayList<>();
    Map<HiddenKey, Integer> hidden = new TreeMap<>(HIDDEN_ORDER);
    for (Placement placement : state.placements()) {
      if (canSee(state, rules, viewer, placement.component())) {
        Location at = placement.at();
        visible.add(new PlacedView(placement.component().value(), at.zone().value(), at.key()));
      } else {
        hidden.merge(new HiddenKey(placement.at(), backOf(rules, placement)), 1, Integer::sum);
      }
    }
    List<HiddenView> collapsed =
        hidden.entrySet().stream()
            .map(
                e ->
                    new HiddenView(
                        e.getKey().at().zone().value(),
                        e.getKey().at().key(),
                        e.getKey().back(),
                        e.getValue()))
            .toList();
    return new DuelView(
        state.phase().name(),
        state.active().value(),
        state.age(),
        state.first().value(),
        state.deciding() == null ? null : state.deciding().value(),
        players(state),
        state.pawn().side() == null ? null : state.pawn().side().value(),
        state.pawn().space(),
        state.victory() == null || state.victory().winner() == null
            ? null
            : state.victory().winner().value(),
        state.victory() == null ? null : state.victory().reason().name(),
        scores(state, rules),
        visible,
        collapsed,
        pending(state, rules, viewer),
        quotes(state, rules, viewer));
  }

  /**
   * A face-down structure card shows its back (B-BACK-1), and so does a card tucked under a Wonder
   * (B-BACK-3); elsewhere nothing is shown.
   */
  private static String backOf(RuleSet rules, Placement placement) {
    ZoneId zone = placement.at().zone();
    if (!zone.equals(Structure.DOWN) && !zone.equals(Zones.UNDER_WONDER)) {
      return null;
    }
    return rules.catalog().get(placement.component()).back().map(BackId::value).orElse(null);
  }

  /** Public once the game is over: the whole board is in view then. */
  private static List<ScoreView> scores(DuelState state, RuleSet rules) {
    if (state.victory() == null) {
      return List.of();
    }
    return state.players().keySet().stream()
        .sorted(PLAYER_ORDER)
        .map(id -> Scoring.score(state, rules, id))
        .map(
            card ->
                new ScoreView(
                    card.player().value(),
                    card.lines().entrySet().stream()
                        .map(e -> new ScoreLineView(e.getKey(), e.getValue()))
                        .toList(),
                    card.total()))
        .toList();
  }

  private static List<PlayerView> players(DuelState state) {
    return state.players().entrySet().stream()
        .map(
            e -> new PlayerView(e.getKey().value(), e.getValue().coins(), e.getValue().playAgain()))
        .sorted(Comparator.comparing(PlayerView::id))
        .toList();
  }

  /**
   * Both players are quoted every structure card the viewer can see, covered or not, and their own
   * unbuilt Wonders (B-COST-1..5). Only the takeable cards are marked so.
   */
  private static List<QuoteView> quotes(DuelState state, RuleSet rules, Viewer viewer) {
    if (state.phase() != Phase.AGE) {
      return List.of();
    }
    CostSolver solver = new CostSolver(rules);
    java.util.Set<ComponentId> takeable =
        new java.util.HashSet<>(Structure.takeable(state.placements()));
    List<ComponentId> cards =
        state.placements().stream()
            .filter(p -> p.at().zone().equals(Structure.UP) || p.at().zone().equals(Structure.DOWN))
            .map(Placement::component)
            .filter(c -> canSee(state, rules, viewer, c))
            .toList();
    List<QuoteView> quotes = new ArrayList<>();
    for (PlayerId player : state.players().keySet().stream().sorted(PLAYER_ORDER).toList()) {
      List<ComponentId> targets = new ArrayList<>(cards);
      targets.addAll(Placements.idsIn(state.placements(), Construct.unbuilt(player)));
      for (ComponentId target : targets) {
        Quote quote = solver.quote(state, player, target);
        quotes.add(
            new QuoteView(
                player.value(),
                target.value(),
                quote.coins(),
                quote.tradeCost(),
                quote.total(),
                quote.chained(),
                quote.payableWith(state.player(player).coins()),
                takeable.contains(target),
                quote.lines().stream()
                    .map(
                        l ->
                            new QuoteView.LineView(
                                l.resource().id(),
                                l.needed(),
                                l.produced(),
                                l.discounted(),
                                l.bought(),
                                l.unitPrice()))
                    .toList()));
      }
    }
    return quotes;
  }

  private static PendingView pending(DuelState state, RuleSet rules, Viewer viewer) {
    if (state.agenda().isEmpty() || !(state.agenda().top() instanceof DecisionStep decision)) {
      return null;
    }
    boolean decides =
        viewer instanceof Viewer.Seat seat && seat.player().equals(decision.decider());
    List<String> options = new ArrayList<>();
    int hidden = 0;
    for (Option option : decision.options()) {
      switch (option) {
        case ComponentOption c when decides || canSee(state, rules, viewer, c.component()) ->
            options.add(c.component().value());
        case ComponentOption c -> hidden++;
        case PlayerOption p -> options.add(p.player().value().toString());
      }
    }
    return new PendingView(decision.decider().value(), options, hidden);
  }

  /**
   * Whether {@code viewer} may know which component {@code component} is: it lies in a public zone,
   * or in an owner zone of the viewer, or the viewer has seen it before.
   */
  public static boolean canSee(
      DuelState state, RuleSet rules, Viewer viewer, ComponentId component) {
    Location at = Placements.locationOf(state.placements(), component);
    Visibility visibility = rules.zones().kind(at.zone()).visibility();
    if (visibility == Visibility.PUBLIC) {
      return true;
    }
    if (!(viewer instanceof Viewer.Seat seat)) {
      return false;
    }
    if (visibility == Visibility.OWNER && at.key().equals(seat.player().value().toString())) {
      return true;
    }
    return state.knowledge().getOrDefault(seat.player(), Set.of()).contains(component);
  }
}
