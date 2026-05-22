package net.supersnetwork.actiondeck;

import net.fabricmc.api.ModInitializer;
import net.supersnetwork.actiondeck.block.ActionDeckBlockEntities;
import net.supersnetwork.actiondeck.block.ActionDeckBlocks;
import net.supersnetwork.actiondeck.command.ActionDeckCommands;
import net.supersnetwork.actiondeck.data.ActionDeckCardDefinitions;
import net.supersnetwork.actiondeck.data.ActionDeckDeckDefinitions;
import net.supersnetwork.actiondeck.item.ActionDeckItems;
import net.supersnetwork.actiondeck.recipe.ActionDeckRecipes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActionDeck implements ModInitializer {
	public static final String MOD_ID = "action_deck";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Action Deck");
		ActionDeckItems.register();
		ActionDeckBlocks.register();
		ActionDeckBlockEntities.register();
		ActionDeckRecipes.register();
		ActionDeckCardDefinitions.register();
		ActionDeckDeckDefinitions.register();
		ActionDeckCommands.register();
	}
}
