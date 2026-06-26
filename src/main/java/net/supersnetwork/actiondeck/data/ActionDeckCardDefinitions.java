package net.supersnetwork.actiondeck.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.supersnetwork.actiondeck.ActionDeck;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class ActionDeckCardDefinitions implements SimpleSynchronousResourceReloadListener {
	public static final String CARD_ID_KEY = "action_deck:card_id";
	public static final Identifier RELOAD_ID = Identifier.of(ActionDeck.MOD_ID, "cards");
	public static final Identifier DEFAULT_DECK = Identifier.of(ActionDeck.MOD_ID, "unknown");
	public static final Identifier DEFAULT_FRONT_TEXTURE = Identifier.of(ActionDeck.MOD_ID, "item/card_generic_gold");
	public static final Identifier DEFAULT_BACK_TEXTURE = Identifier.of(ActionDeck.MOD_ID, "item/card_generic_gold_back");
	public static final Identifier DEFAULT_ITEM_MODEL = Identifier.of(ActionDeck.MOD_ID, "item/card");

	private static final String ROOT_PATH = "action_deck/cards";
	private static final String CARD_FILE = "/card.json";
	private static volatile Map<Identifier, CardDefinition> cards = Map.of();

	private ActionDeckCardDefinitions() {
	}

	public static void register() {
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new ActionDeckCardDefinitions());
	}

	@Override
	public Identifier getFabricId() {
		return RELOAD_ID;
	}

	@Override
	public void reload(ResourceManager manager) {
		Map<Identifier, CardDefinition> loaded = new LinkedHashMap<>();

		manager.findResources(ROOT_PATH, id -> id.getPath().endsWith(CARD_FILE)).forEach((resourceId, resource) -> {
			try (BufferedReader reader = resource.getReader()) {
				JsonElement element = JsonParser.parseReader(reader);
				Identifier cardId = cardIdFromResource(resourceId);
				loaded.put(cardId, parse(cardId, JsonHelper.asObject(element, "card definition")));
			} catch (Exception exception) {
				ActionDeck.LOGGER.warn("Failed to load card definition {}", resourceId, exception);
			}
		});

		replaceCards(loaded.values());

		ActionDeck.LOGGER.info("Loaded {} Action Deck card definitions", cards.size());
	}

	public static Collection<CardDefinition> all() {
		return cards.values();
	}

	public static Optional<CardDefinition> get(Identifier id) {
		return Optional.ofNullable(cards.get(id));
	}

	public static boolean contains(Identifier id) {
		return cards.containsKey(id);
	}

	public static void applySynced(Collection<CardDefinition> cards) {
		replaceCards(cards);
	}

	private static void replaceCards(Collection<CardDefinition> cards) {
		Map<Identifier, CardDefinition> replacement = new LinkedHashMap<>();
		cards.stream()
			.sorted(Comparator.comparing(definition -> definition.id().toString()))
			.forEach(definition -> replacement.put(definition.id(), definition));
		ActionDeckCardDefinitions.cards = Collections.unmodifiableMap(replacement);
	}

	private static CardDefinition parse(Identifier id, JsonObject json) {
		return new CardDefinition(
			id,
			parseText(json, "name").orElse(Text.literal(titleCase(id.getPath()))),
			parseText(json, "description"),
			parseIdentifier(json, "deck").orElse(DEFAULT_DECK),
			parseDisplayValue(json, "rank"),
			parseDisplayValue(json, "suit"),
			parseTextures(json),
			parseIdentifier(json, "item_model").or(() -> Optional.of(DEFAULT_ITEM_MODEL))
		);
	}

	private static CardDefinition.Textures parseTextures(JsonObject json) {
		if (json.has("textures")) {
			JsonObject textures = JsonHelper.getObject(json, "textures");
			return new CardDefinition.Textures(
				parseIdentifier(textures, "front").orElse(DEFAULT_FRONT_TEXTURE),
				parseIdentifier(textures, "back").orElse(DEFAULT_BACK_TEXTURE)
			);
		}

		return new CardDefinition.Textures(
			parseIdentifier(json, "front_texture")
				.or(() -> parseIdentifier(json, "texture"))
				.orElse(DEFAULT_FRONT_TEXTURE),
			parseIdentifier(json, "back_texture").orElse(DEFAULT_BACK_TEXTURE)
		);
	}

	private static CardDefinition.DisplayValue parseDisplayValue(JsonObject json, String key) {
		if (!json.has(key)) {
			return new CardDefinition.DisplayValue("unknown", Text.literal("Unknown"), Optional.empty());
		}

		JsonElement element = json.get(key);
		if (element.isJsonPrimitive()) {
			String value = element.getAsString();
			return new CardDefinition.DisplayValue(value, Text.literal(titleCase(value)), Optional.empty());
		}

		JsonObject object = JsonHelper.asObject(element, key);
		String id = JsonHelper.getString(object, "id", "unknown");
		Text display = parseText(object, "display").orElse(Text.literal(titleCase(id)));
		Optional<String> symbol = parseString(object, "symbol");
		return new CardDefinition.DisplayValue(id, display, symbol);
	}

	private static Identifier cardIdFromResource(Identifier resourceId) throws IOException {
		String path = resourceId.getPath();
		if (!path.startsWith(ROOT_PATH + "/") || !path.endsWith(CARD_FILE)) {
			throw new IOException("Invalid card definition path: " + resourceId);
		}

		String cardPath = path.substring((ROOT_PATH + "/").length(), path.length() - CARD_FILE.length());
		if (cardPath.isBlank() || cardPath.contains("/")) {
			throw new IOException("Card definitions must be in " + ROOT_PATH + "/<card_id>/card.json: " + resourceId);
		}

		return Identifier.of(resourceId.getNamespace(), cardPath);
	}

	static Optional<Text> parseText(JsonObject json, String key) {
		if (!json.has(key)) {
			return Optional.empty();
		}

		JsonElement element = json.get(key);
		if (element.isJsonPrimitive()) {
			return Optional.of(Text.literal(element.getAsString()));
		}
		return TextCodecs.CODEC.parse(JsonOps.INSTANCE, element).result();
	}

	static Optional<Identifier> parseIdentifier(JsonObject json, String key) {
		return parseString(json, key).map(Identifier::of);
	}

	static Optional<String> parseString(JsonObject json, String key) {
		if (!json.has(key)) {
			return Optional.empty();
		}
		return Optional.of(JsonHelper.getString(json, key));
	}

	static String titleCase(String value) {
		StringBuilder result = new StringBuilder();
		for (String part : value.split("[_\\-]+")) {
			if (part.isEmpty()) {
				continue;
			}
			if (result.length() > 0) {
				result.append(' ');
			}
			result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
		}
		return result.toString();
	}
}
