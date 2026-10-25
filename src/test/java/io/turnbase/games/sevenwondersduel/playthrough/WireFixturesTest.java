package io.turnbase.games.sevenwondersduel.playthrough;

import static io.turnbase.games.sevenwondersduel.GdkDoubles.ONE;
import static io.turnbase.games.sevenwondersduel.GdkDoubles.TWO;
import static io.turnbase.games.sevenwondersduel.GdkDoubles.action;
import static io.turnbase.games.sevenwondersduel.GdkDoubles.init;
import static io.turnbase.games.sevenwondersduel.GdkDoubles.team;
import static io.turnbase.games.sevenwondersduel.GdkDoubles.viewer;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.turnbase.games.sevenwondersduel.SevenWondersDuelGame;
import io.turnbase.games.sevenwondersduel.SevenWondersDuelState;
import io.turnbase.games.sevenwondersduel.agenda.DecisionStep;
import io.turnbase.games.sevenwondersduel.catalog.BasePack;
import io.turnbase.games.sevenwondersduel.catalog.RuleSet;
import io.turnbase.games.sevenwondersduel.content.BaseContent;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.snapshot.Snapshots;
import io.turnbase.games.sevenwondersduel.state.DuelState;
import io.turnbase.games.sevenwondersduel.state.Phase;
import io.turnbase.games.sevenwondersduel.view.EventView;
import io.turnbase.games.sevenwondersduel.view.PawnView;
import io.turnbase.games.sevenwondersduel.view.PlaceView;
import io.turnbase.games.sevenwondersduel.wire.ServerMessage;
import io.turnbase.games.sevenwondersduel.wire.WireAction;
import io.turnbase.gdk.effect.SendToPlayerEffect;
import io.turnbase.gdk.model.Player;
import io.turnbase.gdk.result.ActionResult;
import io.turnbase.serialization.GdkObjectMapper;
import io.turnbase.serialization.GdkSerialization;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;

/**
 * The messages the frontend parses, as the GDK serializer writes them, pinned to checked-in JSON.
 * {@code src/main/ts/core/wire.ts} mirrors these shapes and its tests read the same files, so a
 * wire change fails on whichever side did not follow. To accept a deliberate change, run with
 * {@code -Dwire.update=true} and review the diff.
 */
class WireFixturesTest {

  private static final Path DIR = Path.of("src/test/resources/wire");
  private static final long SEED = 7;
  private static final RuleSet RULES = RuleSet.assemble(List.of(new BasePack(), new BaseContent()));
  private static final SevenWondersDuelGame GAME = new SevenWondersDuelGame();
  private static final ObjectMapper MAPPER = GdkObjectMapper.create();
  private static final GdkSerialization SERIALIZATION = new GdkSerialization(MAPPER);

  private static Player seat(PlayerId id) {
    return id.value().equals(ONE.id()) ? ONE : TWO;
  }

