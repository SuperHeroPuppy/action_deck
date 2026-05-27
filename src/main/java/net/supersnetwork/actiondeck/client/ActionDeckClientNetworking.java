package net.supersnetwork.actiondeck.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.supersnetwork.actiondeck.ActionDeck;
import net.supersnetwork.actiondeck.data.ActionDeckCardDefinitions;
import net.supersnetwork.actiondeck.data.ActionDeckDeckDefinitions;
import net.supersnetwork.actiondeck.network.ActionDeckNetworking;

@Environment(EnvType.CLIENT)
public final class ActionDeckClientNetworking {
	private ActionDeckClientNetworking() {
	}

	public static void registerClientHandlers() {
		ClientPlayNetworking.registerGlobalReceiver(ActionDeckNetworking.SYNC_DEFINITIONS, (client, handler, buffer, responseSender) -> {
			ActionDeckNetworking.SyncedDefinitions definitions = ActionDeckNetworking.readDefinitions(buffer);
			client.execute(() -> {
				ActionDeckCardDefinitions.applySynced(definitions.cards());
				ActionDeckDeckDefinitions.applySynced(definitions.decks());
				ActionDeck.LOGGER.info(
					"Received {} Action Deck card definitions and {} deck definitions from server",
					definitions.cards().size(),
					definitions.decks().size()
				);
			});
		});
	}
}