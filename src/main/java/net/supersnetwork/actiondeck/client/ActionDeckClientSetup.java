package net.supersnetwork.actiondeck.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.supersnetwork.actiondeck.ActionDeck;
import net.supersnetwork.actiondeck.block.ActionDeckBlockEntities;
import net.supersnetwork.actiondeck.block.ActionDeckBlocks;
import net.supersnetwork.actiondeck.item.ActionDeckItems;

@Environment(EnvType.CLIENT)
public class ActionDeckClientSetup {
	public static final Identifier FALLBACK_CARD_ID = new Identifier(ActionDeck.MOD_ID, "generic_gold_spade_ace");

	public static void registerClientSystems() {
		ActionDeckClientNetworking.registerClientHandlers();
		BlockEntityRendererRegistry.register(ActionDeckBlockEntities.DECK_STACK, DeckStackBlockEntityRenderer::new);
		BuiltinItemRendererRegistry.INSTANCE.register(ActionDeckItems.CARD, new CardItemRenderer()::render);
		BuiltinItemRendererRegistry.INSTANCE.register(ActionDeckBlocks.DECK_STACK.asItem(), new DeckStackItemRenderer()::render);
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public Identifier getFabricId() {
				return new Identifier(ActionDeck.MOD_ID, "model_geometry");
			}

			@Override
			public void reload(ResourceManager manager) {
				CardRenderHelper.reloadModelGeometry(manager);
			}
		});
	}
}
