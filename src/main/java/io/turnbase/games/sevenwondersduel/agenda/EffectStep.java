package io.turnbase.games.sevenwondersduel.agenda;

import io.turnbase.games.sevenwondersduel.event.EffectRef;
import io.turnbase.games.sevenwondersduel.id.PlayerId;

/**
 * An instant effect still to be applied.
 *
 * @param effect which effect of which component
 * @param owner the player who acquired the effect's source
 */
public record EffectStep(EffectRef effect, PlayerId owner) implements Step {}
