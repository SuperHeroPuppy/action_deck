package net.supersnetwork.actiondeck.client;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.supersnetwork.actiondeck.item.Card;

public class CardItemRenderer {
	public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		Identifier cardId = Card.getCardId(stack).orElse(null);

		matrices.push();
		matrices.translate(0.5, 0.5, 0.5);
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0f));
		if (cardId == null) {
			CardRenderHelper.renderDefaultHandCard(matrices, vertexConsumers, light, overlay);
		} else {
			CardRenderHelper.renderHandCard(matrices, vertexConsumers, cardId, light, overlay);
		}
		matrices.pop();
	}
}
