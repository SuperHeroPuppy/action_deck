package net.supersnetwork.actiondeck.client;

import net.fabricmc.api.ClientModInitializer;
import net.supersnetwork.actiondeck.ActionDeck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActionDeckClient implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger(ActionDeck.MOD_ID);

	@Override
	public void onInitializeClient() {
		LOGGER.info("Initializing Action Deck Client");
		ActionDeckClientSetup.registerClientSystems();
	}
}
