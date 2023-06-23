package org.distril.beengine.util

import com.google.gson.JsonObject
import com.nukkitx.protocol.bedrock.data.skin.*
import java.nio.charset.StandardCharsets
import java.util.*

object SkinUtil {

	fun fromToken(skinJson: JsonObject): SerializedSkin {
		val builder = SerializedSkin.builder()

		builder.skinId(skinJson["SkinId"]?.asString)
		builder.playFabId(skinJson["PlayFabId"]?.asString)

		skinJson["SkinResourcePatch"]?.asString?.let {
			builder.skinResourcePatch(String(Base64.getDecoder().decode(it), StandardCharsets.UTF_8))
		}

		builder.skinData(this.getImageData(skinJson, "Skin"))

		val animations = skinJson.getAsJsonArray("AnimatedImageData")?.map {
			this.getAnimationData(it.asJsonObject)
		}
		builder.animations(animations)

		skinJson["PersonaSkin"]?.asBoolean?.let { personaSkin ->
			builder.persona(personaSkin)

			val pieces = skinJson.getAsJsonArray("PersonaPieces")?.map {
				this.getPersonaPieceData(it.asJsonObject)
			}
			builder.personaPieces(pieces)

			val tintColors = skinJson.getAsJsonArray("PieceTintColors")?.map {
				this.getPersonaPieceTintData(it.asJsonObject)
			}
			builder.tintColors(tintColors)
		}

		builder.capeData(this.getImageData(skinJson, "Cape"))

		skinJson["SkinGeometryData"]?.asString?.let {
			builder.geometryData(String(Base64.getDecoder().decode(it), StandardCharsets.UTF_8))
		}

		skinJson["SkinAnimationData"]?.asString?.let {
			builder.animationData(String(Base64.getDecoder().decode(it), StandardCharsets.UTF_8))
		}

		builder.premium(skinJson["PremiumSkin"]?.asBoolean == true)
		builder.capeOnClassic(skinJson["CapeOnClassicSkin"]?.asBoolean == true)
		builder.capeId(skinJson["CapeId"]?.asString)
		builder.skinColor(skinJson["SkinColor"]?.asString)
		builder.armSize(skinJson["ArmSize"]?.asString)

		return builder.build()
	}

	private fun getAnimationData(animationJson: JsonObject): AnimationData {
		val width = animationJson["ImageWidth"].asInt
		val height = animationJson["ImageHeight"].asInt
		val data = Base64.getDecoder().decode(animationJson["Image"].asString)
		val frames = animationJson["Frames"].asFloat
		val textureType = AnimatedTextureType.values()[animationJson["Type"].asInt]
		var expressionType = AnimationExpressionType.BLINKING
		animationJson["ExpressionType"]?.asInt?.let {
			expressionType = AnimationExpressionType.values()[it]
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
		val colors = pieceTintJson.getAsJsonArray("Colors").map { it.asString }

		return PersonaPieceTintData(pieceType, colors)
	}

	private fun getImageData(imageJson: JsonObject, name: String): ImageData {
		val dataKey = name + "Data"
		val widthKey = name + "ImageWidth"
		val heightKey = name + "ImageHeight"

		if (imageJson.has(dataKey)) {
			val skinImage = Base64.getDecoder().decode(imageJson[dataKey].asString)
			val width = imageJson[widthKey]?.asInt
			val height = imageJson[heightKey]?.asInt

			return if (width != null && height != null) {
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
