package net.supersnetwork.actiondeck.client;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.supersnetwork.actiondeck.data.ActionDeckCardDefinitions;
import net.supersnetwork.actiondeck.data.CardDefinition;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public final class CardRenderHelper {
	private static final Dimensions HAND_CARD = new Dimensions(-0.5f, 0.5f, -0.5f, 0.5f, 0.001f, 0.0f, 1.0f);
	private static final Dimensions STACK_CARD = new Dimensions(-0.3125f, 0.3125f, -0.4375f, 0.4375f, 0.0625f, 0.125f, 0.875f);

	private CardRenderHelper() {
	}

	public static void renderHandCard(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Identifier cardId, int light, int overlay) {
		renderCard(matrices, vertexConsumers, cardId, light, overlay, HAND_CARD, false);
	}

	public static void renderStackCard(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Identifier cardId, int light, int overlay) {
		renderCard(matrices, vertexConsumers, cardId, light, overlay, STACK_CARD, true);
	}

	private static void renderCard(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Identifier cardId, int light, int overlay, Dimensions dimensions, boolean renderSides) {
		CardDefinition definition = ActionDeckCardDefinitions.get(cardId).orElse(null);
		Identifier front = definition == null ? ActionDeckCardDefinitions.DEFAULT_FRONT_TEXTURE : definition.textures().front();
		Identifier back = definition == null ? ActionDeckCardDefinitions.DEFAULT_BACK_TEXTURE : definition.textures().back();

		Identifier frontTexture = texturePath(front);
		Identifier backTexture = texturePath(back);
		renderHorizontalQuad(matrices, vertexConsumers, backTexture, 0.0f, light, overlay, false, dimensions);
		renderHorizontalQuad(matrices, vertexConsumers, frontTexture, dimensions.thickness, light, overlay, true, dimensions);
		if (renderSides) {
			renderSideQuads(matrices, vertexConsumers, frontTexture, light, overlay, dimensions);
		}
	}

	private static void renderHorizontalQuad(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Identifier texture, float y, int light, int overlay, boolean top, Dimensions dimensions) {
		VertexConsumer vertices = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(texture));
		MatrixStack.Entry entry = matrices.peek();
		Matrix4f position = entry.getPositionMatrix();
		Matrix3f normal = entry.getNormalMatrix();
		float normalY = top ? 1.0f : -1.0f;

		if (top) {
			vertex(vertices, position, normal, dimensions.minX, y, dimensions.minZ, dimensions.minU, 0.0f, 0.0f, normalY, 0.0f, light, overlay);
			vertex(vertices, position, normal, dimensions.minX, y, dimensions.maxZ, dimensions.minU, 1.0f, 0.0f, normalY, 0.0f, light, overlay);
			vertex(vertices, position, normal, dimensions.maxX, y, dimensions.maxZ, dimensions.maxU, 1.0f, 0.0f, normalY, 0.0f, light, overlay);
			vertex(vertices, position, normal, dimensions.maxX, y, dimensions.minZ, dimensions.maxU, 0.0f, 0.0f, normalY, 0.0f, light, overlay);
		} else {
			vertex(vertices, position, normal, dimensions.maxX, y, dimensions.minZ, dimensions.maxU, 0.0f, 0.0f, normalY, 0.0f, light, overlay);
			vertex(vertices, position, normal, dimensions.maxX, y, dimensions.maxZ, dimensions.maxU, 1.0f, 0.0f, normalY, 0.0f, light, overlay);
			vertex(vertices, position, normal, dimensions.minX, y, dimensions.maxZ, dimensions.minU, 1.0f, 0.0f, normalY, 0.0f, light, overlay);
			vertex(vertices, position, normal, dimensions.minX, y, dimensions.minZ, dimensions.minU, 0.0f, 0.0f, normalY, 0.0f, light, overlay);
		}
	}

	private static void renderSideQuads(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Identifier texture, int light, int overlay, Dimensions dimensions) {
		VertexConsumer vertices = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(texture));
		MatrixStack.Entry entry = matrices.peek();
		Matrix4f position = entry.getPositionMatrix();
		Matrix3f normal = entry.getNormalMatrix();
		float sideVMin = 1.0f - dimensions.thickness;

		vertex(vertices, position, normal, dimensions.minX, 0.0f, dimensions.minZ, dimensions.minU, sideVMin, 0.0f, 0.0f, -1.0f, light, overlay);
		vertex(vertices, position, normal, dimensions.maxX, 0.0f, dimensions.minZ, dimensions.maxU, sideVMin, 0.0f, 0.0f, -1.0f, light, overlay);
		vertex(vertices, position, normal, dimensions.maxX, dimensions.thickness, dimensions.minZ, dimensions.maxU, 1.0f, 0.0f, 0.0f, -1.0f, light, overlay);
		vertex(vertices, position, normal, dimensions.minX, dimensions.thickness, dimensions.minZ, dimensions.minU, 1.0f, 0.0f, 0.0f, -1.0f, light, overlay);

		vertex(vertices, position, normal, dimensions.maxX, 0.0f, dimensions.maxZ, dimensions.maxU, sideVMin, 0.0f, 0.0f, 1.0f, light, overlay);
		vertex(vertices, position, normal, dimensions.minX, 0.0f, dimensions.maxZ, dimensions.minU, sideVMin, 0.0f, 0.0f, 1.0f, light, overlay);
		vertex(vertices, position, normal, dimensions.minX, dimensions.thickness, dimensions.maxZ, dimensions.minU, 1.0f, 0.0f, 0.0f, 1.0f, light, overlay);
		vertex(vertices, position, normal, dimensions.maxX, dimensions.thickness, dimensions.maxZ, dimensions.maxU, 1.0f, 0.0f, 0.0f, 1.0f, light, overlay);

		vertex(vertices, position, normal, dimensions.maxX, 0.0f, dimensions.minZ, dimensions.minU, 0.0f, 1.0f, 0.0f, 0.0f, light, overlay);
		vertex(vertices, position, normal, dimensions.maxX, 0.0f, dimensions.maxZ, dimensions.maxU, 0.0f, 1.0f, 0.0f, 0.0f, light, overlay);
		vertex(vertices, position, normal, dimensions.maxX, dimensions.thickness, dimensions.maxZ, dimensions.maxU, dimensions.thickness, 1.0f, 0.0f, 0.0f, light, overlay);
		vertex(vertices, position, normal, dimensions.maxX, dimensions.thickness, dimensions.minZ, dimensions.minU, dimensions.thickness, 1.0f, 0.0f, 0.0f, light, overlay);

		vertex(vertices, position, normal, dimensions.minX, 0.0f, dimensions.maxZ, dimensions.maxU, 0.0f, -1.0f, 0.0f, 0.0f, light, overlay);
		vertex(vertices, position, normal, dimensions.minX, 0.0f, dimensions.minZ, dimensions.minU, 0.0f, -1.0f, 0.0f, 0.0f, light, overlay);
		vertex(vertices, position, normal, dimensions.minX, dimensions.thickness, dimensions.minZ, dimensions.minU, dimensions.thickness, -1.0f, 0.0f, 0.0f, light, overlay);
		vertex(vertices, position, normal, dimensions.minX, dimensions.thickness, dimensions.maxZ, dimensions.maxU, dimensions.thickness, -1.0f, 0.0f, 0.0f, light, overlay);
	}

	private static void vertex(VertexConsumer vertices, Matrix4f position, Matrix3f normal, float x, float y, float z, float u, float v, float normalX, float normalY, float normalZ, int light, int overlay) {
		vertices.vertex(position, x, y, z)
			.color(255, 255, 255, 255)
			.texture(u, v)
			.overlay(overlay)
			.light(light)
			.normal(normal, normalX, normalY, normalZ)
			.next();
	}

	private static Identifier texturePath(Identifier texture) {
		return new Identifier(texture.getNamespace(), "textures/" + texture.getPath() + ".png");
	}

	private record Dimensions(float minX, float maxX, float minZ, float maxZ, float thickness, float minU, float maxU) {
	}
}
