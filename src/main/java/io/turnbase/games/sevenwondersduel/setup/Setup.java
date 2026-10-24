package io.turnbase.games.sevenwondersduel.setup;

import io.turnbase.games.sevenwondersduel.agenda.Agenda;
import io.turnbase.games.sevenwondersduel.catalog.Component;
import io.turnbase.games.sevenwondersduel.catalog.ComponentType;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.catalog.SetupParams;
import io.turnbase.games.sevenwondersduel.catalog.Tag;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.id.ZoneId;
import io.turnbase.games.sevenwondersduel.military.MilitaryTrack;
import io.turnbase.games.sevenwondersduel.rng.Rng;
import io.turnbase.games.sevenwondersduel.rng.Rng.Drawn;
import io.turnbase.games.sevenwondersduel.rng.RngCursor;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.state.Pawn;
import io.turnbase.games.sevenwondersduel.state.Phase;
import io.turnbase.games.sevenwondersduel.state.PlayerState;
import io.turnbase.games.sevenwondersduel.structure.Structure;
import io.turnbase.games.sevenwondersduel.zone.Location;
import io.turnbase.games.sevenwondersduel.zone.Placement;
import io.turnbase.games.sevenwondersduel.zone.Placements;
import io.turnbase.games.sevenwondersduel.zone.Zones;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Builds the starting state of a game from the rule set and a seed (base.md §5.1, B-SET-3/4/6 and
 * B-WND-1). Content is found by {@link Tag} and {@link ComponentType}; one seeded stream feeds
 * every shuffle, so a seed fixes the whole setup.
 */
public final class Setup {

  private static final ZoneId DECK = Zones.DECK;
  private static final ZoneId BOARD = Zones.BOARD;
  private static final ZoneId BOX = Zones.BOX;
  private static final ZoneId DRAFT = Zones.DRAFT;

  /** The shuffled Wonders still to be revealed to the draft. */
  public static final Location WONDER_POOL = new Location(DECK, "wonders");

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
    SetupParams params = rules.setup();
    Table table = new Table(rules, RngCursor.start(seed));
    PlayerId first = table.pick(List.of(a, b));

    List<ComponentId> progress = ids(rules.catalog().tagged(Tag.PROGRESS));
    require(
        progress.size() >= params.progressOnBoard(),
        "progress tokens",
        params.progressOnBoard(),
        progress);
    List<ComponentId> shuffledProgress = table.shuffle(progress);
    table.placeAll(shuffledProgress.subList(0, params.progressOnBoard()), new Location(BOARD, ""));
    table.placeAll(
        shuffledProgress.subList(params.progressOnBoard(), progress.size()), new Location(BOX, ""));

    placeMilitaryTokens(table, rules, List.of(a, b));

    List<ComponentId> guilds = ids(rules.catalog().tagged(Tag.GUILD));
    require(guilds.size() >= params.guildsInPlay(), "guilds", params.guildsInPlay(), guilds);
    List<ComponentId> shuffledGuilds = table.shuffle(guilds);
    List<Integer> ages = rules.ages();
    int lastAge = ages.get(ages.size() - 1);
    for (int age : ages) {
      buildAgeDeck(table, rules, params, age, age == lastAge ? shuffledGuilds : List.of());
    }
    table.placeAll(
        shuffledGuilds.subList(params.guildsInPlay(), guilds.size()), new Location(BOX, ""));

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
            a,
            new PlayerState(params.startingCoins(), false),
            b,
            new PlayerState(params.startingCoins(), false)),
        Map.of(a, Set.of(), b, Set.of()),
        table.cursor(),
        rules.packIds(),
        Agenda.EMPTY,
        Pawn.CENTRE,
        null);
  }

  /**
   * B-SET-6: removes {@code removedPerAge} random cards to the box, adds the drawn Guilds (the
   * first {@code guildsInPlay} of {@code guilds}, if any) and shuffles the result into the deck.
   */
  private static void buildAgeDeck(
      Table table, RuleSet rules, SetupParams params, int age, List<ComponentId> guilds) {
    List<ComponentId> cards =
        ids(
            rules.catalog().tagged(Tag.age(age)).stream()
                .filter(c -> !c.tags().contains(Tag.GUILD))
                .toList());
    int expected = rules.layoutForAge(age).size();
    int added = guilds.isEmpty() ? 0 : params.guildsInPlay();
    if (cards.size() - params.removedPerAge() + added != expected) {
      throw new IllegalArgumentException(
          "age "
              + age
              + " needs "
              + (expected - added + params.removedPerAge())
              + " cards, found "
              + cards.size());
    }
    Structure.requireBacks(rules, cards);
    Structure.requireBacks(rules, guilds.subList(0, added));
    List<ComponentId> shuffled = table.shuffle(cards);
    table.placeAll(shuffled.subList(0, params.removedPerAge()), new Location(BOX, ""));
    List<ComponentId> deck =
        new ArrayList<>(shuffled.subList(params.removedPerAge(), shuffled.size()));
    deck.addAll(guilds.subList(0, added));
    table.placeAll(table.shuffle(deck), ageDeck(age));
  }

  /**
   * B-SET-2: the military tokens of each zone lie face up on that zone, each on the side of the
   * seat it is tagged for ({@link MilitaryTrack#seatTag}); {@code sides} lists the seats in order.
   */
  private static void placeMilitaryTokens(Table table, RuleSet rules, List<PlayerId> sides) {
    for (int zone : MilitaryTrack.TOKEN_ZONES) {
      Map<Integer, Component> bySeat = new TreeMap<>();
      for (Component token : rules.catalog().tagged(MilitaryTrack.zoneTag(zone))) {
        int seat = seatOf(token, sides.size());
        if (bySeat.putIfAbsent(seat, token) != null) {
          throw new IllegalArgumentException(
              "zone " + zone + " holds one military token per seat, found two for seat " + seat);
        }
      }
      bySeat.forEach(
          (seat, token) ->
              table.placeAll(List.of(token.id()), MilitaryTrack.tokenSpot(sides.get(seat), zone)));
    }
  }

  private static int seatOf(Component token, int seats) {
    for (int seat = 0; seat < seats; seat++) {
      if (token.tags().contains(MilitaryTrack.seatTag(seat))) {
        return seat;
      }
    }
    throw new IllegalArgumentException(
        "military token '" + token.id().value() + "' is not tagged for a seat of " + seats);
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
