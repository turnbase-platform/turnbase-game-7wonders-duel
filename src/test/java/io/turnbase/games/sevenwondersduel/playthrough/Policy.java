package io.turnbase.games.sevenwondersduel.playthrough;

import io.turnbase.games.sevenwondersduel.agenda.ComponentOption;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.content.Colors;
import io.turnbase.games.sevenwondersduel.engine.Answer;
import io.turnbase.games.sevenwondersduel.engine.Build;
import io.turnbase.games.sevenwondersduel.engine.PlayerAction;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.science.Science;
import io.turnbase.games.sevenwondersduel.science.Symbol;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;

/** How a bot chooses among legal actions: at random, or leaning towards one aim. */
public enum Policy {
  RANDOM,
  /** Builds and takes the components that add a science symbol the player does not hold. */
  SCIENTIST,
  /** Builds military (red) cards. */
  WARMONGER;

  private static final double LEAN = 0.9;

  PlayerAction pick(RuleSet rules, DuelState state, Bot.Move move, Random random) {
    Predicate<PlayerAction> liked = liking(rules, state, move);
    if (random.nextDouble() < LEAN) {
      List<PlayerAction> preferred = move.actions().stream().filter(liked).toList();
      if (!preferred.isEmpty()) {
        return preferred.get(random.nextInt(preferred.size()));
      }
    }
    return move.actions().get(random.nextInt(move.actions().size()));
  }

  private Predicate<PlayerAction> liking(RuleSet rules, DuelState state, Bot.Move move) {
    return switch (this) {
      case RANDOM -> action -> false;
      case WARMONGER ->
          action ->
              action instanceof Build build
                  && rules.catalog().get(build.card()).tags().contains(Colors.RED);
      case SCIENTIST ->
          action -> component(action).filter(id -> addsSymbol(rules, state, move, id)).isPresent();
    };
  }

  private static Optional<ComponentId> component(PlayerAction action) {
    if (action instanceof Build build) {
      return Optional.of(build.card());
    }
    if (action instanceof Answer answer && answer.choice() instanceof ComponentOption option) {
      return Optional.of(option.component());
    }
    return Optional.empty();
  }

  private static boolean addsSymbol(RuleSet rules, DuelState state, Bot.Move move, ComponentId id) {
    var held = Science.counts(state, rules, move.actor()).keySet();
    return rules.catalog().get(id).modifiers().stream()
        .anyMatch(m -> m instanceof Symbol s && !held.contains(s.symbol()));
  }
}
