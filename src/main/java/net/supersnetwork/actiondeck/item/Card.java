package net.supersnetwork.actiondeck.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.supersnetwork.actiondeck.block.ActionDeckBlocks;
import net.supersnetwork.actiondeck.block.DeckStackBlockEntity;
import net.supersnetwork.actiondeck.data.ActionDeckCardDefinitions;
import net.supersnetwork.actiondeck.data.ActionDeckDeckDefinitions;
import net.supersnetwork.actiondeck.data.CardDefinition;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * The Card item for the Action Deck mod.
 * This is the base item for action deck cards.
 * Cards can be assigned to specific decks and card variants via NBT data.
 * 
 * NBT Format:
 * - "deck": String - The deck name (e.g., "generic_gold")
 * - "card_number": Float - Card number (1-14, used for model predicate)
 * - "card_suit": Float - Card suit (1-5, used for model predicate)
 */
public class Card extends Item {
	public Card(Settings settings) {
		super(settings.maxCount(64));
	}

	@Override
	public Text getName(ItemStack stack) {
		NbtCompound nbt = stack.getNbt();
		Optional<CardDefinition> definition = getDefinition(nbt);
		if (definition.isPresent()) {
			return definition.get().name();
		}
		if (nbt != null && hasCardValue(nbt)) {
			return Text.literal("Card: ")
				.formatted(Formatting.WHITE)
				.append(getFormattedCardName(nbt));
		}
		return super.getName(stack);
	}

	@Override
	public void appendTooltip(ItemStack stack, World world, java.util.List<Text> tooltip, TooltipContext context) {
		super.appendTooltip(stack, world, tooltip, context);

		NbtCompound nbt = stack.getNbt();
		if (nbt == null) {
			return;
		}

		getDefinition(nbt).ifPresent(definition -> {
			definition.description().ifPresent(description -> tooltip.add(description.copy().formatted(Formatting.GRAY)));
			tooltip.add(Text.literal("Deck: ")
				.formatted(Formatting.GRAY)
				.append(getFormattedDeckText(definition.deck())));
			tooltip.add(Text.literal("Rank: ")
				.formatted(Formatting.GRAY)
				.append(definition.rank().display().copy().formatted(Formatting.WHITE)));
			tooltip.add(Text.literal("Suit: ")
				.formatted(Formatting.GRAY)
				.append(getFormattedSuitText(definition)));
		});

		String deck = getDeckName(nbt);
		if (deck != null) {
			tooltip.add(Text.literal("Deck: ")
				.formatted(Formatting.GRAY)
				.append(getFormattedDeckText(deck)));
		}

		if (hasCardValue(nbt)) {
			int cardNumber = Math.round(getCardNumber(nbt));
			int cardSuit = Math.round(getCardSuit(nbt));
			String cardName = getCardName(cardNumber, cardSuit);
			SuitInfo suit = SuitInfo.fromId(cardSuit);

			tooltip.add(Text.literal("Card: ")
				.formatted(Formatting.GRAY)
				.append(Text.literal(cardName).formatted(Formatting.WHITE)));
			tooltip.add(Text.literal("Suit: ")
				.formatted(Formatting.GRAY)
				.append(Text.literal(suit.symbol + " " + suit.name).formatted(suit.formatting)));
		}
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		if (context.getHand() != Hand.MAIN_HAND || context.getPlayer() == null) {
			return ActionResult.PASS;
		}

		if (context.getWorld().getBlockState(context.getBlockPos()).isOf(ActionDeckBlocks.DECK_STACK)) {
			return ActionResult.PASS;
		}

		Optional<Identifier> cardId = getCardId(context.getStack());
		if (cardId.isEmpty()) {
			return ActionResult.PASS;
		}

		return placeSingleCardDeck(context, cardId.get());
	}

