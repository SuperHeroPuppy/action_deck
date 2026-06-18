package net.supersnetwork.actiondeck.network;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.supersnetwork.actiondeck.ActionDeck;
import net.supersnetwork.actiondeck.data.ActionDeckCardDefinitions;
import net.supersnetwork.actiondeck.data.ActionDeckDeckDefinitions;
import net.supersnetwork.actiondeck.data.CardDefinition;
import net.supersnetwork.actiondeck.data.DeckDefinition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public final class ActionDeckNetworking {
	public static final Identifier SYNC_DEFINITIONS = new Identifier(ActionDeck.MOD_ID, "sync_definitions");

	private ActionDeckNetworking() {
	}

	public static void registerServerHandlers() {
		ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> sendDefinitions(player));
	}

	public static void sendDefinitions(ServerPlayerEntity player) {
		if (!ServerPlayNetworking.canSend(player, SYNC_DEFINITIONS)) {
			return;
		}

		ServerPlayNetworking.send(player, SYNC_DEFINITIONS, writeDefinitions());
	}

	public static PacketByteBuf writeDefinitions() {
		PacketByteBuf buffer = PacketByteBufs.create();
		writeCards(buffer, ActionDeckCardDefinitions.all());
		writeDecks(buffer, ActionDeckDeckDefinitions.all());
		return buffer;
	}

	public static SyncedDefinitions readDefinitions(PacketByteBuf buffer) {
		return new SyncedDefinitions(readCards(buffer), readDecks(buffer));
	}

	private static void writeCards(PacketByteBuf buffer, Collection<CardDefinition> cards) {
		buffer.writeVarInt(cards.size());
		for (CardDefinition card : cards) {
			buffer.writeIdentifier(card.id());
			writeText(buffer, card.name());
			writeOptionalText(buffer, card.description());
			buffer.writeIdentifier(card.deck());
			writeDisplayValue(buffer, card.rank());
			writeDisplayValue(buffer, card.suit());
			buffer.writeIdentifier(card.textures().front());
			buffer.writeIdentifier(card.textures().back());
			writeOptionalIdentifier(buffer, card.itemModel());
		}
	}

	private static List<CardDefinition> readCards(PacketByteBuf buffer) {
		int size = buffer.readVarInt();
		List<CardDefinition> cards = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			Identifier id = buffer.readIdentifier();
			Text name = readText(buffer);
			Optional<Text> description = readOptionalText(buffer);
			Identifier deck = buffer.readIdentifier();
			CardDefinition.DisplayValue rank = readDisplayValue(buffer);
			CardDefinition.DisplayValue suit = readDisplayValue(buffer);
			CardDefinition.Textures textures = new CardDefinition.Textures(buffer.readIdentifier(), buffer.readIdentifier());
			Optional<Identifier> itemModel = readOptionalIdentifier(buffer);
			cards.add(new CardDefinition(id, name, description, deck, rank, suit, textures, itemModel));
		}
		return List.copyOf(cards);
	}

	private static void writeDecks(PacketByteBuf buffer, Collection<DeckDefinition> decks) {
		buffer.writeVarInt(decks.size());
		for (DeckDefinition deck : decks) {
			buffer.writeIdentifier(deck.id());
			writeText(buffer, deck.name());
			writeOptionalText(buffer, deck.description());
			writeOptionalIdentifier(buffer, deck.defaultBack());
			buffer.writeVarInt(deck.cards().size());
			for (Identifier card : deck.cards()) {
				buffer.writeIdentifier(card);
			}
			buffer.writeBoolean(deck.deckPack().isPresent());
			deck.deckPack().ifPresent(deckPack -> {
				buffer.writeIdentifier(deckPack.craftingBlock());
				writeOptionalIdentifier(buffer, deckPack.texture());
			});
		}
	}

	private static List<DeckDefinition> readDecks(PacketByteBuf buffer) {
		int size = buffer.readVarInt();
		List<DeckDefinition> decks = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			Identifier id = buffer.readIdentifier();
			Text name = readText(buffer);
			Optional<Text> description = readOptionalText(buffer);
			Optional<Identifier> defaultBack = readOptionalIdentifier(buffer);
			int cardCount = buffer.readVarInt();
			List<Identifier> cards = new ArrayList<>(cardCount);
			for (int cardIndex = 0; cardIndex < cardCount; cardIndex++) {
				cards.add(buffer.readIdentifier());
			}
			Optional<DeckDefinition.DeckPack> deckPack = buffer.readBoolean()
				? Optional.of(new DeckDefinition.DeckPack(buffer.readIdentifier(), readOptionalIdentifier(buffer)))
				: Optional.empty();
			decks.add(new DeckDefinition(id, name, description, defaultBack, List.copyOf(cards), deckPack));
		}
		return List.copyOf(decks);
	}

	private static void writeDisplayValue(PacketByteBuf buffer, CardDefinition.DisplayValue value) {
		buffer.writeString(value.id());
		writeText(buffer, value.display());
		writeOptionalString(buffer, value.symbol());
	}

	private static CardDefinition.DisplayValue readDisplayValue(PacketByteBuf buffer) {
		return new CardDefinition.DisplayValue(buffer.readString(32767), readText(buffer), readOptionalString(buffer));
	}

	private static void writeText(PacketByteBuf buffer, Text text) {
		buffer.writeString(Text.Serializer.toJson(text));
	}

	private static Text readText(PacketByteBuf buffer) {
		Text text = Text.Serializer.fromJson(buffer.readString(32767));
		return text == null ? Text.literal("") : text;
	}

	private static void writeOptionalText(PacketByteBuf buffer, Optional<Text> text) {
		buffer.writeBoolean(text.isPresent());
		text.ifPresent(value -> writeText(buffer, value));
	}

	private static Optional<Text> readOptionalText(PacketByteBuf buffer) {
		return buffer.readBoolean() ? Optional.of(readText(buffer)) : Optional.empty();
	}

	private static void writeOptionalIdentifier(PacketByteBuf buffer, Optional<Identifier> identifier) {
		buffer.writeBoolean(identifier.isPresent());
		identifier.ifPresent(buffer::writeIdentifier);
	}

	private static Optional<Identifier> readOptionalIdentifier(PacketByteBuf buffer) {
		return buffer.readBoolean() ? Optional.of(buffer.readIdentifier()) : Optional.empty();
	}

	private static void writeOptionalString(PacketByteBuf buffer, Optional<String> value) {
		buffer.writeBoolean(value.isPresent());
		value.ifPresent(buffer::writeString);
	}

	private static Optional<String> readOptionalString(PacketByteBuf buffer) {
		return buffer.readBoolean() ? Optional.of(buffer.readString(32767)) : Optional.empty();
	}

	public record SyncedDefinitions(List<CardDefinition> cards, List<DeckDefinition> decks) {
	}
}
