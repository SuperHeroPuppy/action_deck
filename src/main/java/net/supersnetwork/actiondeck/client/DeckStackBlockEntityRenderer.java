package net.supersnetwork.actiondeck.client;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.supersnetwork.actiondeck.block.DeckStackBlockEntity;

import java.util.List;

public class DeckStackBlockEntityRenderer implements BlockEntityRenderer<DeckStackBlockEntity> {
	public DeckStackBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
	}

	@Override
	public void render(DeckStackBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		List<Identifier> cards = entity.getCards();
		int visibleCards = Math.min(cards.size(), 16);

		matrices.push();
		matrices.translate(0.5, 0.015, 0.5);
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f));

		for (int i = 0; i < visibleCards; i++) {
			int cardIndex = cards.size() - visibleCards + i;
			matrices.push();
			matrices.translate(0.0, i * 0.064, 0.0);
			CardRenderHelper.renderStackCard(matrices, vertexConsumers, cards.get(cardIndex), light, overlay);
			matrices.pop();
		}

		matrices.pop();
	}
}
