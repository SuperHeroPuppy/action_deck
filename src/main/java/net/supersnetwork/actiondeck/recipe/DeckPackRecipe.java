package net.supersnetwork.actiondeck.recipe;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.supersnetwork.actiondeck.data.ActionDeckDeckDefinitions;
import net.supersnetwork.actiondeck.data.DeckDefinition;
import net.supersnetwork.actiondeck.item.DeckPackItem;

import java.util.Optional;

public class DeckPackRecipe extends SpecialCraftingRecipe {
	public DeckPackRecipe(CraftingRecipeCategory category) {
		super(category);
	}

	@Override
	public boolean matches(CraftingRecipeInput inventory, World world) {
		return findDeck(inventory).isPresent();
	}

	@Override
	public ItemStack craft(CraftingRecipeInput inventory, RegistryWrapper.WrapperLookup registryLookup) {
		return findDeck(inventory)
			.map(deck -> DeckPackItem.create(deck.id()))
			.orElse(ItemStack.EMPTY);
	}

	private static Optional<DeckDefinition> findDeck(CraftingRecipeInput inventory) {
		if (inventory.getWidth() != 3 || inventory.getHeight() != 3) {
			return Optional.empty();
		}

		for (int slot = 0; slot < inventory.getSize(); slot++) {
			if (slot == 4) {
				continue;
			}
			if (!inventory.getStackInSlot(slot).isOf(Items.PAPER)) {
				return Optional.empty();
			}
		}

		ItemStack center = inventory.getStackInSlot(4);
		if (center.isEmpty()) {
			return Optional.empty();
		}

		return ActionDeckDeckDefinitions.all().stream()
			.filter(deck -> deck.deckPack().isPresent())
			.filter(deck -> {
				Identifier blockId = deck.deckPack().orElseThrow().craftingBlock();
				Block block = Registries.BLOCK.get(blockId);
				return block != net.minecraft.block.Blocks.AIR && center.isOf(block.asItem());
			})
			.findFirst();
	}

	@Override
	public boolean fits(int width, int height) {
		return width >= 3 && height >= 3;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return ActionDeckRecipes.DECK_PACK;
	}
}
