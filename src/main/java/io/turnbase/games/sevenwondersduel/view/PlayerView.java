package io.turnbase.games.sevenwondersduel.view;

import java.util.UUID;

/** One seat's public scalars. */
public record PlayerView(UUID id, int coins, boolean playAgain) {}
