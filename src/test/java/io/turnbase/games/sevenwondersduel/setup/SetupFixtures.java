package io.turnbase.games.sevenwondersduel.setup;

import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P1;
import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P2;

import io.turnbase.games.sevenwondersduel.catalog.BasePack;
import io.turnbase.games.sevenwondersduel.catalog.Component;
import io.turnbase.games.sevenwondersduel.catalog.ComponentType;
import io.turnbase.games.sevenwondersduel.catalog.ContentPack;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.catalog.Tag;
import io.turnbase.games.sevenwondersduel.draft.Draft;
import io.turnbase.games.sevenwondersduel.engine.ChooseStarter;
import io.turnbase.games.sevenwondersduel.engine.DraftWonder;
import io.turnbase.games.sevenwondersduel.engine.Engine;
import io.turnbase.games.sevenwondersduel.id.BackId;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PackId;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.zone.Placements;
import io.turnbase.games.sevenwondersduel.zone.ZoneKind;
import io.turnbase.games.sevenwondersduel.zone.Zones;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** A catalog sized like the base game: 23/23/20 Age cards, 7 Guilds, 12 Wonders, 10 tokens. */
public final class SetupFixtures {

  record Piece(ComponentId id, ComponentType type, Set<Tag> tags, Optional<BackId> back)
      implements Component {
    @Override
    public String name() {
      return id.value();
    }
  }

  /** The fixture back groups: one per Age and one for the Guilds. */
  public static final List<BackId> BACKS =
      List.of(BackId.of("age1"), BackId.of("age2"), BackId.of("age3"), BackId.of("guild"));

  static Piece piece(String id, ComponentType type, Tag... tags) {
    return new Piece(ComponentId.of(id), type, Set.of(tags), Optional.empty());
  }

  static Piece backed(Piece piece, String back) {
    return new Piece(piece.id(), piece.type(), piece.tags(), Optional.of(BackId.of(back)));
  }

  public static List<Component> pieces(
      int age1, int age2, int age3, int guilds, int wonders, int progress) {
    List<Component> all = new ArrayList<>();
    int[] perAge = {age1, age2, age3};
    for (int age = 1; age <= 3; age++) {
      for (int i = 0; i < perAge[age - 1]; i++) {
        all.add(backed(piece("a" + age + "-" + i, ComponentType.CARD, Tag.age(age)), "age" + age));
      }
    }
    for (int i = 0; i < guilds; i++) {
      all.add(backed(piece("guild-" + i, ComponentType.CARD, Tag.GUILD), "guild"));
    }
    for (int i = 0; i < wonders; i++) {
      all.add(piece("wonder-" + i, ComponentType.WONDER));
    }
    for (int i = 0; i < progress; i++) {
      all.add(piece("progress-" + i, ComponentType.TOKEN, Tag.PROGRESS));
    }
    return all;
  }

  public static RuleSet rules(List<Component> components) {
    ContentPack pack =
        new ContentPack() {
          @Override
          public PackId id() {
            return PackId.of("fixture");
          }

          @Override
          public List<ZoneKind> zoneKinds() {
            return List.of();
          }

          @Override
          public List<BackId> backs() {
            return BACKS;
          }

          @Override
          public List<Component> components() {
            return components;
          }
        };
    return RuleSet.assemble(List.of(new BasePack(), pack));
  }

  public static RuleSet baseSized() {
    return rules(pieces(23, 23, 20, 7, 12, 10));
  }

  /** A game in Age I with the starter choice made: the first player moves, nothing is pending. */
  public static DuelState startedAgeOne(RuleSet rules, long seed) {
    Engine engine = new Engine(rules);
    DuelState state = Setup.newGame(rules, seed, P1, P2);
    while (Draft.toMove(state).isPresent()) {
      PlayerId mover = Draft.toMove(state).get();
      ComponentId wonder = Placements.idsIn(state.placements(), Zones.DRAFT).get(0);
      state = engine.apply(state, mover, new DraftWonder(wonder)).state();
    }
    return engine.apply(state, state.deciding(), new ChooseStarter(state.deciding())).state();
  }

  private SetupFixtures() {}
}
