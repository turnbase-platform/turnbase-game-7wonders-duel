package io.turnbase.games.sevenwondersduel.scoring;

import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P1;
import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P2;
import static io.turnbase.games.sevenwondersduel.effect.Fixtures.city;
import static io.turnbase.games.sevenwondersduel.effect.Fixtures.zone;
import static org.assertj.core.api.Assertions.assertThat;

import io.turnbase.games.sevenwondersduel.catalog.Component;
import io.turnbase.games.sevenwondersduel.catalog.ComponentType;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.catalog.Tag;
import io.turnbase.games.sevenwondersduel.effect.Fixtures;
import io.turnbase.games.sevenwondersduel.effect.Modifier;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.id.ZoneId;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.state.Pawn;
import io.turnbase.games.sevenwondersduel.zone.Location;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ScoringTest {

  private record Part(ComponentId id, ComponentType type, Set<Tag> tags, List<Modifier> modifiers)
      implements Component {

    static Part of(String id, ComponentType type, Modifier... modifiers) {
      return new Part(ComponentId.of(id), type, Set.of(), List.of(modifiers));
    }

    Part tagged(Tag tag) {
      return new Part(id, type, Set.of(tag), modifiers);
    }

    @Override
    public String name() {
      return id.value();
    }
  }

  private static Location built(PlayerId player) {
    return new Location(ZoneId.of("wonders.built"), player.value().toString());
  }

  private static RuleSet rules(Part... parts) {
    return Fixtures.rules(parts);
  }

  private static DuelState state(RuleSet rules, Object... placed) {
    return Fixtures.state(rules, P1, 0, 0, placed);
  }

  @Test
  void fixedPointsLandInTheLineOfTheirComponent() {
    Part blue = Part.of("blue", ComponentType.CARD, Points.fixed(3)).tagged(Tag.BLUE);
    Part green = Part.of("green", ComponentType.CARD, Points.fixed(2));
    Part wonder = Part.of("wonder", ComponentType.WONDER, Points.fixed(4));
    Part token = Part.of("token", ComponentType.TOKEN, Points.fixed(5));
    RuleSet rules = rules(blue, green, wonder, token);
    DuelState s =
        state(
            rules,
            blue.id(),
            city(P1),
            green.id(),
            city(P1),
            wonder.id(),
            built(P1),
            token.id(),
            city(P1));

    Scorecard card = Scoring.score(s, rules, P1);

    assertThat(card.line("blue")).isEqualTo(3);
    assertThat(card.line("buildings")).isEqualTo(2);
    assertThat(card.line("wonders")).isEqualTo(4);
    assertThat(card.line("progress")).isEqualTo(5);
    assertThat(card.total()).isEqualTo(14);
  }

  @Test
  void computedPointsSeeTheOwnersCity() {
    Part math =
        Part.of(
                "math",
                ComponentType.TOKEN,
                (Points) ctx -> 3 * ctx.count(ctx.owner(), Tag.PROGRESS))
            .tagged(Tag.PROGRESS);
    Part law = Part.of("law", ComponentType.TOKEN).tagged(Tag.PROGRESS);
    Part other = Part.of("other", ComponentType.TOKEN).tagged(Tag.PROGRESS);
    RuleSet rules = rules(math, law, other);
    DuelState s = state(rules, math.id(), city(P1), law.id(), city(P1), other.id(), city(P2));

    assertThat(Scoring.score(s, rules, P1).line("progress")).isEqualTo(6);
    assertThat(Scoring.score(s, rules, P2).line("progress")).isZero();
  }

  @Test
  void componentsOutsideTheCityScoreNothing() {
    Part card = Part.of("card", ComponentType.CARD, Points.fixed(3));
    Part wonder = Part.of("wonder", ComponentType.WONDER, Points.fixed(4));
    RuleSet rules = rules(card, wonder);
    DuelState s = state(rules, card.id(), zone("discard"), wonder.id(), zone("deck"));

    assertThat(Scoring.score(s, rules, P1).total()).isZero();
  }

  @Test
  void twoModifiersOnOneComponentAdd() {
    Part card = Part.of("card", ComponentType.CARD, Points.fixed(1), Points.fixed(2));
    RuleSet rules = rules(card);
    DuelState s = state(rules, card.id(), city(P1));

    assertThat(Scoring.score(s, rules, P1).line("buildings")).isEqualTo(3);
  }

  @Test
  void militaryPointsGoToThePlayerTheirOpponentIsLosingTo() {
    RuleSet rules = rules();
    DuelState s = state(rules).withPawn(new Pawn(P2, 4));

    assertThat(Scoring.score(s, rules, P1).line("military")).isEqualTo(5);
    assertThat(Scoring.score(s, rules, P2).line("military")).isZero();
  }

  @Test
  void aPawnAtTheCentreScoresForNobody() {
    RuleSet rules = rules();
    DuelState s = state(rules);

    assertThat(Scoring.score(s, rules, P1).line("military")).isZero();
    assertThat(Scoring.score(s, rules, P2).line("military")).isZero();
  }

  @Test
  void treasuryScoresOnePointPerThreeCoinsRoundedDown() {
    RuleSet rules = rules();
    DuelState s = Fixtures.state(rules, P1, 8, 9);

    assertThat(Scoring.score(s, rules, P1).line("coins")).isEqualTo(2);
    assertThat(Scoring.score(s, rules, P2).line("coins")).isEqualTo(3);
  }
}
