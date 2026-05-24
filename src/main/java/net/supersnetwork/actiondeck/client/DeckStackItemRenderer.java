package net.supersnetwork.actiondeck.client;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.supersnetwork.actiondeck.block.DeckStackBlockEntity;

import java.util.List;

public class DeckStackItemRenderer {
	public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		List<Identifier> cards = DeckStackBlockEntity.readCardsFromStack(stack);
		int visibleCards = Math.min(cards.size(), 8);
		boolean faceDown = DeckStackBlockEntity.isFaceDown(stack);

		matrices.push();
		matrices.translate(0.5, 0.5, 0.5);
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0f));

		if (visibleCards == 0) {
			CardRenderHelper.renderStackCard(matrices, vertexConsumers, ActionDeckClientSetup.FALLBACK_CARD_ID, light, overlay, faceDown);
		} else {
			for (int i = 0; i < visibleCards; i++) {
				int cardIndex = faceDown ? visibleCards - 1 - i : cards.size() - visibleCards + i;
				matrices.push();
				matrices.translate(0.0, i * 0.064, 0.0);
				CardRenderHelper.renderStackCard(matrices, vertexConsumers, cards.get(cardIndex), light, overlay, faceDown);
				matrices.pop();
			}
		}

		matrices.pop();
	}
}
