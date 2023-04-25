package org.distril.beengine.util

import com.google.gson.JsonObject
import com.nukkitx.protocol.bedrock.data.skin.*
import java.nio.charset.StandardCharsets
import java.util.*

object SkinUtil {

	fun fromToken(skinJson: JsonObject): SerializedSkin {
		val builder = SerializedSkin.builder()
		if (skinJson.has("SkinId")) builder.skinId(skinJson["SkinId"].asString)
		if (skinJson.has("PlayFabId")) builder.playFabId(skinJson["PlayFabId"].asString)
		if (skinJson.has("SkinResourcePatch")) {
			builder.skinResourcePatch(
				String(
					Base64.getDecoder().decode(skinJson["SkinResourcePatch"].asString),
					StandardCharsets.UTF_8
				)
			)
		}

		builder.skinData(this.getImageData(skinJson, "Skin"))
		if (skinJson.has("AnimatedImageData")) {
			val animations = mutableListOf<AnimationData>()

			skinJson.getAsJsonArray("AnimatedImageData").forEach {
				animations.add(getAnimationData(it.asJsonObject))
			}

			builder.animations(animations)
		}

		if (skinJson.has("PersonaSkin")) {
			builder.persona(skinJson["PersonaSkin"].asBoolean)
			if (skinJson.has("PersonaPieces")) {
				val pieces = mutableListOf<PersonaPieceData>()

				skinJson.getAsJsonArray("PersonaPieces").forEach {
					pieces.add(getPersonaPieceData(it.asJsonObject))
				}

				builder.personaPieces(pieces)
			}

			if (skinJson.has("PieceTintColors")) {
				val tintColors = mutableListOf<PersonaPieceTintData>()

				skinJson.getAsJsonArray("PieceTintColors").forEach {
					tintColors.add(getPersonaPieceTintData(it.asJsonObject))
				}

				builder.tintColors(tintColors)
			}
		}

		builder.capeData(this.getImageData(skinJson, "Cape"))
		if (skinJson.has("SkinGeometryData")) {
			builder.geometryData(
				String(
					Base64.getDecoder().decode(skinJson["SkinGeometryData"].asString),
					StandardCharsets.UTF_8
				)
			)
		}

		if (skinJson.has("SkinAnimationData")) {
			builder.animationData(
				String(
					Base64.getDecoder().decode(skinJson["SkinAnimationData"].asString),
					StandardCharsets.UTF_8
				)
			)
		}

		if (skinJson.has("PremiumSkin")) builder.premium(skinJson["PremiumSkin"].asBoolean)
		if (skinJson.has("CapeOnClassicSkin")) builder.capeOnClassic(skinJson["CapeOnClassicSkin"].asBoolean)
		if (skinJson.has("CapeId")) builder.capeId(skinJson["CapeId"].asString)
		if (skinJson.has("SkinColor")) builder.skinColor(skinJson["SkinColor"].asString)
		if (skinJson.has("ArmSize")) builder.armSize(skinJson["ArmSize"].asString)

		return builder.build()
	}

	private fun getAnimationData(animationJson: JsonObject): AnimationData {
		val width = animationJson["ImageWidth"].asInt
		val height = animationJson["ImageHeight"].asInt
		val data = Base64.getDecoder().decode(animationJson["Image"].asString)
		val frames = animationJson["Frames"].asFloat
		val textureType = AnimatedTextureType.values()[animationJson["Type"].asInt]
		var expressionType = AnimationExpressionType.BLINKING
		if (animationJson.has("ExpressionType")) {
			expressionType = AnimationExpressionType.values()[animationJson["ExpressionType"].asInt]
		}

		return AnimationData(ImageData.of(width, height, data), textureType, frames, expressionType)
	}

	private fun getPersonaPieceData(pieceJson: JsonObject): PersonaPieceData {
		val pieceId = pieceJson["PieceId"].asString
		val pieceType = pieceJson["PieceType"].asString
		val packId = pieceJson["PackId"].asString
		val isDefault = pieceJson["IsDefault"].asBoolean
		val productId = pieceJson["ProductId"].asString
		return PersonaPieceData(pieceId, pieceType, packId, isDefault, productId)
	}

	private fun getPersonaPieceTintData(pieceTintJson: JsonObject): PersonaPieceTintData {
		val pieceType = pieceTintJson["PieceType"].asString
		val colors = mutableListOf<String>()
		pieceTintJson.getAsJsonArray("Colors").forEach {
			colors.add(it.asString)
		}

		return PersonaPieceTintData(pieceType, colors)
	}

	private fun getImageData(imageJson: JsonObject, name: String): ImageData {
		if (imageJson.has(name + "Data")) {
			val skinImage = Base64.getDecoder().decode(imageJson[name + "Data"].asString)
			return if (imageJson.has(name + "ImageHeight") && imageJson.has(name + "ImageWidth")) {
				val width = imageJson[name + "ImageWidth"].asInt
				val height = imageJson[name + "ImageHeight"].asInt
				ImageData.of(width, height, skinImage)
			} else {
				when (skinImage.size / 4) {
					2048 -> ImageData.of(64, 32, skinImage)
					4096 -> ImageData.of(64, 64, skinImage)
					8192 -> ImageData.of(128, 64, skinImage)
					16384 -> ImageData.of(128, 128, skinImage)
					else -> ImageData.EMPTY
				}
			}
		}

		return ImageData.EMPTY
	}
}
