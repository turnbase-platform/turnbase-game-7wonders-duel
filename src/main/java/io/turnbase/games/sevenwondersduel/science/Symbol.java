package io.turnbase.games.sevenwondersduel.science;

import io.turnbase.games.sevenwondersduel.effect.Modifier;

/** The component shows a scientific symbol while it is in its owner's city (B-SCI-2). */
public record Symbol(ScienceSymbol symbol) implements Modifier {}
