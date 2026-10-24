package io.turnbase.games.sevenwondersduel.view;

import java.util.List;
import java.util.UUID;

/**
 * The decision on top of the agenda: who must answer and what they may pick. The decider sees every
 * option; anyone else sees only the options they may identify and a count of the rest.
 *
 * @param decider the player who must answer
 * @param options a component id or player id per option the viewer may see, in offer order
 * @param hidden how many options the viewer may not identify
 */
public record PendingView(UUID decider, List<String> options, int hidden) {}
