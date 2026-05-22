package net.supersnetwork.actiondeck.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.supersnetwork.actiondeck.data.ActionDeckCardDefinitions;
import net.supersnetwork.actiondeck.data.CardDefinition;

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
				.append(Text.literal(definition.deck().toString()).formatted(Formatting.GOLD)));
			tooltip.add(Text.literal("Rank: ")
				.formatted(Formatting.GRAY)
				.append(definition.rank().display().copy().formatted(Formatting.WHITE)));
			tooltip.add(Text.literal("Suit: ")
				.formatted(Formatting.GRAY)
				.append(definition.suit().display().copy().formatted(Formatting.WHITE)));
		});

		String deck = getDeckName(nbt);
		if (deck != null) {
			tooltip.add(Text.literal("Deck: ")
				.formatted(Formatting.GRAY)
				.append(Text.literal(toTitleCase(deck)).formatted(Formatting.GOLD)));
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

	private static String toTitleCase(String value) {
		String[] words = value.toLowerCase(Locale.ROOT).split("[_\\s]+");
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
