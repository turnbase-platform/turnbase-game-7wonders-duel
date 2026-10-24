package io.turnbase.games.sevenwondersduel.draft;

import io.turnbase.games.sevenwondersduel.age.AgeStart;
import io.turnbase.games.sevenwondersduel.effect.Resolution;
import io.turnbase.games.sevenwondersduel.id.ComponentId;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.id.ZoneId;
import io.turnbase.games.sevenwondersduel.setup.Setup;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.state.Phase;
import io.turnbase.games.sevenwondersduel.zone.Location;
import io.turnbase.games.sevenwondersduel.zone.Placements;
import java.util.List;
import java.util.Optional;

/**
 * The Wonder draft (B-WND-2..5). It keeps no state of its own: whose pick it is follows from the
 * first player and how many Wonders have been taken, so a saved game resumes mid-draft unchanged.
 */
public final class Draft {

  private static final ZoneId DRAFT = ZoneId.of("draft");
  private static final ZoneId UNBUILT = ZoneId.of("wonders.unbuilt");
  private static final ZoneId BOX = ZoneId.of("box");

  private Draft() {}

  /** The player who must pick now, or empty when no draft is under way. */
  public static Optional<PlayerId> toMove(DuelState state) {
    if (state.phase() != Phase.WONDER_DRAFT) {
      return Optional.empty();
    }
    int picks = picks(state);
    int round = picks / Setup.DRAFT_ROUND;
    int slot = picks % Setup.DRAFT_ROUND;
    boolean firstPlayerPicks = (slot == 0 || slot == Setup.DRAFT_ROUND - 1) == (round % 2 == 0);
    return Optional.of(firstPlayerPicks ? state.first() : state.opponentOf(state.first()));
  }

  /** Whether {@code wonder} is currently offered. */
  public static boolean isOffered(DuelState state, ComponentId wonder) {
    return Placements.idsIn(state.placements(), DRAFT).contains(wonder);
  }

  /**
   * Gives {@code wonder} to {@code player}, then moves the draft on: the next four Wonders are
   * revealed after each round (B-WND-3) and, after the last pick, the unused ones go to the box
   * (B-WND-4) and Age I starts.
   */
  public static void take(Resolution resolution, PlayerId player, ComponentId wonder) {
    resolution.move(wonder, new Location(UNBUILT, player.value().toString()));
    DuelState state = resolution.state();
    if (!Placements.idsIn(state.placements(), DRAFT).isEmpty()) {
      follow(resolution);
      return;
    }
    List<ComponentId> pool = Placements.idsIn(state.placements(), Setup.WONDER_POOL);
    if (picks(state) < Setup.DRAFT_WONDERS) {
      pool.subList(0, Setup.DRAFT_ROUND).forEach(id -> resolution.move(id, Location.of(DRAFT)));
      follow(resolution);
      return;
    }
    pool.forEach(id -> resolution.move(id, Location.of(BOX)));
    AgeStart.begin(resolution, 1, state.first());
  }

  private static void follow(Resolution resolution) {
    PlayerId mover = toMove(resolution.state()).orElseThrow();
    resolution.advance(s -> s.withActive(mover));
  }

  private static int picks(DuelState state) {
    return (int) state.placements().stream().filter(p -> p.at().zone().equals(UNBUILT)).count();
  }
}
