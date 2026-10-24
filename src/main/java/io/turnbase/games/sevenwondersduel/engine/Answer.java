package io.turnbase.games.sevenwondersduel.engine;

import io.turnbase.games.sevenwondersduel.agenda.Option;

/**
 * The reply to the decision on top of the agenda (B-EFF-3).
 *
 * @param choice the picked option; must be one of those offered
 */
public record Answer(Option choice) implements PlayerAction {}
