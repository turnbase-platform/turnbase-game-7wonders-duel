package io.turnbase.games.sevenwondersduel.setup;

import io.turnbase.games.sevenwondersduel.catalog.BasePack;
import io.turnbase.games.sevenwondersduel.catalog.Component;
import io.turnbase.games.sevenwondersduel.catalog.ComponentType;
import io.turnbase.games.sevenwondersduel.catalog.ContentPack;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.catalog.Tag;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PackId;
import io.turnbase.games.sevenwondersduel.zone.ZoneKind;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** A catalog sized like the base game: 23/23/20 Age cards, 7 Guilds, 12 Wonders, 10 tokens. */
public final class SetupFixtures {

  record Piece(ComponentId id, ComponentType type, Set<Tag> tags) implements Component {
    @Override
    public String name() {
      return id.value();
    }
  }

  static Piece piece(String id, ComponentType type, Tag... tags) {
    return new Piece(ComponentId.of(id), type, Set.of(tags));
  }

  public static List<Component> pieces(
      int age1, int age2, int age3, int guilds, int wonders, int progress) {
    List<Component> all = new ArrayList<>();
    int[] perAge = {age1, age2, age3};
    for (int age = 1; age <= 3; age++) {
      for (int i = 0; i < perAge[age - 1]; i++) {
        all.add(piece("a" + age + "-" + i, ComponentType.CARD, Tag.age(age)));
      }
    }
    for (int i = 0; i < guilds; i++) {
      all.add(piece("guild-" + i, ComponentType.CARD, Tag.GUILD));
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
          public List<Component> components() {
            return components;
          }
        };
    return RuleSet.assemble(List.of(new BasePack(), pack));
  }

  public static RuleSet baseSized() {
    return rules(pieces(23, 23, 20, 7, 12, 10));
  }

  private SetupFixtures() {}
}
