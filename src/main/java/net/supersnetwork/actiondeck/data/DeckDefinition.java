package net.supersnetwork.actiondeck.data;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;

public record DeckDefinition(
	Identifier id,
	Text name,
	Optional<Text> description,
	Optional<Identifier> defaultBack,
	List<Identifier> cards
) {
}
