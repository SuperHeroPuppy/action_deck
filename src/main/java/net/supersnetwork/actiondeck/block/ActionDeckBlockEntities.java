package net.supersnetwork.actiondeck.block;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.supersnetwork.actiondeck.ActionDeck;

public class ActionDeckBlockEntities {
	public static final BlockEntityType<DeckStackBlockEntity> DECK_STACK = Registry.register(
		Registries.BLOCK_ENTITY_TYPE,
		new Identifier(ActionDeck.MOD_ID, "deck_stack"),
		BlockEntityType.Builder.create(DeckStackBlockEntity::new, ActionDeckBlocks.DECK_STACK).build(null)
	);

	public static void register() {
	}
}
