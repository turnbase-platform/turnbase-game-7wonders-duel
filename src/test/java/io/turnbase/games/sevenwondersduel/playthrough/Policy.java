package io.turnbase.games.sevenwondersduel.playthrough;

import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.catalog.Tag;
import io.turnbase.games.sevenwondersduel.content.Colors;
import io.turnbase.games.sevenwondersduel.engine.Build;
import io.turnbase.games.sevenwondersduel.engine.PlayerAction;
import java.util.List;
import java.util.Random;

/** How a bot chooses among legal actions: at random, or leaning towards one colour of card. */
public enum Policy {
  RANDOM(null),
  SCIENTIST(Colors.GREEN),
  WARMONGER(Colors.RED);

  private static final double LEAN = 0.8;

  private final Tag favourite;

  Policy(Tag favourite) {
    this.favourite = favourite;
  }

  /** Builds of the favourite colour win most of the time; otherwise any action, uniformly. */
  PlayerAction pick(RuleSet rules, List<PlayerAction> actions, Random random) {
    if (favourite != null && random.nextDouble() < LEAN) {
      List<PlayerAction> liked =
          actions.stream()
              .filter(Build.class::isInstance)
              .filter(a -> rules.catalog().get(((Build) a).card()).tags().contains(favourite))
              .toList();
      if (!liked.isEmpty()) {
        return liked.get(random.nextInt(liked.size()));
      }
    }
    return actions.get(random.nextInt(actions.size()));
  }
}
