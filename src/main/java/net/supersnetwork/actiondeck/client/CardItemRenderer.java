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
		Identifier cardId = Card.getCardId(stack).orElse(ActionDeckClientSetup.FALLBACK_CARD_ID);

		matrices.push();
		matrices.translate(0.5, 0.5, 0.5);
		applyDisplayTransform(mode, matrices);
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0f));
		CardRenderHelper.renderHandCard(matrices, vertexConsumers, cardId, light, overlay);
		matrices.pop();
	}

	private static void applyDisplayTransform(ModelTransformationMode mode, MatrixStack matrices) {
		switch (mode) {
			case THIRD_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND -> {
				matrices.translate(0.0 / 16.0, 3.25 / 16.0, 3.5 / 16.0);
				matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(43.25f));
				matrices.scale(0.55f, 0.55f, 0.55f);
			}
			case FIRST_PERSON_RIGHT_HAND, FIRST_PERSON_LEFT_HAND -> {
				matrices.translate(0.25 / 16.0, 5.0 / 16.0, 1.0 / 16.0);
				matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-12.75f));
				matrices.scale(0.55f, 0.55f, 0.55f);
			}
			case GROUND -> {
				matrices.translate(0.0, 3.0 / 16.0, 0.0);
				matrices.scale(0.25f, 0.25f, 0.25f);
			}
			case GUI -> matrices.scale(0.9f, 0.9f, 0.9f);
			case HEAD -> {
				matrices.translate(0.0, 14.25 / 16.0, 7.0 / 16.0);
				matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-180.0f));
			}
			case FIXED -> matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-180.0f));
			default -> {
			}
		}
	}
}
