package net.supersnetwork.actiondeck.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.supersnetwork.actiondeck.ActionDeck;
import net.supersnetwork.actiondeck.data.ActionDeckCardDefinitions;
import net.supersnetwork.actiondeck.data.ActionDeckDeckDefinitions;
import net.supersnetwork.actiondeck.network.ActionDeckNetworking;

@Environment(EnvType.CLIENT)
public final class ActionDeckClientNetworking {
	private ActionDeckClientNetworking() {
	}

	public static void registerClientHandlers() {
		ActionDeckNetworking.registerPayloadTypes();
		ClientPlayNetworking.registerGlobalReceiver(ActionDeckNetworking.SYNC_DEFINITIONS, (payload, context) -> {
			apply(context.client(), payload.definitions(), false);
		});
		ClientPlayNetworking.registerGlobalReceiver(ActionDeckNetworking.LEGACY_SYNC_DEFINITIONS, (payload, context) -> {
			apply(context.client(), payload.definitions(), true);
		});
	}

	private static void apply(MinecraftClient client, ActionDeckNetworking.SyncedDefinitions definitions, boolean legacy) {
		client.execute(() -> {
			ActionDeckCardDefinitions.applySynced(definitions.cards());
			ActionDeckDeckDefinitions.applySynced(definitions.decks());
			ActionDeck.LOGGER.info(
				"Received {} Action Deck card definitions and {} deck definitions from server ({})",
				definitions.cards().size(),
				definitions.decks().size(),
				legacy ? "legacy protocol" : "protocol v2"
			);
		});
	}
}
