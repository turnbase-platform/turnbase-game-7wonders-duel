package io.turnbase.games.sevenwondersduel.wire;

import java.util.UUID;

/**
 * What a client sends: one flat record, because the GDK has no polymorphic deserialization. Which
 * fields are set depends on {@code type}; {@link WireActions} checks.
 *
 * @param type {@code draftWonder}, {@code chooseStarter}, {@code discard}, {@code build}, {@code
 *     buildWonder}, {@code answerComponent} or {@code answerPlayer}
 * @param component the card (or Wonder, or token) acted on
 * @param wonder the Wonder to build, for {@code buildWonder}
 * @param player the player named, for {@code chooseStarter} and {@code answerPlayer}
 */
public record WireAction(String type, String component, String wonder, UUID player) {}