	private static ActionResult placeSingleCardDeck(ItemUsageContext context, Identifier cardId) {
		World world = context.getWorld();
		ItemStack deckStack = new ItemStack(ActionDeckBlocks.DECK_STACK);
		DeckStackBlockEntity.writeCardsToStack(deckStack, List.of(cardId), false);
		ItemPlacementContext placementContext = new ItemPlacementContext(
			context.getPlayer(),
			context.getHand(),
			deckStack,
			new BlockHitResult(context.getHitPos(), context.getSide(), context.getBlockPos(), context.hitsInsideBlock())
		);

		if (!placementContext.canPlace()) {
			return ActionResult.FAIL;
		}

		BlockState placementState = ActionDeckBlocks.DECK_STACK.getPlacementState(placementContext);
		if (placementState == null) {
			return ActionResult.FAIL;
		}

		BlockPos placementPos = placementContext.getBlockPos();
		if (!world.canPlace(placementState, placementPos, ShapeContext.absent())) {
			return ActionResult.FAIL;
		}

		if (world.isClient) {
			return ActionResult.SUCCESS;
		}

		if (!world.setBlockState(placementPos, placementState, Block.NOTIFY_ALL)) {
			return ActionResult.FAIL;
		}

		ActionDeckBlocks.DECK_STACK.onPlaced(world, placementPos, placementState, context.getPlayer(), deckStack);
		BlockSoundGroup soundGroup = placementState.getSoundGroup();
		world.playSound(null, placementPos, soundGroup.getPlaceSound(), SoundCategory.BLOCKS, (soundGroup.getVolume() + 1.0f) / 2.0f, soundGroup.getPitch() * 0.8f);

		if (!context.getPlayer().getAbilities().creativeMode) {
			context.getStack().decrement(1);
		}

		return ActionResult.success(false);
	}

	public static Optional<CardDefinition> getDefinition(NbtCompound nbt) {
		if (nbt == null || !nbt.contains(ActionDeckCardDefinitions.CARD_ID_KEY)) {
			return Optional.empty();
		}

		try {
			return ActionDeckCardDefinitions.get(new Identifier(nbt.getString(ActionDeckCardDefinitions.CARD_ID_KEY)));
		} catch (Exception ignored) {
			return Optional.empty();
		}
	}

	public static Optional<Identifier> getCardId(ItemStack stack) {
		NbtCompound nbt = stack.getNbt();
		if (nbt == null || !nbt.contains(ActionDeckCardDefinitions.CARD_ID_KEY)) {
			return Optional.empty();
		}

		try {
			return Optional.of(new Identifier(nbt.getString(ActionDeckCardDefinitions.CARD_ID_KEY)));
		} catch (Exception ignored) {
			return Optional.empty();
		}
	}

	private static String getDeckName(NbtCompound nbt) {
		if (nbt.contains("deck")) {
			return nbt.getString("deck");
		}
		if (nbt.contains("Deck")) {
			return nbt.getString("Deck");
		}
		return null;
	}

	private static boolean hasCardValue(NbtCompound nbt) {
		return (nbt.contains("card_number") || nbt.contains("action_deck:card_number"))
			&& (nbt.contains("card_suit") || nbt.contains("action_deck:card_suit"));
	}

	private static float getCardNumber(NbtCompound nbt) {
		if (nbt.contains("card_number")) {
			return nbt.getFloat("card_number");
		}
		return nbt.getFloat("action_deck:card_number");
	}

	private static float getCardSuit(NbtCompound nbt) {
		if (nbt.contains("card_suit")) {
			return nbt.getFloat("card_suit");
		}
		return nbt.getFloat("action_deck:card_suit");
	}

	private static String getCardName(int cardNumber, int cardSuit) {
		if (cardSuit >= 5 || cardSuit < 1) {
			return cardNumber >= 1 && cardNumber <= 14 ? Integer.toString(cardNumber) : "Unknown";
		}
		return switch (cardNumber) {
			case 1 -> "Ace";
			case 11 -> "Jack";
			case 12 -> "Queen";
			case 13 -> "King";
			case 14 -> "Custom";
			default -> cardNumber >= 2 && cardNumber <= 10 ? Integer.toString(cardNumber) : "Unknown";
		};
	}

