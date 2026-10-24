package io.turnbase.games.sevenwondersduel.view;

import java.util.List;
import java.util.UUID;

/**
 * The decision on top of the agenda: who must answer and what they may pick. Options are public
 * components or players, never hidden ones.
 *
 * @param decider the player who must answer
 * @param options a component id or player id per option, in offer order
 */
public record PendingView(UUID decider, List<String> options) {}
