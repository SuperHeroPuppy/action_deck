package net.supersnetwork.actiondeck.recipe;

import net.minecraft.block.Block;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.supersnetwork.actiondeck.data.ActionDeckDeckDefinitions;
import net.supersnetwork.actiondeck.data.DeckDefinition;
import net.supersnetwork.actiondeck.item.DeckPackItem;

import java.util.Optional;

public class DeckPackRecipe extends SpecialCraftingRecipe {
	public DeckPackRecipe(Identifier id, CraftingRecipeCategory category) {
		super(id, category);
	}

	@Override
	public boolean matches(RecipeInputInventory inventory, World world) {
		return findDeck(inventory).isPresent();
	}

	@Override
	public ItemStack craft(RecipeInputInventory inventory, DynamicRegistryManager registryManager) {
		return findDeck(inventory)
			.map(deck -> DeckPackItem.create(deck.id()))
			.orElse(ItemStack.EMPTY);
	}

	private static Optional<DeckDefinition> findDeck(RecipeInputInventory inventory) {
		if (inventory.getWidth() != 3 || inventory.getHeight() != 3) {
			return Optional.empty();
		}

		for (int slot = 0; slot < inventory.size(); slot++) {
			if (slot == 4) {
				continue;
			}
			if (!inventory.getStack(slot).isOf(Items.PAPER)) {
				return Optional.empty();
			}
		}

		ItemStack center = inventory.getStack(4);
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
