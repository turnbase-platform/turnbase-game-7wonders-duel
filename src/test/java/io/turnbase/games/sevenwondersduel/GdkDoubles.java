package io.turnbase.games.sevenwondersduel;

import io.turnbase.gdk.context.ActionContext;
import io.turnbase.gdk.context.InitContext;
import io.turnbase.gdk.context.PlayerLeftContext;
import io.turnbase.gdk.context.PlayerLeftReason;
import io.turnbase.gdk.context.ResyncContext;
import io.turnbase.gdk.context.ViewerRole;
import io.turnbase.gdk.model.GameParameters;
import io.turnbase.gdk.model.Player;
import io.turnbase.gdk.model.Team;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** Minimal in-memory GDK contexts for driving the game's hooks in tests. */
public final class GdkDoubles {

  public static final Player ONE = player("00000000-0000-0000-0000-000000000001", "One");
  public static final Player TWO = player("00000000-0000-0000-0000-000000000002", "Two");

  private GdkDoubles() {}

  public static Player player(String id, String name) {
    UUID uuid = UUID.fromString(id);
    return new Player(uuid, name, UUID.nameUUIDFromBytes(("team-" + id).getBytes()));
  }

  public static Team team(Player... players) {
    return new Team(players[0].teamId(), "team", List.of(players));
  }

  public static InitContext init(long seed, Team... teams) {
    return new InitContext() {
      @Override
      public List<Team> teams() {
        return List.of(teams);
      }

      @Override
      public GameParameters parameters() {
        return new GameParameters() {
          @Override
          public int getInt(String key) {
            throw new UnsupportedOperationException(key);
          }

          @Override
          public double getDecimal(String key) {
            throw new UnsupportedOperationException(key);
          }

          @Override
          public boolean getBoolean(String key) {
            throw new UnsupportedOperationException(key);
          }

          @Override
          public String getString(String key) {
            throw new UnsupportedOperationException(key);
          }

          @Override
          public String getEnum(String key) {
            throw new UnsupportedOperationException(key);
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
        return seed;
      }
    };
  }

  public static ActionContext action(Player actor, Object payload) {
    return new ActionContext() {
      @Override
      public Player player() {
        return actor;
      }

      @Override
      public <A> A action(Class<A> type) {
        return type.cast(payload);
      }
    };
  }

  public static ResyncContext viewer(Player viewer) {
    return new ResyncContext() {
      @Override
      public ViewerRole role() {
        return ViewerRole.PLAYER;
      }

      @Override
      public Optional<Player> viewer() {
        return Optional.of(viewer);
      }
    };
  }

  public static ResyncContext spectator() {
    return new ResyncContext() {
      @Override
      public ViewerRole role() {
        return ViewerRole.SPECTATOR;
      }

      @Override
      public Optional<Player> viewer() {
        return Optional.empty();
      }
    };
  }

  public static PlayerLeftContext left(Player who, PlayerLeftReason reason) {
    return new PlayerLeftContext() {
      @Override
      public Player player() {
        return who;
      }

      @Override
      public PlayerLeftReason reason() {
        return reason;
      }
    };
  }
}
