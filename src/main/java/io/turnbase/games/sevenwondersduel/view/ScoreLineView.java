package io.turnbase.games.sevenwondersduel.view;

/** A named line of a final score; the name is a stable id the frontend labels. */
public record ScoreLineView(String line, int points) {}
