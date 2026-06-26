package net.supersnetwork.actiondeck.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.supersnetwork.actiondeck.data.ActionDeckDeckDefinitions;
import net.supersnetwork.actiondeck.data.DeckDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DeckPackItem extends Item {
	public static final String DECK_ID_KEY = "action_deck:deck_id";
	public static final int CARDS_PER_PACK = 5;

	public DeckPackItem(Settings settings) {
		super(settings.maxCount(64));
	}

	@Override
	public Text getName(ItemStack stack) {
		Optional<DeckDefinition> deck = getDeck(stack);
		if (deck.isPresent()) {
			return Text.translatable("item.action_deck.deck_pack.named", deck.get().name());
		}
		return super.getName(stack);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		super.appendTooltip(stack, context, tooltip, type);
		getDeck(stack).ifPresent(deck -> {
			deck.description().ifPresent(description -> tooltip.add(description.copy().formatted(Formatting.GRAY)));
			tooltip.add(Text.translatable("item.action_deck.deck_pack.contains", CARDS_PER_PACK)
				.formatted(Formatting.GRAY));
		});
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		Optional<DeckDefinition> deck = getDeck(stack);
		if (deck.isEmpty() || deck.get().cards().isEmpty()) {
			return TypedActionResult.fail(stack);
		}

		if (!world.isClient) {
			world.playSound(
				null,
				user.getX(),
				user.getY(),
				user.getZ(),
				SoundEvents.ITEM_BOOK_PAGE_TURN,
				SoundCategory.PLAYERS,
				0.9f,
				1.2f
			);

			List<Identifier> availableCards = new ArrayList<>(deck.get().cards());
			for (int i = 0; i < CARDS_PER_PACK; i++) {
				if (availableCards.isEmpty()) {
					availableCards.addAll(deck.get().cards());
				}
				Identifier cardId = availableCards.remove(world.random.nextInt(availableCards.size()));
				ItemStack card = Card.createCard(cardId);
				if (!user.getInventory().insertStack(card)) {
					user.dropItem(card, false);
				}
			}

			if (!user.getAbilities().creativeMode) {
				stack.decrement(1);
			}
			user.incrementStat(Stats.USED.getOrCreateStat(this));
		}

		return TypedActionResult.success(stack, world.isClient);
	}

	public static ItemStack create(Identifier deckId) {
		ItemStack stack = new ItemStack(ActionDeckItems.DECK_PACK);
		NbtCompound nbt = new NbtCompound();
		nbt.putString(DECK_ID_KEY, deckId.toString());
		ActionDeckStackData.set(stack, nbt);
		return stack;
	}

	public static Optional<Identifier> getDeckId(ItemStack stack) {
		NbtCompound nbt = ActionDeckStackData.get(stack);
		if (nbt == null || !nbt.contains(DECK_ID_KEY)) {
			return Optional.empty();
		}

		try {
			return Optional.of(Identifier.of(nbt.getString(DECK_ID_KEY)));
		} catch (Exception ignored) {
			return Optional.empty();
		}
	}

	private static Optional<DeckDefinition> getDeck(ItemStack stack) {
		return getDeckId(stack).flatMap(ActionDeckDeckDefinitions::get);
	}
}
