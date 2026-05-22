package net.supersnetwork.actiondeck.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.supersnetwork.actiondeck.ActionDeck;
import net.supersnetwork.actiondeck.block.ActionDeckBlocks;
import net.supersnetwork.actiondeck.block.DeckStackBlockEntity;
import net.supersnetwork.actiondeck.data.ActionDeckCardDefinitions;
import net.supersnetwork.actiondeck.data.ActionDeckDeckDefinitions;
import net.supersnetwork.actiondeck.data.CardDefinition;
import net.supersnetwork.actiondeck.data.DeckDefinition;
import net.supersnetwork.actiondeck.item.Card;

import java.util.stream.Stream;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class ActionDeckCommands {
	private static final DynamicCommandExceptionType INVALID_CARD = new DynamicCommandExceptionType(
		value -> Text.literal("Unknown card: " + value)
	);
	private static final DynamicCommandExceptionType INVALID_DECK = new DynamicCommandExceptionType(
		value -> Text.literal("Unknown deck: " + value)
	);

	private ActionDeckCommands() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(literal("actiondeck")
				.requires(source -> source.hasPermissionLevel(2))
				.then(literal("card")
					.then(literal("give")
						.then(argument("card", IdentifierArgumentType.identifier())
							.suggests((context, builder) -> CommandSource.suggestMatching(cardSuggestions(), builder))
							.executes(context -> giveCard(
								context.getSource(),
								IdentifierArgumentType.getIdentifier(context, "card")
							)))))
				.then(literal("deck")
					.then(literal("give")
						.then(argument("deck", IdentifierArgumentType.identifier())
							.suggests((context, builder) -> CommandSource.suggestMatching(deckSuggestions(), builder))
							.executes(context -> giveDeck(
								context.getSource(),
								IdentifierArgumentType.getIdentifier(context, "deck")
							))))));

			dispatcher.register(literal("card")
				.requires(source -> source.hasPermissionLevel(2))
				.then(literal("give")
					.then(argument("card", IdentifierArgumentType.identifier())
						.suggests((context, builder) -> CommandSource.suggestMatching(cardSuggestions(), builder))
						.executes(context -> giveCard(
							context.getSource(),
							IdentifierArgumentType.getIdentifier(context, "card")
						)))));
		});
	}

	private static int giveCard(ServerCommandSource source, Identifier cardId) throws CommandSyntaxException {
		ServerPlayerEntity player = source.getPlayerOrThrow();
		validateCardId(cardId);
		ItemStack stack = Card.createCard(cardId);

		if (!player.getInventory().insertStack(stack)) {
			player.dropItem(stack, false);
		}

		source.sendFeedback(() -> Text.literal("Gave " + player.getName().getString() + " " + cardId + "."), false);
		return 1;
	}

	private static int giveDeck(ServerCommandSource source, Identifier deckId) throws CommandSyntaxException {
		ServerPlayerEntity player = source.getPlayerOrThrow();
		DeckDefinition deck = parseDeck(deckId);
		ItemStack stack = new ItemStack(ActionDeckBlocks.DECK_STACK);
		DeckStackBlockEntity.writeCardsToStack(stack, deck.cards());

		if (!player.getInventory().insertStack(stack)) {
			player.dropItem(stack, false);
		}

		source.sendFeedback(() -> Text.literal("Gave " + player.getName().getString() + " deck " + deck.id() + "."), false);
		return 1;
	}

	private static void validateCardId(Identifier id) throws CommandSyntaxException {
		if (ActionDeckCardDefinitions.contains(id)) {
			return;
		}
		throw INVALID_CARD.create(id);
	}

	private static DeckDefinition parseDeck(Identifier id) throws CommandSyntaxException {
		return ActionDeckDeckDefinitions.get(id).orElseThrow(() -> INVALID_DECK.create(id));
	}

	private static Stream<String> cardSuggestions() {
		return ActionDeckCardDefinitions.all().stream()
			.map(CardDefinition::id)
			.flatMap(ActionDeckCommands::suggestIdentifier);
	}

	private static Stream<String> deckSuggestions() {
		return ActionDeckDeckDefinitions.all().stream()
			.map(DeckDefinition::id)
			.flatMap(ActionDeckCommands::suggestIdentifier);
	}

	private static Stream<String> suggestIdentifier(Identifier id) {
		return Stream.of(id.toString());
	}
}
