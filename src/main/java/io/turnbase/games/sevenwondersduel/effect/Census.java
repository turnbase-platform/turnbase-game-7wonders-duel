package io.turnbase.games.sevenwondersduel.effect;

import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.catalog.Tag;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.state.DuelState;

/** How many of something an owner has, for effects that pay per card or per Wonder. */
@FunctionalInterface
public interface Census {

  int count(DuelState state, RuleSet rules, PlayerId owner);

  /** The owner's live components carrying {@code tag}. */
  static Census of(Tag tag) {
    return new OfTag(tag);
  }

  /** Counts by tag; a record so that equal cards compare equal. */
  record OfTag(Tag tag) implements Census {
    @Override
    public int count(DuelState state, RuleSet rules, PlayerId owner) {
      return Ownership.count(state, rules, owner, tag);
    }
  }
}