	private static Text getFormattedCardName(NbtCompound nbt) {
		int cardNumber = Math.round(getCardNumber(nbt));
		int cardSuit = Math.round(getCardSuit(nbt));
		String cardName = getCardName(cardNumber, cardSuit);
		SuitInfo suit = SuitInfo.fromId(cardSuit);
		return Text.literal(cardName + " of " + suit.name + " " + suit.symbol)
			.formatted(suit.formatting);
	}

	private static Text getFormattedDeckText(Identifier deckId) {
		MutableText deckText = Text.literal(toTitleCase(deckId.getNamespace()) + ": ")
			.formatted(Formatting.GOLD);
		Text deckName = ActionDeckDeckDefinitions.get(deckId)
			.map(deckDefinition -> deckDefinition.name().copy())
			.orElse(Text.literal(toTitleCase(deckId.getPath())));
		return deckText.append(deckName.copy().formatted(Formatting.GOLD));
	}

	private static Text getFormattedDeckText(String deckValue) {
		try {
			return getFormattedDeckText(new Identifier(deckValue));
		} catch (Exception ignored) {
			return Text.literal(toTitleCase(deckValue)).formatted(Formatting.GOLD);
		}
	}

	private static Text getFormattedSuitText(CardDefinition definition) {
		MutableText suitText = definition.suit().display().copy().formatted(Formatting.WHITE);
		definition.suit().symbol().ifPresent(symbol -> suitText.append(Text.literal(" " + symbol).formatted(Formatting.WHITE)));
		return suitText;
	}

	private static String toTitleCase(String value) {
		String[] words = value.toLowerCase(Locale.ROOT).split("[_\\-\\s]+");
		StringBuilder title = new StringBuilder();
		for (String word : words) {
			if (word.isEmpty()) {
				continue;
			}
			if (title.length() > 0) {
				title.append(' ');
			}
			title.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
		}
		return title.toString();
	}

	private record SuitInfo(String name, String symbol, Formatting formatting) {
		private static SuitInfo fromId(int cardSuit) {
			return switch (cardSuit) {
				case 1 -> new SuitInfo("Spades", "\u2660", Formatting.DARK_GRAY);
				case 2 -> new SuitInfo("Diamonds", "\u2666", Formatting.RED);
				case 3 -> new SuitInfo("Clubs", "\u2663", Formatting.DARK_GREEN);
				case 4 -> new SuitInfo("Hearts", "\u2665", Formatting.RED);
				case 5 -> new SuitInfo("Custom", "\u2726", Formatting.LIGHT_PURPLE);
				default -> new SuitInfo("Unknown", "?", Formatting.GRAY);
			};
		}
	}

	/**
	 * Creates a card with the specified deck type.
	 * This creates a basic card without a specific suit/number.
	 */
	public static ItemStack createCard(String deckType) {
		ItemStack stack = new ItemStack(ActionDeckItems.CARD);
		NbtCompound nbt = new NbtCompound();
		nbt.putString("deck", deckType);
		
		stack.setNbt(nbt);
		return stack;
	}

	/**
	 * Creates a card with specified deck, suit, and number.
	 * 
	 * @param deckType The deck name (e.g., "generic_gold")
	 * @param cardNumber The card number (1-14: 1=Ace, 2-10=Number, 11=Jack, 12=Queen, 13=King, 14=Custom)
	 * @param cardSuit The card suit (1=Spade, 2=Diamond, 3=Club, 4=Heart, 5=Custom)
	 * @return ItemStack with all NBT data set
	 */
	public static ItemStack createCard(String deckType, float cardNumber, float cardSuit) {
		ItemStack stack = createCard(deckType);
		NbtCompound nbt = stack.getOrCreateNbt();
		nbt.putFloat("card_number", cardNumber);
		nbt.putFloat("card_suit", cardSuit);
		return stack;
	}

	public static ItemStack createCard(Identifier cardId) {
		ItemStack stack = new ItemStack(ActionDeckItems.CARD);
		NbtCompound nbt = new NbtCompound();
		nbt.putString(ActionDeckCardDefinitions.CARD_ID_KEY, cardId.toString());
		stack.setNbt(nbt);
		return stack;
	}
}
