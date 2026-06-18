package net.supersnetwork.actiondeck.item;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.supersnetwork.actiondeck.ActionDeck;

public class ActionDeckItems {
	public static final Item CARD = register(new Card(new Item.Settings()), "card");
	public static final Item DECK_PACK = register(new DeckPackItem(new Item.Settings()), "deck_pack");

	public static Item register(Item item, String id) {
		return Registry.register(Registries.ITEM, new Identifier(ActionDeck.MOD_ID, id), item);
	}

	public static void register() {
		// Add items to creative tab
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(content -> {
			content.add(CARD);
		});
	}
}
