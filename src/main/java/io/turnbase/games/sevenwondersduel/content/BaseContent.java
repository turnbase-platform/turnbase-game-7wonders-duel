package io.turnbase.games.sevenwondersduel.content;

import io.turnbase.games.sevenwondersduel.catalog.Component;
import io.turnbase.games.sevenwondersduel.catalog.ContentPack;
import io.turnbase.games.sevenwondersduel.id.BackId;
import io.turnbase.games.sevenwondersduel.id.PackId;
import io.turnbase.games.sevenwondersduel.zone.ZoneKind;
import java.util.List;
import java.util.stream.Stream;

/**
 * The base game's cards, Wonders and tokens, kept apart from {@code BasePack} (zones and layouts)
 * so that tests can run the engine over the board alone with a small fixture catalogue.
 */
public final class BaseContent implements ContentPack {

  @Override
  public PackId id() {
    return PackId.of("base-content");
  }

  @Override
  public List<ZoneKind> zoneKinds() {
    return List.of();
  }

  @Override
  public List<Component> components() {
    return Stream.<List<? extends Component>>of(
            AgeOne.cards(), AgeTwo.cards(), AgeThree.cards(), Guilds.cards(), Wonders.all())
        .flatMap(List::stream)
        .<Component>map(component -> component)
        .toList();
  }

  @Override
  public List<BackId> backs() {
    return Backs.ALL;
  }
}
