package net.supersnetwork.actiondeck.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Identifier;
import net.supersnetwork.actiondeck.ActionDeck;
import net.supersnetwork.actiondeck.data.ActionDeckCardDefinitions;
import net.supersnetwork.actiondeck.data.CardDefinition;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.io.BufferedReader;
import java.util.Optional;

public final class CardRenderHelper {
	private static final Dimensions HAND_CARD = new Dimensions(-0.5f, 0.5f, -0.5f, 0.5f, 0.001f, 0.0f, 1.0f);
	private static final Identifier DECK_STACK_MODEL = new Identifier(ActionDeck.MOD_ID, "models/deck_stack_base.json");
	private static final Dimensions DEFAULT_STACK_CARD = new Dimensions(-0.3125f, 0.3125f, -0.4375f, 0.4375f, 0.0625f, 0.125f, 0.875f);
	private static Dimensions stackCard = DEFAULT_STACK_CARD;
	private static boolean stackCardLoaded;

	private CardRenderHelper() {
	}

	public static void reloadModelGeometry(ResourceManager manager) {
		stackCard = loadStackDimensions(manager).orElse(DEFAULT_STACK_CARD);
		stackCardLoaded = true;
	}

	public static void renderDefaultHandCard(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		renderCard(matrices, vertexConsumers, null, light, overlay, HAND_CARD, false, false);
	}

	public static void renderHandCard(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Identifier cardId, int light, int overlay) {
		renderCard(matrices, vertexConsumers, cardId, light, overlay, HAND_CARD, false, false);
	}

	public static void renderFlatItem(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Identifier texture, int light, int overlay) {
		Identifier texturePath = texturePath(texture);
		renderHorizontalQuad(matrices, vertexConsumers, texturePath, 0.0f, light, overlay, false, HAND_CARD);
		renderHorizontalQuad(matrices, vertexConsumers, texturePath, HAND_CARD.thickness, light, overlay, true, HAND_CARD);
	}

	public static void renderStackCard(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Identifier cardId, int light, int overlay) {
		renderStackCard(matrices, vertexConsumers, cardId, light, overlay, false);
	}

	public static void renderStackCard(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Identifier cardId, int light, int overlay, boolean faceDown) {
		renderCard(matrices, vertexConsumers, cardId, light, overlay, stackCard(), true, faceDown);
	}

	private static void renderCard(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Identifier cardId, int light, int overlay, Dimensions dimensions, boolean renderSides, boolean faceDown) {
		CardDefinition definition = ActionDeckCardDefinitions.get(cardId).orElse(null);
		Identifier front = definition == null ? ActionDeckCardDefinitions.DEFAULT_FRONT_TEXTURE : definition.textures().front();
		Identifier back = definition == null ? ActionDeckCardDefinitions.DEFAULT_BACK_TEXTURE : definition.textures().back();

		Identifier frontTexture = texturePath(front);
		Identifier backTexture = texturePath(back);
		Identifier topTexture = faceDown ? backTexture : frontTexture;
		Identifier bottomTexture = faceDown ? frontTexture : backTexture;
		Identifier sideTexture = faceDown ? backTexture : frontTexture;

		renderHorizontalQuad(matrices, vertexConsumers, bottomTexture, 0.0f, light, overlay, false, dimensions);
		renderHorizontalQuad(matrices, vertexConsumers, topTexture, dimensions.thickness, light, overlay, true, dimensions);
		if (renderSides) {
			renderSideQuads(matrices, vertexConsumers, sideTexture, light, overlay, dimensions);
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

	private static Optional<Dimensions> loadStackDimensions(ResourceManager manager) {
		Optional<Resource> resource = manager.getResource(DECK_STACK_MODEL);
		if (resource.isEmpty()) {
			return Optional.empty();
		}

		try (BufferedReader reader = resource.get().getReader()) {
			JsonObject json = JsonHelper.asObject(JsonParser.parseReader(reader), "deck stack model");
			JsonArray elements = JsonHelper.getArray(json, "elements");
			if (elements.isEmpty()) {
				return Optional.empty();
			}

			JsonObject element = JsonHelper.asObject(elements.get(0), "deck stack model element");
			float[] from = readVector(element, "from");
			float[] to = readVector(element, "to");
			float[] uv = readFaceUv(element, "up");

			return Optional.of(new Dimensions(
				toRenderCoordinate(from[0]),
				toRenderCoordinate(to[0]),
				toRenderCoordinate(from[2]),
				toRenderCoordinate(to[2]),
				(to[1] - from[1]) / 16.0f,
				Math.min(uv[0], uv[2]) / 16.0f,
				Math.max(uv[0], uv[2]) / 16.0f
			));
		} catch (Exception exception) {
			ActionDeck.LOGGER.warn("Failed to load deck stack model geometry from {}", DECK_STACK_MODEL, exception);
			return Optional.empty();
		}
	}

	private static Dimensions stackCard() {
		if (!stackCardLoaded && MinecraftClient.getInstance().getResourceManager() != null) {
			reloadModelGeometry(MinecraftClient.getInstance().getResourceManager());
		}
		return stackCard;
	}

	private static float[] readVector(JsonObject object, String key) {
		JsonArray array = JsonHelper.getArray(object, key);
		if (array.size() != 3) {
			throw new IllegalArgumentException(key + " must contain 3 values");
		}
		return new float[] {
			JsonHelper.asFloat(array.get(0), key + "[0]"),
			JsonHelper.asFloat(array.get(1), key + "[1]"),
			JsonHelper.asFloat(array.get(2), key + "[2]")
		};
	}

	private static float[] readFaceUv(JsonObject element, String face) {
		JsonObject faces = JsonHelper.getObject(element, "faces");
		JsonObject faceObject = JsonHelper.getObject(faces, face);
		JsonArray array = JsonHelper.getArray(faceObject, "uv");
		if (array.size() != 4) {
			throw new IllegalArgumentException(face + " uv must contain 4 values");
		}
		return new float[] {
			JsonHelper.asFloat(array.get(0), face + ".uv[0]"),
			JsonHelper.asFloat(array.get(1), face + ".uv[1]"),
			JsonHelper.asFloat(array.get(2), face + ".uv[2]"),
			JsonHelper.asFloat(array.get(3), face + ".uv[3]")
		};
	}

	private static float toRenderCoordinate(float modelCoordinate) {
		return (modelCoordinate - 8.0f) / 16.0f;
	}

	private record Dimensions(float minX, float maxX, float minZ, float maxZ, float thickness, float minU, float maxU) {
	}
}