  private static void pinned(String name, Object value) {
    JsonNode actual = MAPPER.valueToTree(value);
    // The GDK's own writer is what the host calls; it must accept the value unchanged.
    SERIALIZATION.writePayload(value, Long.MAX_VALUE);
    Path file = DIR.resolve(name + ".json");
    try {
      if (Boolean.getBoolean("wire.update")) {
        Files.createDirectories(DIR);
        Files.writeString(
            file, MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(actual) + "\n");
      }
      assertThat(file).as("fixture " + file + " (run with -Dwire.update=true)").exists();
      assertThat(actual).isEqualTo(MAPPER.readTree(Files.readString(file)));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static ServerMessage sentTo(ActionResult<SevenWondersDuelState> result, Player to) {
    return result.effects().stream()
        .filter(SendToPlayerEffect.class::isInstance)
        .map(SendToPlayerEffect.class::cast)
        .filter(e -> e.playerId().equals(to.id()))
        .map(e -> (ServerMessage) e.payload())
        .findFirst()
        .orElseThrow();
  }

  /** Plays random legal moves until {@code stop} holds, returning the last action's result. */
  private static ActionResult<SevenWondersDuelState> playUntil(
      SevenWondersDuelState from, Predicate<DuelState> stop) {
    SevenWondersDuelState state = from;
    Random random = new Random(SEED);
    while (true) {
      DuelState duel = Snapshots.fromSnapshot(state);
      assertThat(duel.phase())
          .as("the game ended before the wanted position")
          .isNotEqualTo(Phase.OVER);
      Bot.Move move = Bot.legalActions(RULES, duel);
      var chosen = move.actions().get(random.nextInt(move.actions().size()));
      ActionResult<SevenWondersDuelState> result =
          GAME.onAction(state, action(seat(move.actor()), Moves.wire(chosen)));
      state = result.state();
      if (stop.test(Snapshots.fromSnapshot(state))) {
        return result;
      }
    }
  }

  private static SevenWondersDuelState start() {
    return GAME.onInit(init(SEED, team(ONE), team(TWO))).state();
  }

  @Test
  void resyncCarriesTheViewAndTheCatalog() {
    pinned("resync", GAME.onResync(start(), viewer(ONE)).view());
  }

  @Test
  void anActionShowsTheOtherSeatTheViewAndTheEvents() {
    pinned("view-after-draft-pick", sentTo(playUntil(start(), d -> true), TWO));
  }

  @Test
  void aViewInsideAnAgeCarriesQuotesAndEvents() {
    ActionResult<SevenWondersDuelState> result = playUntil(start(), d -> d.phase() == Phase.AGE);
    DuelState duel = Snapshots.fromSnapshot(result.state());

    pinned("view-in-age", sentTo(result, seat(duel.active())));
  }

  @Test
  void aViewWithADecisionPendingNamesTheDeciderAndTheOptions() {
    ActionResult<SevenWondersDuelState> result =
        playUntil(start(), d -> !d.agenda().isEmpty() && d.agenda().top() instanceof DecisionStep);
    DuelState duel = Snapshots.fromSnapshot(result.state());

    ServerMessage message = sentTo(result, seat(((DecisionStep) duel.agenda().top()).decider()));
    assertThat(message.view().pending()).isNotNull();
    pinned("view-with-decision", message);
  }

  @Test
  void aFinishedGameCarriesTheVictory() {
    ActionResult<SevenWondersDuelState> result = playUntil(start(), d -> d.victory() != null);

    ServerMessage message = sentTo(result, ONE);
    assertThat(message.view().victoryReason()).isNotNull();
    pinned("view-game-over", message);
  }

  @Test
  void aRefusalCarriesOnlyTheReason() {
    pinned("rejected", ServerMessage.rejected("not your turn"));
  }

  @Test
  void everyEventKindHasAShape() {
    UUID player = ONE.id();
    PlaceView deck = new PlaceView("deck", "");
    PlaceView city = new PlaceView("city", player.toString());
    pinned(
        "events",
        List.of(
            new EventView(0, -1, "moved", null, "lumber-yard", deck, city, null, 0, null, null),
            new EventView(1, 0, "coins", "lumber-yard", null, null, null, player, -2, null, null),
            new EventView(2, 0, "trade", null, null, null, null, player, 3, null, null),
            new EventView(3, 0, "chain", null, "baths", null, null, player, 0, null, null),
            new EventView(
                4,
                0,
                "pawn",
                null,
                null,
                null,
                null,
                null,
                0,
                new PawnView(null, 0),
                new PawnView(player, 1)),
            new EventView(
                5, -1, "playAgainGranted", "piraeus", null, null, null, player, 0, null, null),
            new EventView(
                6, -1, "playAgainCleared", null, null, null, null, player, 0, null, null)));
  }

  @Test
  void everyActionTypeHasAShape() {
    UUID player = TWO.id();
    pinned(
        "actions",
        List.of(
            new WireAction("draftWonder", "the-colossus", null, null),
            new WireAction("chooseStarter", null, null, player),
            new WireAction("discard", "lumber-yard", null, null),
            new WireAction("build", "lumber-yard", null, null),
            new WireAction("buildWonder", "lumber-yard", "the-colossus", null),
            new WireAction("answerComponent", "baths", null, null),
            new WireAction("answerPlayer", null, null, player)));
  }
}
