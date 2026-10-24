package io.turnbase.games.sevenwondersduel.military;

import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P1;
import static io.turnbase.games.sevenwondersduel.effect.Fixtures.P2;
import static org.assertj.core.api.Assertions.assertThat;

import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.effect.Fixtures;
import io.turnbase.games.sevenwondersduel.effect.Fixtures.Card;
import io.turnbase.games.sevenwondersduel.effect.LoseCoins;
import io.turnbase.games.sevenwondersduel.effect.Resolution;
import io.turnbase.games.sevenwondersduel.event.CoinsChanged;
import io.turnbase.games.sevenwondersduel.event.EventRecord;
import io.turnbase.games.sevenwondersduel.event.PawnMoved;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.id.ZoneId;
import io.turnbase.games.sevenwondersduel.state.Pawn;
import io.turnbase.games.sevenwondersduel.state.Phase;
import io.turnbase.games.sevenwondersduel.state.Victory;
import io.turnbase.games.sevenwondersduel.zone.Location;
import io.turnbase.games.sevenwondersduel.zone.Placements;
import java.util.List;
import org.junit.jupiter.api.Test;

class MilitaryTest {

  private static final PlayerId MOVER = P1;
  private static final PlayerId RIVAL = P2;
  private static final Card LOOT_TWO = Card.of("loot2").withEffects(new LoseCoins(2));
  private static final Card LOOT_FIVE = Card.of("loot5").withEffects(new LoseCoins(5));
  private static final RuleSet RULES = Fixtures.rules(LOOT_TWO, LOOT_FIVE);

  /** Rival has {@code coins}; each token sits on the given zone of the rival's side. */
  private static Resolution resolution(Pawn pawn, int coins, Object... tokensAndZones) {
    Object[] placed = new Object[tokensAndZones.length];
    for (int i = 0; i < tokensAndZones.length; i += 2) {
      placed[i] = ((Card) tokensAndZones[i]).id();
      placed[i + 1] = MilitaryTrack.tokenSpot(RIVAL, (Integer) tokensAndZones[i + 1]);
    }
    return new Resolution(Fixtures.state(RULES, MOVER, 7, coins, placed).withPawn(pawn), RULES);
  }

  private static List<Integer> lossesOf(Resolution r, PlayerId player) {
    return r.events().stream()
        .map(EventRecord::event)
        .filter(CoinsChanged.class::isInstance)
        .map(CoinsChanged.class::cast)
        .filter(e -> e.player().equals(player))
        .map(CoinsChanged::delta)
        .toList();
  }

  private static Location locationOf(Resolution r, Card token) {
    return Placements.locationOf(r.state().placements(), token.id());
  }

  @Test
  void aShieldFromTheCentreEntersTheRivalsSideWithoutFiringAnything() {
    Resolution r = resolution(Pawn.CENTRE, 7);

    Military.advance(r, MOVER, 1);

    assertThat(r.state().pawn()).isEqualTo(new Pawn(RIVAL, 1));
    assertThat(r.events())
        .extracting(EventRecord::event)
        .containsExactly(new PawnMoved(Pawn.CENTRE, new Pawn(RIVAL, 1)));
    assertThat(r.state().agenda().isEmpty()).isTrue();
  }

  @Test
  void enteringAZoneFiresItsTokenOnceAndDestroysIt() {
    Resolution r = resolution(Pawn.CENTRE, 7, LOOT_TWO, 2);

    Military.advance(r, MOVER, 3);
    r.drain();

    assertThat(lossesOf(r, RIVAL)).containsExactly(-2);
    assertThat(locationOf(r, LOOT_TWO))
        .isEqualTo(new Location(ZoneId.of("destroyed"), RIVAL.value().toString()));
  }

  @Test
  void theLossIsCappedByTheVictimsTreasury() {
    Resolution r = resolution(Pawn.CENTRE, 3, LOOT_FIVE, 2);

    Military.advance(r, MOVER, 3);
    r.drain();

    assertThat(lossesOf(r, RIVAL)).containsExactly(-3);
    assertThat(r.state().player(RIVAL).coins()).isZero();
  }

  @Test
  void movingWithinAZoneFiresNothing() {
    Resolution r = resolution(new Pawn(RIVAL, 3), 7, LOOT_TWO, 2);

    Military.advance(r, MOVER, 2);

    assertThat(r.state().pawn()).isEqualTo(new Pawn(RIVAL, 5));
    assertThat(r.state().agenda().isEmpty()).isTrue();
    assertThat(locationOf(r, LOOT_TWO).zone()).isEqualTo(ZoneId.of("military"));
  }

  @Test
  void tokensPassedInOneMoveFireInTheOrderEntered() {
    Resolution r = resolution(Pawn.CENTRE, 20, LOOT_FIVE, 3, LOOT_TWO, 2);

    Military.advance(r, MOVER, 6);
    r.drain();

    assertThat(lossesOf(r, RIVAL)).containsExactly(-2, -5);
  }

  @Test
  void aMoveThatCrossesTheCentreEndsOnTheMoversAdvantage() {
    Resolution r = resolution(new Pawn(MOVER, 2), 7);

    Military.advance(r, MOVER, 3);

    assertThat(r.state().pawn()).isEqualTo(new Pawn(RIVAL, 1));
    assertThat(r.state().agenda().isEmpty()).isTrue();
  }

  @Test
  void leavingAZoneAndReEnteringItFiresAFreshTokenButNotAConsumedOne() {
    Resolution r = resolution(Pawn.CENTRE, 20, LOOT_TWO, 2);
    Military.advance(r, MOVER, 3);
    r.drain();
    // the rival pushes the pawn back to the centre, and a new token is laid on the zone
    r.setPawn(Pawn.CENTRE);
    r.advance(
        s ->
            s.withPlacements(
                Placements.place(
                    s.placements(),
                    LOOT_FIVE.id(),
                    MilitaryTrack.tokenSpot(RIVAL, 2),
                    RULES.zones())));

    Military.advance(r, MOVER, 3);
    r.drain();

    assertThat(lossesOf(r, RIVAL)).containsExactly(-2, -5);
  }

  @Test
  void reachingTheCapitalWinsAndDropsTheRestOfTheMove() {
    Resolution r = resolution(Pawn.CENTRE, 7);

    Military.advance(r, MOVER, 12);

    assertThat(r.state().pawn()).isEqualTo(new Pawn(RIVAL, Pawn.CAPITAL));
    assertThat(r.state().phase()).isEqualTo(Phase.OVER);
    assertThat(r.state().victory()).isEqualTo(new Victory(MOVER, Victory.Reason.MILITARY));
    assertThat(r.events().stream().filter(e -> e.event() instanceof PawnMoved)).hasSize(9);
  }

  @Test
  void aVictoryDiscardsTheTokenEffectsCollectedOnTheWay() {
    Resolution r = resolution(Pawn.CENTRE, 7, LOOT_TWO, 2);

    Military.advance(r, MOVER, 9);
    r.drain();

    assertThat(r.state().player(RIVAL).coins()).isEqualTo(7);
    assertThat(r.state().agenda().isEmpty()).isTrue();
  }
}
