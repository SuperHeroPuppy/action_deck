package net.supersnetwork.actiondeck.block;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.supersnetwork.actiondeck.ActionDeck;
import net.supersnetwork.actiondeck.item.DeckStackItem;

public class ActionDeckBlocks {
	public static final Block DECK_STACK = register("deck_stack", new DeckStackBlock(AbstractBlock.Settings.create()
		.mapColor(MapColor.WHITE_GRAY)
		.strength(0.4f)
		.sounds(BlockSoundGroup.WOOL)
		.nonOpaque()));

	private static Block register(String id, Block block) {
		Identifier identifier = new Identifier(ActionDeck.MOD_ID, id);
		Registry.register(Registries.ITEM, identifier, new DeckStackItem(block, new Item.Settings()));
		return Registry.register(Registries.BLOCK, identifier, block);
	}

	public static void register() {
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(content -> {
			content.add(DECK_STACK);
		});
	}
}
