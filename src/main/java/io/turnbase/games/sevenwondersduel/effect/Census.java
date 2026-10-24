package io.turnbase.games.sevenwondersduel.effect;

import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.catalog.Tag;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import java.util.List;

/** How many of something an owner has, for effects that pay per card or per Wonder. */
@FunctionalInterface
public interface Census {

  int count(DuelState state, RuleSet rules, PlayerId owner);

  /** The owner's live components carrying {@code tag}. */
  static Census of(Tag tag) {
    return new OfTag(tag);
  }

  /** The owner's treasury. */
  static Census coins() {
    return (state, rules, owner) -> state.player(owner).coins();
  }

  /** The counts of all {@code parts} added together, within one city. */
  static Census sum(Census... parts) {
    return new Sum(List.of(parts));
  }

  /** The larger of the owner's and the opponent's count: "the city with the most" (B-GUILD-1). */
  static Census largerOfBoth(Census inner) {
    return new LargerOfBoth(inner);
  }

  /** Whole groups of {@code size} in {@code inner}, rounded down. */
  static Census every(int size, Census inner) {
    if (size <= 0) {
      throw new IllegalArgumentException("size must be positive: " + size);
    }
    return new Every(size, inner);
  }

  /** Counts by tag; a record so that equal cards compare equal. */
  record OfTag(Tag tag) implements Census {
    @Override
    public int count(DuelState state, RuleSet rules, PlayerId owner) {
      return Ownership.count(state, rules, owner, tag);
    }
  }

  /** Adds counts; a record so that equal cards compare equal. */
  record Sum(List<Census> parts) implements Census {
    @Override
    public int count(DuelState state, RuleSet rules, PlayerId owner) {
      return parts.stream().mapToInt(part -> part.count(state, rules, owner)).sum();
    }
  }

  /** Takes the maximum over both cities. */
  record LargerOfBoth(Census inner) implements Census {
    @Override
    public int count(DuelState state, RuleSet rules, PlayerId owner) {
      return Math.max(
          inner.count(state, rules, owner), inner.count(state, rules, state.opponentOf(owner)));
    }
  }

  /** Counts whole groups. */
  record Every(int size, Census inner) implements Census {
    @Override
    public int count(DuelState state, RuleSet rules, PlayerId owner) {
      return inner.count(state, rules, owner) / size;
    }
  }
}
