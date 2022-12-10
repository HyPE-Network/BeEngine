package org.distril.beengine.util;

import com.google.gson.JsonObject;
import com.nukkitx.protocol.bedrock.data.skin.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class SkinUtil {

	public static SerializedSkin fromToken(JsonObject skinToken) {
		SerializedSkin.Builder builder = SerializedSkin.builder();

		if (skinToken.has("SkinId")) {
			builder.skinId(skinToken.get("SkinId").getAsString());
		}

		if (skinToken.has("PlayFabId")) {
			builder.playFabId(skinToken.get("PlayFabId").getAsString());
		}

		if (skinToken.has("SkinResourcePatch")) {
			builder.skinResourcePatch(new String(Base64.getDecoder().decode(skinToken.get("SkinResourcePatch").getAsString()),
					StandardCharsets.UTF_8));
		}

		builder.skinData(SkinUtil.getImageData(skinToken, "Skin"));

		if (skinToken.has("AnimatedImageData")) {
			List<AnimationData> animations = new ArrayList<>();
			var array = skinToken.getAsJsonArray("AnimatedImageData");
			array.forEach(element -> animations.add(getAnimationData(element.getAsJsonObject())));

			builder.animations(animations);
		}

		if (skinToken.has("PersonaSkin")) {
			builder.persona(skinToken.get("PersonaSkin").getAsBoolean());
			if (skinToken.has("PersonaPieces")) {
				List<PersonaPieceData> pieces = new ArrayList<>();
				var array = skinToken.getAsJsonArray("PersonaPieces");
				array.forEach(element -> pieces.add(SkinUtil.getPersonaPieceData(element.getAsJsonObject())));

				builder.personaPieces(pieces);
			}

			if (skinToken.has("PieceTintColors")) {
				List<PersonaPieceTintData> pieces = new ArrayList<>();
				var array = skinToken.getAsJsonArray("PieceTintColors");
				array.forEach(element -> pieces.add(SkinUtil.getPersonaPieceTintData(element.getAsJsonObject())));

				builder.tintColors(pieces);
			}
		}

		builder.capeData(SkinUtil.getImageData(skinToken, "Cape"));

		if (skinToken.has("SkinGeometryData")) {
			builder.geometryData(new String(Base64.getDecoder().decode(skinToken.get("SkinGeometryData").getAsString()),
					StandardCharsets.UTF_8));
		}

		if (skinToken.has("SkinAnimationData")) {
			builder.animationData(new String(Base64.getDecoder().decode(skinToken.get("SkinAnimationData").getAsString()),
					StandardCharsets.UTF_8));
		}

		if (skinToken.has("PremiumSkin")) {
			builder.premium(skinToken.get("PremiumSkin").getAsBoolean());
		}

		if (skinToken.has("CapeOnClassicSkin")) {
			builder.capeOnClassic(skinToken.get("CapeOnClassicSkin").getAsBoolean());
		}

		if (skinToken.has("CapeId")) {
			builder.capeId(skinToken.get("CapeId").getAsString());
		}

		if (skinToken.has("SkinColor")) {
			builder.skinColor(skinToken.get("SkinColor").getAsString());
		}

		if (skinToken.has("ArmSize")) {
			builder.armSize(skinToken.get("ArmSize").getAsString());
		}

		return builder.build();
	}

	private static AnimationData getAnimationData(JsonObject object) {
		var width = object.get("ImageWidth").getAsInt();
		var height = object.get("ImageHeight").getAsInt();
		var data = Base64.getDecoder().decode(object.get("Image").getAsString());
		var frames = object.get("Frames").getAsFloat();
		var textureType = AnimatedTextureType.values()[object.get("Type").getAsInt()];
		AnimationExpressionType expressionType = AnimationExpressionType.BLINKING;
		if (object.has("ExpressionType")) {
			expressionType = AnimationExpressionType.values()[object.get("ExpressionType").getAsInt()];
		}

		return new AnimationData(ImageData.of(width, height, data), textureType, frames, expressionType);
	}

	private static PersonaPieceData getPersonaPieceData(JsonObject skinToken) {
		var pieceId = skinToken.get("PieceId").getAsString();
		var pieceType = skinToken.get("PieceType").getAsString();
		var packId = skinToken.get("PackId").getAsString();
		var isDefault = skinToken.get("IsDefault").getAsBoolean();
		var productId = skinToken.get("ProductId").getAsString();

		return new PersonaPieceData(pieceId, pieceType, packId, isDefault, productId);
	}

	private static PersonaPieceTintData getPersonaPieceTintData(JsonObject skinToken) {
		var pieceType = skinToken.get("PieceType").getAsString();

		List<String> colors = new ArrayList<>();
		skinToken.getAsJsonArray("Colors").forEach(element -> colors.add(element.getAsString()));

		return new PersonaPieceTintData(pieceType, colors);
	}

	private static ImageData getImageData(JsonObject token, String name) {
		if (token.has(name + "Data")) {
			var skinImage = Base64.getDecoder().decode(token.get(name + "Data").getAsString());
			if (token.has(name + "ImageHeight") && token.has(name + "ImageWidth")) {
				var width = token.get(name + "ImageWidth").getAsInt();
				var height = token.get(name + "ImageHeight").getAsInt();
				return ImageData.of(width, height, skinImage);
			} else {
				return switch (skinImage.length / 4) {
					case 2048 -> ImageData.of(64, 32, skinImage);
					case 4096 -> ImageData.of(64, 64, skinImage);
					case 8192 -> ImageData.of(128, 64, skinImage);
					case 16384 -> ImageData.of(128, 128, skinImage);
					default -> ImageData.EMPTY;
				};
			}
		}

		return ImageData.EMPTY;
	}
}
