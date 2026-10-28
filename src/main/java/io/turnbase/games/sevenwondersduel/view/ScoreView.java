package io.turnbase.games.sevenwondersduel.view;

import java.util.List;
import java.util.UUID;

/** One player's final score: the lines in display order, then their sum (B-END-2). */
public record ScoreView(UUID player, List<ScoreLineView> lines, int total) {}
