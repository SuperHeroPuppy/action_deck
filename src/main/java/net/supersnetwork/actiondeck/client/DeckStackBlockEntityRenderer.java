package net.supersnetwork.actiondeck.client;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.supersnetwork.actiondeck.block.DeckStackBlock;
import net.supersnetwork.actiondeck.block.DeckStackBlockEntity;

import java.util.List;

public class DeckStackBlockEntityRenderer implements BlockEntityRenderer<DeckStackBlockEntity> {
	private static final int MAX_VISIBLE_CARDS = 16;
	private static final float CARD_SPACING = 1.0f / 16.0f;

	public DeckStackBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
	}

	@Override
	public void render(DeckStackBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		List<Identifier> cards = entity.getCards();
		int visibleCards = Math.min(cards.size(), MAX_VISIBLE_CARDS);
		boolean faceDown = entity.isFaceDown();
		float rotationDegrees = entity.getCachedState().get(DeckStackBlock.ROTATION) * 22.5f;

		matrices.push();
		matrices.translate(0.5, 0.0, 0.5);
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f + rotationDegrees));

		for (int i = 0; i < visibleCards; i++) {
			int cardIndex = faceDown ? visibleCards - 1 - i : cards.size() - visibleCards + i;
			matrices.push();
			matrices.translate(0.0, i * CARD_SPACING, 0.0);
			CardRenderHelper.renderStackCard(matrices, vertexConsumers, cards.get(cardIndex), light, overlay, faceDown);
			matrices.pop();
		}

		matrices.pop();
	}
}
