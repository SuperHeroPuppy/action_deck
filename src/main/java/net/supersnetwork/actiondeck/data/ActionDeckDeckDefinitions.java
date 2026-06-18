package net.supersnetwork.actiondeck.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.supersnetwork.actiondeck.ActionDeck;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ActionDeckDeckDefinitions implements SimpleSynchronousResourceReloadListener {
	public static final Identifier RELOAD_ID = new Identifier(ActionDeck.MOD_ID, "decks");

	private static final String ROOT_PATH = "action_deck/decks";
	private static final String DECK_FILE = ".json";
	private static final Map<Identifier, DeckDefinition> DECKS = new LinkedHashMap<>();

	private ActionDeckDeckDefinitions() {
	}

	public static void register() {
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new ActionDeckDeckDefinitions());
	}

	@Override
	public Identifier getFabricId() {
		return RELOAD_ID;
	}

	@Override
	public void reload(ResourceManager manager) {
		Map<Identifier, DeckDefinition> loaded = new LinkedHashMap<>();

		manager.findResources(ROOT_PATH, id -> id.getPath().endsWith(DECK_FILE)).forEach((resourceId, resource) -> {
			try (BufferedReader reader = resource.getReader()) {
				JsonElement element = JsonParser.parseReader(reader);
				Identifier deckId = deckIdFromResource(resourceId);
				loaded.put(deckId, parse(deckId, JsonHelper.asObject(element, "deck definition")));
			} catch (Exception exception) {
				ActionDeck.LOGGER.warn("Failed to load deck definition {}", resourceId, exception);
			}
		});

		replaceDecks(loaded.values());

		ActionDeck.LOGGER.info("Loaded {} Action Deck deck definitions", DECKS.size());
	}

	public static Collection<DeckDefinition> all() {
		return DECKS.values();
	}

	public static Optional<DeckDefinition> get(Identifier id) {
		return Optional.ofNullable(DECKS.get(id));
	}

	public static boolean contains(Identifier id) {
		return DECKS.containsKey(id);
	}

	public static void applySynced(Collection<DeckDefinition> decks) {
		replaceDecks(decks);
	}

	private static void replaceDecks(Collection<DeckDefinition> decks) {
		DECKS.clear();
		decks.stream()
			.sorted(Comparator.comparing(definition -> definition.id().toString()))
			.forEach(definition -> DECKS.put(definition.id(), definition));
	}

	private static DeckDefinition parse(Identifier id, JsonObject json) {
		return new DeckDefinition(
			id,
			ActionDeckCardDefinitions.parseText(json, "name").orElse(Text.literal(ActionDeckCardDefinitions.titleCase(id.getPath()))),
			ActionDeckCardDefinitions.parseText(json, "description"),
			ActionDeckCardDefinitions.parseIdentifier(json, "default_back"),
			parseCards(json),
			parseDeckPack(json)
		);
	}

	private static Optional<DeckDefinition.DeckPack> parseDeckPack(JsonObject json) {
		if (!json.has("deck_pack")) {
			return Optional.empty();
		}

		JsonObject deckPack = JsonHelper.getObject(json, "deck_pack");
		Identifier craftingBlock = new Identifier(JsonHelper.getString(deckPack, "crafting_block"));
		Optional<Identifier> texture = ActionDeckCardDefinitions.parseIdentifier(deckPack, "texture");
		return Optional.of(new DeckDefinition.DeckPack(craftingBlock, texture));
	}

	private static List<Identifier> parseCards(JsonObject json) {
		List<Identifier> cards = new ArrayList<>();
		JsonArray array = JsonHelper.getArray(json, "cards", new JsonArray());
		for (JsonElement element : array) {
			cards.add(new Identifier(JsonHelper.asString(element, "card id")));
		}
		return List.copyOf(cards);
	}

	private static Identifier deckIdFromResource(Identifier resourceId) throws IOException {
		String path = resourceId.getPath();
		if (!path.startsWith(ROOT_PATH + "/") || !path.endsWith(DECK_FILE)) {
			throw new IOException("Invalid deck definition path: " + resourceId);
		}

		String deckPath = path.substring((ROOT_PATH + "/").length(), path.length() - DECK_FILE.length());
		if (deckPath.isBlank() || deckPath.contains("/")) {
			throw new IOException("Deck definitions must be in " + ROOT_PATH + "/<deck_id>.json: " + resourceId);
		}

		return new Identifier(resourceId.getNamespace(), deckPath);
	}
}
