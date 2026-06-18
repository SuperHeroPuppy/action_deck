package net.supersnetwork.actiondeck.client;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.supersnetwork.actiondeck.data.ActionDeckDeckDefinitions;
import net.supersnetwork.actiondeck.item.DeckPackItem;

public class DeckPackItemRenderer {
	private static final Identifier FALLBACK_TEXTURE = new Identifier("minecraft", "item/paper");

	public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		Identifier texture = DeckPackItem.getDeckId(stack)
			.flatMap(ActionDeckDeckDefinitions::get)
			.flatMap(deck -> deck.deckPack())
			.flatMap(deckPack -> deckPack.texture())
			.orElse(FALLBACK_TEXTURE);

		matrices.push();
		matrices.translate(0.5, 0.5, 0.5);
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0f));
		CardRenderHelper.renderFlatItem(matrices, vertexConsumers, texture, light, overlay);
		matrices.pop();
	}
}
