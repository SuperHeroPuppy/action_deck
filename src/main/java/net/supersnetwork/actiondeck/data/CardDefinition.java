package net.supersnetwork.actiondeck.data;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Optional;

public record CardDefinition(
	Identifier id,
	Text name,
	Optional<Text> description,
	Identifier deck,
	DisplayValue rank,
	DisplayValue suit,
	Textures textures,
	Optional<Identifier> itemModel
) {
	public record DisplayValue(String id, Text display, Optional<String> symbol) {
	}

	public record Textures(Identifier front, Identifier back) {
	}
}
