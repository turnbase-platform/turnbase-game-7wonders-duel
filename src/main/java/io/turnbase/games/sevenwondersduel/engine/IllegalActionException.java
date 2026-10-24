package io.turnbase.games.sevenwondersduel.engine;

/** The action is not allowed now, from this player or with this content. The state is unchanged. */
public final class IllegalActionException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public IllegalActionException(String message) {
    super(message);
  }
}
