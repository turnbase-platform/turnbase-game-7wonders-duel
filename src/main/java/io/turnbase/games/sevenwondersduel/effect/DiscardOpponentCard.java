package io.turnbase.games.sevenwondersduel.effect;

import io.turnbase.games.sevenwondersduel.agenda.ComponentOption;
import io.turnbase.games.sevenwondersduel.agenda.Option;
import io.turnbase.games.sevenwondersduel.catalog.Tag;
import io.turnbase.games.sevenwondersduel.id.PlayerId;
import io.turnbase.games.sevenwondersduel.zone.Placements;
import io.turnbase.games.sevenwondersduel.zone.Spots;
import java.util.List;

/**
 * The owner picks one card of the opponent's city that carries {@code tag} (a colour) and discards
 * it. With no such card nothing happens (B-EFF-3).
 */
public record DiscardOpponentCard(Tag tag) implements Effect {

  @Override
  public void apply(Resolution resolution, PlayerId owner) {
    PlayerId opponent = resolution.state().opponentOf(owner);
    List<Option> targets =
        Placements.idsIn(resolution.state().placements(), Spots.city(opponent)).stream()
            .filter(id -> resolution.rules().catalog().get(id).tags().contains(tag))
            .<Option>map(ComponentOption::new)
            .toList();
    resolution.decide(owner, targets);
  }

  @Override
  public void resume(Resolution resolution, PlayerId owner, Option answer) {
    resolution.move(((ComponentOption) answer).component(), Spots.DISCARD);
  }

  @Override
  public List<Badge> badges() {
    return List.of(Badge.of("discardOpponent", 0, tag.value()));
  }
}
