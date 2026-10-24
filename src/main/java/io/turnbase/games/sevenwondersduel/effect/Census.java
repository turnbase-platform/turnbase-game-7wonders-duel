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

  /** What this census counts, for a client to show; opaque unless a census describes itself. */
  default Counted counted() {
    return Counted.OPAQUE;
  }

  /** The owner's live components carrying {@code tag}. */
  static Census of(Tag tag) {
    return new OfTag(tag);
  }

  /** The owner's treasury. */
  static Census coins() {
    return new Coins();
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

  /**
   * What a census counts: the labels of what is counted, how many make one unit, and whether the
   * larger of both cities counts.
   */
  record Counted(List<String> of, int per, boolean larger) {
    static final Counted OPAQUE = new Counted(List.of(), 1, false);

    public Counted {
      of = List.copyOf(of);
    }

    /** The badge of a mechanic that pays {@code amount} for each unit counted. */
    public Badge badge(String kind, int amount) {
      return new Badge(kind, amount, of, per, larger);
    }
  }

  /** The owner's treasury; a record so that equal cards compare equal. */
  record Coins() implements Census {
    @Override
    public int count(DuelState state, RuleSet rules, PlayerId owner) {
      return state.player(owner).coins();
    }

    @Override
    public Counted counted() {
      return new Counted(List.of("coins"), 1, false);
    }
  }

  /** Counts by tag; a record so that equal cards compare equal. */
  record OfTag(Tag tag) implements Census {
    @Override
    public int count(DuelState state, RuleSet rules, PlayerId owner) {
      return Ownership.count(state, rules, owner, tag);
    }

    @Override
    public Counted counted() {
      return new Counted(List.of(tag.value()), 1, false);
    }
  }

  /** Adds counts; a record so that equal cards compare equal. */
  record Sum(List<Census> parts) implements Census {
    @Override
    public int count(DuelState state, RuleSet rules, PlayerId owner) {
      return parts.stream().mapToInt(part -> part.count(state, rules, owner)).sum();
    }

    @Override
    public Counted counted() {
      return new Counted(
          parts.stream().flatMap(part -> part.counted().of().stream()).toList(),
          1,
          parts.stream().anyMatch(part -> part.counted().larger()));
    }
  }

  /** Takes the maximum over both cities. */
  record LargerOfBoth(Census inner) implements Census {
    @Override
    public int count(DuelState state, RuleSet rules, PlayerId owner) {
      return Math.max(
          inner.count(state, rules, owner), inner.count(state, rules, state.opponentOf(owner)));
    }

    @Override
    public Counted counted() {
      return new Counted(inner.counted().of(), inner.counted().per(), true);
    }
  }

  /** Counts whole groups. */
  record Every(int size, Census inner) implements Census {
    @Override
    public int count(DuelState state, RuleSet rules, PlayerId owner) {
      return inner.count(state, rules, owner) / size;
    }

    @Override
    public Counted counted() {
      Counted counted = inner.counted();
      return new Counted(counted.of(), counted.per() * size, counted.larger());
    }
  }
}
