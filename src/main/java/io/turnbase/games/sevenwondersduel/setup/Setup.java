package io.turnbase.games.sevenwondersduel.setup;

import io.turnbase.games.sevenwondersduel.agenda.Agenda;
import io.turnbase.games.sevenwondersduel.catalog.Component;
import io.turnbase.games.sevenwondersduel.catalog.ComponentType;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.catalog.Tag;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.id.ZoneId;
import io.turnbase.games.sevenwondersduel.rng.Rng;
import io.turnbase.games.sevenwondersduel.rng.Rng.Drawn;
import io.turnbase.games.sevenwondersduel.rng.RngCursor;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.state.Pawn;
import io.turnbase.games.sevenwondersduel.state.Phase;
import io.turnbase.games.sevenwondersduel.state.PlayerState;
import io.turnbase.games.sevenwondersduel.zone.Location;
import io.turnbase.games.sevenwondersduel.zone.Placement;
import io.turnbase.games.sevenwondersduel.zone.Placements;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Builds the starting state of a game from the rule set and a seed (base.md §5.1, B-SET-3/4/6 and
 * B-WND-1). Content is found by {@link Tag} and {@link ComponentType}; one seeded stream feeds
 * every shuffle, so a seed fixes the whole setup.
 */
public final class Setup {

  private static final ZoneId DECK = ZoneId.of("deck");
  private static final ZoneId BOARD = ZoneId.of("board");
  private static final ZoneId BOX = ZoneId.of("box");
  private static final ZoneId DRAFT = ZoneId.of("draft");

  /** The shuffled Wonders still to be revealed to the draft. */
  public static final Location WONDER_POOL = new Location(DECK, "wonders");

  static final int STARTING_COINS = 7;
  static final int PROGRESS_ON_BOARD = 5;
  static final int REMOVED_PER_AGE = 3;
  static final int GUILDS_IN_PLAY = 3;

  /** Wonders the draft takes from the pool; whatever stays in the pool goes to the box. */
  public static final int DRAFT_WONDERS = 8;

  /** Wonders revealed to the draft at a time. */
  public static final int DRAFT_ROUND = 4;

  private Setup() {}

  /** The deck of Age {@code age}, dealt into the structure when the Age starts. */
  public static Location ageDeck(int age) {
    return new Location(DECK, String.valueOf(age));
  }

  public static DuelState newGame(RuleSet rules, long seed, PlayerId a, PlayerId b) {
    if (a.equals(b)) {
      throw new IllegalArgumentException("a duel needs two different players");
    }
    Table table = new Table(rules, RngCursor.start(seed));
    PlayerId first = table.pick(List.of(a, b));

    List<ComponentId> progress = ids(rules.catalog().tagged(Tag.PROGRESS));
    require(progress.size() >= PROGRESS_ON_BOARD, "progress tokens", PROGRESS_ON_BOARD, progress);
    List<ComponentId> shuffledProgress = table.shuffle(progress);
    table.placeAll(shuffledProgress.subList(0, PROGRESS_ON_BOARD), new Location(BOARD, ""));
    table.placeAll(
        shuffledProgress.subList(PROGRESS_ON_BOARD, progress.size()), new Location(BOX, ""));

    List<ComponentId> guilds = ids(rules.catalog().tagged(Tag.GUILD));
    require(guilds.size() >= GUILDS_IN_PLAY, "guilds", GUILDS_IN_PLAY, guilds);
    List<ComponentId> shuffledGuilds = table.shuffle(guilds);
    List<Integer> ages = rules.ages();
    int lastAge = ages.get(ages.size() - 1);
    for (int age : ages) {
      buildAgeDeck(table, rules, age, age == lastAge ? shuffledGuilds : List.of());
    }
    table.placeAll(shuffledGuilds.subList(GUILDS_IN_PLAY, guilds.size()), new Location(BOX, ""));

    List<ComponentId> wonders = ids(rules.catalog().ofType(ComponentType.WONDER));
    require(wonders.size() >= DRAFT_WONDERS, "wonders", DRAFT_WONDERS, wonders);
    List<ComponentId> shuffledWonders = table.shuffle(wonders);
    table.placeAll(shuffledWonders.subList(0, DRAFT_ROUND), new Location(DRAFT, ""));
    table.placeAll(shuffledWonders.subList(DRAFT_ROUND, wonders.size()), WONDER_POOL);

    return new DuelState(
        Phase.WONDER_DRAFT,
        first,
        0,
        first,
        null,
        table.placements(),
        Map.of(
            a, new PlayerState(STARTING_COINS, false), b, new PlayerState(STARTING_COINS, false)),
        Map.of(a, Set.of(), b, Set.of()),
        table.cursor(),
        rules.packIds(),
        Agenda.EMPTY,
        Pawn.CENTRE);
  }

  /**
   * B-SET-6: removes {@link #REMOVED_PER_AGE} random cards to the box, adds the drawn Guilds (the
   * first {@link #GUILDS_IN_PLAY} of {@code guilds}, if any) and shuffles the result into the deck.
   */
  private static void buildAgeDeck(Table table, RuleSet rules, int age, List<ComponentId> guilds) {
    List<ComponentId> cards =
        ids(
            rules.catalog().tagged(Tag.age(age)).stream()
                .filter(c -> !c.tags().contains(Tag.GUILD))
                .toList());
    int expected = rules.layoutForAge(age).size();
    int added = guilds.isEmpty() ? 0 : GUILDS_IN_PLAY;
    if (cards.size() - REMOVED_PER_AGE + added != expected) {
      throw new IllegalArgumentException(
          "age "
              + age
              + " needs "
              + (expected - added + REMOVED_PER_AGE)
              + " cards, found "
              + cards.size());
    }
    List<ComponentId> shuffled = table.shuffle(cards);
    table.placeAll(shuffled.subList(0, REMOVED_PER_AGE), new Location(BOX, ""));
    List<ComponentId> deck = new ArrayList<>(shuffled.subList(REMOVED_PER_AGE, shuffled.size()));
    deck.addAll(guilds.subList(0, added));
    table.placeAll(table.shuffle(deck), ageDeck(age));
  }

  private static List<ComponentId> ids(List<Component> components) {
    return components.stream().map(Component::id).toList();
  }

  private static void require(boolean fits, String what, int needed, List<ComponentId> found) {
    if (!fits) {
      throw new IllegalArgumentException(
          "setup needs at least " + needed + " " + what + ", found " + found.size());
    }
  }

  /** The placements and random cursor being built up. */
  private static final class Table {
    private final RuleSet rules;
    private List<Placement> placements = List.of();
    private RngCursor cursor;

    Table(RuleSet rules, RngCursor cursor) {
      this.rules = rules;
      this.cursor = cursor;
    }

    <T> T pick(List<T> items) {
      Drawn<Integer> drawn = Rng.nextInt(cursor, items.size());
      cursor = drawn.next();
      return items.get(drawn.value());
    }

    List<ComponentId> shuffle(List<ComponentId> items) {
      Drawn<List<ComponentId>> drawn = Rng.shuffle(items, cursor);
      cursor = drawn.next();
      return drawn.value();
    }

    void placeAll(List<ComponentId> ids, Location at) {
      for (ComponentId id : ids) {
        placements = Placements.place(placements, id, at, rules.zones());
      }
    }

    List<Placement> placements() {
      return placements;
    }

    RngCursor cursor() {
      return cursor;
    }
  }
}
