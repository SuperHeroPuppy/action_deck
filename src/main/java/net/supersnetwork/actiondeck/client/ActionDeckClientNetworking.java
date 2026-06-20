package net.supersnetwork.actiondeck.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
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
			decodeAndApply(client, buffer, false);
		});
		ClientPlayNetworking.registerGlobalReceiver(ActionDeckNetworking.LEGACY_SYNC_DEFINITIONS, (client, handler, buffer, responseSender) -> {
			decodeAndApply(client, buffer, true);
		});
	}

	private static void decodeAndApply(MinecraftClient client, PacketByteBuf buffer, boolean legacy) {
		final ActionDeckNetworking.SyncedDefinitions definitions;
		try {
			definitions = legacy
				? ActionDeckNetworking.readLegacyDefinitions(buffer)
				: ActionDeckNetworking.readDefinitions(buffer);
		} catch (RuntimeException exception) {
			ActionDeck.LOGGER.error(
				"Rejected malformed Action Deck {} definition packet",
				legacy ? "legacy" : "v2",
				exception
			);
			return;
		}

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
