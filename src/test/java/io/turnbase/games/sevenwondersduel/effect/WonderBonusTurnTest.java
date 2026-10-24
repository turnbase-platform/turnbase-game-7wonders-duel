package io.turnbase.games.sevenwondersduel.effect;

import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P1;
import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P2;
import static org.assertj.core.api.Assertions.assertThat;

import io.turnbase.games.sevenwondersduel.catalog.ComponentType;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.effect.Fixtures.Card;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.zone.Location;
import io.turnbase.games.sevenwondersduel.zone.Zones;
import org.junit.jupiter.api.Test;

class WonderBonusTurnTest {

  private record Wonder(io.turnbase.games.sevenwondersduel.id.ComponentId id)
      implements io.turnbase.games.sevenwondersduel.catalog.Component {
    @Override
    public String name() {
      return id.value();
    }

    @Override
    public ComponentType type() {
      return ComponentType.WONDER;
    }
  }

  private static Location built(PlayerId player) {
    return new Location(Zones.WONDERS_BUILT, player.value().toString());
  }

  private static Location unbuilt(PlayerId player) {
    return new Location(Zones.WONDERS_UNBUILT, player.value().toString());
  }

  private static Resolution game() {
    Card token = Card.of("theology").withTriggers(new WonderBonusTurn());
    Wonder wonder = new Wonder(io.turnbase.games.sevenwondersduel.id.ComponentId.of("w"));
    Card plain = Card.of("plain");
    RuleSet rules = Fixtures.rules(token, wonder, plain);
    return new Resolution(
        Fixtures.state(
            rules,
            P1,
            7,
            7,
            token.id(),
            Fixtures.city(P1),
            wonder.id(),
            unbuilt(P1),
            plain.id(),
            Fixtures.zone("deck")),
        rules);
  }

  @Test
  void buildingAWonderGrantsAnotherTurn() {
    Resolution r = game();

    r.move(io.turnbase.games.sevenwondersduel.id.ComponentId.of("w"), built(P1));

    assertThat(r.state().player(P1).playAgain()).isTrue();
  }

  @Test
  void theOpponentsWonderGrantsNothing() {
    Resolution r = game();

    r.move(io.turnbase.games.sevenwondersduel.id.ComponentId.of("w"), built(P2));

    assertThat(r.state().player(P1).playAgain()).isFalse();
    assertThat(r.state().player(P2).playAgain()).isFalse();
  }

  @Test
  void anotherComponentEnteringTheWondersGrantsNothing() {
    Resolution r = game();

    r.move(io.turnbase.games.sevenwondersduel.id.ComponentId.of("plain"), built(P1));

    assertThat(r.state().player(P1).playAgain()).isFalse();
  }
}
