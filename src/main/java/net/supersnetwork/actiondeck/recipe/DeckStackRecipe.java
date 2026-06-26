package net.supersnetwork.actiondeck.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.supersnetwork.actiondeck.block.ActionDeckBlocks;
import net.supersnetwork.actiondeck.block.DeckStackBlockEntity;
import net.supersnetwork.actiondeck.item.Card;

import java.util.ArrayList;
import java.util.List;

public class DeckStackRecipe extends SpecialCraftingRecipe {
	public DeckStackRecipe(CraftingRecipeCategory category) {
		super(category);
	}

	@Override
	public boolean matches(CraftingRecipeInput inventory, World world) {
		int cards = 0;
		int inputs = 0;
		int decks = 0;
		int faceDownDecks = 0;

		for (int i = 0; i < inventory.getSize(); i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (stack.isEmpty()) {
				continue;
			}

			if (DeckStackBlockEntity.isCard(stack)) {
				cards++;
				inputs++;
			} else if (DeckStackBlockEntity.isDeckStack(stack)) {
				decks++;
				if (DeckStackBlockEntity.isFaceDown(stack)) {
					faceDownDecks++;
				}
				cards += DeckStackBlockEntity.readCardsFromStack(stack).size();
				inputs++;
			} else {
				return false;
			}
		}

		if (inputs == 0) {
			return false;
		}

		if (inputs == 1 && decks == 1) {
			return true;
		}

		if (faceDownDecks > 0) {
			return false;
		}

		return cards > 0;
	}

	@Override
	public ItemStack craft(CraftingRecipeInput inventory, RegistryWrapper.WrapperLookup registryLookup) {
		ItemStack singleDeck = getSingleDeckInput(inventory);
		if (!singleDeck.isEmpty()) {
			ItemStack result = new ItemStack(ActionDeckBlocks.DECK_STACK);
			DeckStackBlockEntity.writeCardsToStack(
				result,
				DeckStackBlockEntity.readCardsFromStack(singleDeck),
				!DeckStackBlockEntity.isFaceDown(singleDeck)
			);
			return result;
		}

		List<Identifier> cards = new ArrayList<>();

		for (int i = 0; i < inventory.getSize(); i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (stack.isEmpty()) {
				continue;
			}

			if (DeckStackBlockEntity.isCard(stack)) {
				Card.getCardId(stack).ifPresent(cards::add);
			} else if (DeckStackBlockEntity.isDeckStack(stack)) {
				cards.addAll(DeckStackBlockEntity.readCardsFromStack(stack));
			}
		}

		ItemStack result = new ItemStack(ActionDeckBlocks.DECK_STACK);
		DeckStackBlockEntity.writeCardsToStack(result, cards, false);
		return result;
	}

	private static ItemStack getSingleDeckInput(CraftingRecipeInput inventory) {
		ItemStack deckStack = ItemStack.EMPTY;

		for (int i = 0; i < inventory.getSize(); i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (stack.isEmpty()) {
				continue;
			}
			if (!DeckStackBlockEntity.isDeckStack(stack)) {
				return ItemStack.EMPTY;
			}
			if (!deckStack.isEmpty()) {
				return ItemStack.EMPTY;
			}
			deckStack = stack;
		}

		return deckStack;
	}

	@Override
	public boolean fits(int width, int height) {
		return width * height >= 1;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return ActionDeckRecipes.DECK_STACK;
	}
}
