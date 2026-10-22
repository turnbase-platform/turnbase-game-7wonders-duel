package io.turnbase.games.sevenwondersduel;

import static org.assertj.core.api.Assertions.assertThat;

import io.turnbase.gdk.context.ActionContext;
import io.turnbase.gdk.context.ActionDecodeException;
import io.turnbase.gdk.context.InitContext;
import io.turnbase.gdk.context.PlayerLeftContext;
import io.turnbase.gdk.context.PlayerLeftReason;
import io.turnbase.gdk.context.ResyncContext;
import io.turnbase.gdk.context.ViewerRole;
import io.turnbase.gdk.effect.BroadcastEffect;
import io.turnbase.gdk.effect.Effect;
import io.turnbase.gdk.effect.GameOverEffect;
import io.turnbase.gdk.effect.SendToPlayerEffect;
import io.turnbase.gdk.model.GameParameters;
import io.turnbase.gdk.model.Player;
import io.turnbase.gdk.model.Team;
import io.turnbase.gdk.result.ActionResult;
import io.turnbase.serialization.GdkSerialization;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SevenWondersDuelGameTest {

  private final SevenWondersDuelGame game = new SevenWondersDuelGame();
  private final GdkSerialization serialization = new GdkSerialization();

  private final UUID teamA = UUID.randomUUID();
  private final UUID teamB = UUID.randomUUID();
  private final Player a = new Player(UUID.randomUUID(), "Alice", teamA);
  private final Player b = new Player(UUID.randomUUID(), "Bob", teamB);
  private SevenWondersDuelState state;

  @BeforeEach
  void init() {
    InitContext ctx =
        new InitContext() {
          @Override
          public List<Team> teams() {
            return List.of(new Team(teamA, "A", List.of(a)), new Team(teamB, "B", List.of(b)));
          }

          @Override
          public GameParameters parameters() {
            return new GameParameters() {
              @Override
              public int getInt(String key) {
                throw new UnsupportedOperationException();
              }

              @Override
              public double getDecimal(String key) {
                throw new UnsupportedOperationException();
              }

              @Override
              public boolean getBoolean(String key) {
                throw new UnsupportedOperationException();
              }

              @Override
              public String getString(String key) {
                throw new UnsupportedOperationException();
              }

              @Override
              public String getEnum(String key) {
                throw new UnsupportedOperationException();
              }

              @Override
              public boolean contains(String key) {
                return false;
              }

              @Override
              public Set<String> keys() {
                return Set.of();
              }
            };
          }

          @Override
          public long seed() {
            return 1L;
          }
        };
    state = roundTrip(game.onInit(ctx).state());
  }

  @Test
  void theFirstTeamsPlayerMovesFirst() {
    assertThat(state.turn()).isEqualTo(a.id());
    assertThat(state.turnNumber()).isZero();
    assertThat(state.over()).isFalse();
  }

  @Test
  void endingATurnPassesItAndBroadcasts() {
    var result = endTurn(a);
    state = roundTrip(result.state());

    assertThat(state.turn()).isEqualTo(b.id());
    assertThat(state.turnNumber()).isEqualTo(1);
    assertThat(result.effects()).anyMatch(e -> e instanceof BroadcastEffect);
    assertThat(result.effects()).noneMatch(e -> e instanceof GameOverEffect);
  }

  @Test
  void actingOutOfTurnIsRefusedToTheActorAloneAndChangesNothing() {
    var result = endTurn(b);

    assertThat(result.state()).isEqualTo(state);
    assertThat(result.effects()).singleElement().isInstanceOf(SendToPlayerEffect.class);
    assertThat(((SendToPlayerEffect) result.effects().get(0)).playerId()).isEqualTo(b.id());
  }

  @Test
  void anUnknownOrMalformedActionIsRefusedAndConsumesNoTurn() {
    var unknown = act(a, () -> new Action("FLY"));
    var noType = act(a, () -> new Action(null));
    var nullPayload = act(a, () -> null);
    var undecodable =
        act(
            a,
            () -> {
              throw new ActionDecodeException("cannot decode action as Action");
            });

    for (var result : List.of(unknown, noType, nullPayload, undecodable)) {
      assertThat(result.state()).isEqualTo(state);
      assertThat(result.effects()).singleElement().isInstanceOf(SendToPlayerEffect.class);
    }
    assertThat(state.turn()).isEqualTo(a.id());
  }

  @Test
  void theGameEndsAsADrawAfterTheLastTurn() {
    ActionResult<SevenWondersDuelState> last = null;
    for (int i = 0; i < SevenWondersDuelGame.MAX_TURNS; i++) {
      last = endTurn(state.turn().equals(a.id()) ? a : b);
      state = roundTrip(last.state());
    }

    GameOverEffect over = gameOver(last.effects());
    assertThat(over.outcome().draw()).isTrue();
    assertThat(over.outcome().teamResults()).extracting(r -> r.placement()).containsOnly(1);
    assertThat(state.over()).isTrue();
    assertThat(endTurn(state.turn().equals(a.id()) ? a : b).state()).isEqualTo(state);
  }

  @Test
  void leavingMidGameForfeitsToTheOpponent() {
    var result = game.onPlayerLeft(state, left(a, PlayerLeftReason.LEFT));

    GameOverEffect over = gameOver(result.effects());
    assertThat(over.outcome().draw()).isFalse();
    assertThat(over.outcome().teamResults().get(0).teamId()).isEqualTo(teamB);
    assertThat(result.state().over()).isTrue();
  }

  @Test
  void leavingAfterTheGameEndedChangesNothing() {
    var over = new SevenWondersDuelState(a.id(), teamA, b.id(), teamB, a.id(), 6, true);
    var result = game.onPlayerLeft(over, left(b, PlayerLeftReason.GRACE_PERIOD_EXPIRED));

    assertThat(result.state()).isEqualTo(over);
    assertThat(result.effects()).isEmpty();
  }

  @Test
  void resyncShowsEveryViewerTheSamePosition() {
    var forPlayer = (GameMessage.View) game.onResync(state, resync(ViewerRole.PLAYER, b)).view();
    var forSpectator =
        (GameMessage.View) game.onResync(state, resync(ViewerRole.SPECTATOR, null)).view();

    assertThat(forPlayer).isEqualTo(forSpectator);
    assertThat(forPlayer.turn()).isEqualTo(a.id());
  }

  @Test
  void stateAndActionSurviveTheExecutingRoundTrip() {
    assertThat(serialization.roundTrips(endTurn(a).state(), SevenWondersDuelState.class)).isTrue();
    assertThat(serialization.roundTrips(new Action(Action.END_TURN), Action.class)).isTrue();
  }

  private ActionResult<SevenWondersDuelState> endTurn(Player who) {
    return act(who, () -> new Action(Action.END_TURN));
  }

  private ActionResult<SevenWondersDuelState> act(Player who, Supplier<Action> payload) {
    return game.onAction(
        state,
        new ActionContext() {
          @Override
          public Player player() {
            return who;
          }

          @Override
          public <T> T action(Class<T> type) {
            return type.cast(payload.get());
          }
        });
  }

  private static PlayerLeftContext left(Player who, PlayerLeftReason why) {
    return new PlayerLeftContext() {
      @Override
      public Player player() {
        return who;
      }

      @Override
      public PlayerLeftReason reason() {
        return why;
      }
    };
  }

  private static ResyncContext resync(ViewerRole role, Player viewer) {
    return new ResyncContext() {
      @Override
      public ViewerRole role() {
        return role;
      }

      @Override
      public Optional<Player> viewer() {
        return Optional.ofNullable(viewer);
      }
    };
  }

  private static GameOverEffect gameOver(List<Effect> effects) {
    return effects.stream()
        .filter(GameOverEffect.class::isInstance)
        .map(GameOverEffect.class::cast)
        .findFirst()
        .orElseThrow();
  }

  private SevenWondersDuelState roundTrip(SevenWondersDuelState s) {
    return serialization.readState(
        serialization.writeState(s, 10L * 1024 * 1024), SevenWondersDuelState.class);
  }
}
