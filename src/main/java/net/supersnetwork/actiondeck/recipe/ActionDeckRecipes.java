package net.supersnetwork.actiondeck.recipe;

import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.supersnetwork.actiondeck.ActionDeck;

public class ActionDeckRecipes {
	public static final SpecialRecipeSerializer<DeckStackRecipe> DECK_STACK = Registry.register(
		Registries.RECIPE_SERIALIZER,
		new Identifier(ActionDeck.MOD_ID, "deck_stack"),
		new SpecialRecipeSerializer<>(DeckStackRecipe::new)
	);
	public static final SpecialRecipeSerializer<DeckPackRecipe> DECK_PACK = Registry.register(
		Registries.RECIPE_SERIALIZER,
		new Identifier(ActionDeck.MOD_ID, "deck_pack"),
		new SpecialRecipeSerializer<>(DeckPackRecipe::new)
	);

	public static void register() {
	}
}
